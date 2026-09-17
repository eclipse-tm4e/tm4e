/**
 * Copyright (c) 2026 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.ui.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.GrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies isolation, import order and cache updates through the grammar registry's edit-session API.
 * Each test removes its own imports and temporary files without changing other registered grammars.
 */
class GrammarRegistryImportTest {

	/** Makes the import-order regression independent of the JVM's object identity hash codes. */
	private static final class TestGrammarDefinition extends GrammarDefinition {
		private final transient int hashCode;

		TestGrammarDefinition(final String scope, final String path, final int hashCode) {
			super(scope, path);
			this.hashCode = hashCode;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(final @Nullable Object other) {
			// Preserve identity equality; the chosen hash only controls test iteration order.
			return this == other;
		}
	}

	private final IGrammarRegistryManager registry = TMEclipseRegistryPlugin.getGrammarRegistryManager();
	private final List<IGrammarDefinition> grammars = new ArrayList<>();
	private final List<Path> files = new ArrayList<>();

	@AfterEach
	void cleanUp() throws Exception {
		final var session = registry.newEditSession();
		for (final var grammar : grammars) {
			session.unregisterGrammarDefinition(grammar);
		}
		session.save();
		for (final var file : files) {
			Files.deleteIfExists(file);
		}
	}

	@Test
	void cancelledAdditionDoesNotChangeOtherSessionsOrTheLiveRegistry() throws Exception {
		final var scope = "source.tm4e-import-test-" + UUID.randomUUID();
		final var first = createGrammar(scope, "first");
		saveImport(first);
		final var session = registry.newEditSession();
		final var otherSession = registry.newEditSession();
		final var second = createGrammar(scope, "second");

		session.registerGrammarDefinition(second);
		assertDefinitions(session, scope, first, second);
		assertDefinitions(registry, scope, first);
		assertDefinitions(otherSession, scope, first);

		session.reset();
		assertDefinitions(session, scope, first);
	}

	@Test
	void cancelledRemovalDoesNotChangeOtherSessionsOrTheLiveRegistry() throws Exception {
		final var scope = "source.tm4e-import-test-" + UUID.randomUUID();
		final var grammar = createGrammar(scope, "original");
		saveImport(grammar);
		final var session = registry.newEditSession();
		final var otherSession = registry.newEditSession();

		session.unregisterGrammarDefinition(grammar);
		assertDefinitions(session, scope);
		assertDefinitions(registry, scope, grammar);
		assertDefinitions(otherSession, scope, grammar);

		session.reset();
		assertDefinitions(session, scope, grammar);
	}

	@Test
	void savingKeepsTheFirstImportForASharedScope() throws Exception {
		final var scope = "source.tm4e-import-test-" + UUID.randomUUID();
		// These hash codes make an unordered set save the second import first.
		final var first = createGrammar(scope, "first", 1);
		final var second = createGrammar(scope, "second", 0);
		final var session = registry.newEditSession();
		session.registerGrammarDefinition(first);
		session.registerGrammarDefinition(second);
		assertGrammarName(session, first.getScope(), "first");

		session.save();
		assertDefinitions(registry, scope, first, second);
		assertGrammarName(registry, first.getScope(), "first");
		assertGrammarName(reloadRegistry(), first.getScope(), "first");
	}

	@Test
	void replacingALoadedImportReloadsAnOlderSource() throws Exception {
		final var scope = "source.tm4e-import-test-" + UUID.randomUUID();
		final var first = createGrammar(scope, "original");
		saveImport(first);
		assertGrammarName(registry, first.getScope(), "original");
		final var replacement = createGrammar(scope, "replacement");
		// The source changed even though its timestamp is older. No reload delay should be necessary.
		Files.setLastModifiedTime(Path.of(replacement.getPath()), FileTime.fromMillis(first.getLastModified() - 60_000));
		final var session = registry.newEditSession();
		session.registerGrammarDefinition(replacement);
		session.unregisterGrammarDefinition(first);

		session.save();
		assertDefinitions(registry, scope, replacement);
		assertGrammarName(registry, first.getScope(), "replacement");
	}

	@Test
	void replacingALoadedImportLoadsNewDependencies() throws Exception {
		final var scope = "source.tm4e-import-test-" + UUID.randomUUID();
		final var original = createGrammar(scope, "original");
		saveImport(original);
		assertGrammarName(registry, original.getScope(), "original");

		final var dependencyScope = scope + ".dependency";
		final var dependency = createGrammar(dependencyScope, "dependency",
				"{\"match\":\"x\",\"name\":\"keyword.tm4e-import-test\"}");
		final var replacement = createGrammar(scope, "replacement", "{\"include\":\"" + dependencyScope + "\"}");
		final var session = registry.newEditSession();
		// Register the dependency without loading it. A direct lookup would hide a missing dependency load in the replacement.
		session.registerGrammarDefinition(dependency);
		session.registerGrammarDefinition(replacement);
		session.unregisterGrammarDefinition(original);
		session.save();

		assertThat(registry.getGrammarForScope(replacement.getScope())).isNotNull()
				.extracting(grammar -> grammar.tokenizeLine("x").getTokens()[0].getScopes())
				.isEqualTo(List.of(scope, "keyword.tm4e-import-test"));
	}

	@Test
	void replacingALoadedImportRetriesAfterADependencyLoadFails() throws Exception {
		final var scope = "source.tm4e-import-test-" + UUID.randomUUID();
		final var original = createGrammar(scope, "original");
		saveImport(original);
		assertGrammarName(registry, original.getScope(), "original");

		final var dependencyScope = scope + ".dependency";
		final var dependency = createGrammar(dependencyScope, "dependency",
				"{\"match\":\"x\",\"name\":\"keyword.tm4e-import-test\"}");
		final var dependencyFile = Path.of(dependency.getPath());
		final var validDependency = Files.readString(dependencyFile);
		Files.writeString(dependencyFile, "{invalid}");
		final var replacement = createGrammar(scope, "replacement", "{\"include\":\"" + dependencyScope + "\"}");
		final var session = registry.newEditSession();
		session.registerGrammarDefinition(dependency);
		session.registerGrammarDefinition(replacement);
		session.unregisterGrammarDefinition(original);
		session.save();

		assertThatThrownBy(() -> registry.getGrammarForScope(replacement.getScope())).isInstanceOf(TMException.class);
		// Keep the dependency invalid for a retry, so cleanup is checked for both the reload and the retry.
		assertThatThrownBy(() -> registry.getGrammarForScope(replacement.getScope())).isInstanceOf(TMException.class);
		// Correct only the dependency. The unchanged root must not bypass the failed dependency load on the next lookup.
		Files.writeString(dependencyFile, validDependency);
		assertThat(registry.getGrammarForScope(replacement.getScope())).isNotNull()
				.extracting(grammar -> grammar.tokenizeLine("x").getTokens()[0].getScopes())
				.isEqualTo(List.of(scope, "keyword.tm4e-import-test"));
	}

	@Test
	void removingALoadedImportDoesNotReturnItsCachedGrammar() throws Exception {
		final var scope = "source.tm4e-import-test-" + UUID.randomUUID();
		final var grammar = createGrammar(scope, "original");
		saveImport(grammar);
		assertGrammarName(registry, grammar.getScope(), "original");
		final var session = registry.newEditSession();
		session.unregisterGrammarDefinition(grammar);

		session.save();
		assertThat(registry.getGrammarForScope(grammar.getScope())).isNull();
	}

	@Test
	void importingAfterAnUnsuccessfulLookupLoadsTheGrammar() throws Exception {
		final var scope = "source.tm4e-import-test-" + UUID.randomUUID();
		final var grammar = createGrammar(scope, "imported");
		assertThat(registry.getGrammarForScope(grammar.getScope())).isNull();

		saveImport(grammar);
		assertGrammarName(registry, grammar.getScope(), "imported");
	}

	private void saveImport(final IGrammarDefinition grammar) throws Exception {
		final var session = registry.newEditSession();
		session.registerGrammarDefinition(grammar);
		session.save();
	}

	private static void assertDefinitions(final IGrammarRegistryManager registry, final String scope,
			final IGrammarDefinition... expected) {
		assertThat(registry.getDefinitions()).filteredOn(definition -> scope.equals(definition.getScope().getName()))
				.containsExactly(expected);
	}

	private static void assertGrammarName(final IGrammarRegistryManager registry, final ITMScope scope, final String expected) {
		assertThat(registry.getGrammarForScope(scope)).isNotNull().extracting(IGrammar::getName).isEqualTo(expected);
	}

	private IGrammarRegistryManager reloadRegistry() throws Exception {
		// Read the saved preferences through a separate instance, leaving the shared registry used by other tests intact.
		final var implementation = registry.getClass();
		final var constructor = implementation.getDeclaredConstructor();
		constructor.setAccessible(true);
		final var restored = constructor.newInstance();
		final var load = implementation.getDeclaredMethod("load");
		load.setAccessible(true);
		load.invoke(restored);
		return restored;
	}

	private IGrammarDefinition createGrammar(final String scope, final String name, final String... patterns) throws Exception {
		return createGrammar(scope, name, grammars.size(), patterns);
	}

	private IGrammarDefinition createGrammar(final String scope, final String name, final int hashCode, final String... patterns)
			throws Exception {
		final var file = Files.createTempFile("tm4e-grammar-import-", ".json");
		files.add(file);
		Files.writeString(file, "{\"scopeName\":\"" + scope + "\",\"name\":\"" + name + "\",\"patterns\":["
				+ String.join(",", patterns) + "]}");
		final var grammar = new TestGrammarDefinition(scope, file.toString(), hashCode);
		grammars.add(grammar);
		return grammar;
	}
}
