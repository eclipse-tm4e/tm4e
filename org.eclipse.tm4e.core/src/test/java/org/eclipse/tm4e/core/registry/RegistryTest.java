/**
 * Copyright (c) 2023 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.core.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.registry.IGrammarSource.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests grammar registration, dependency loading, and tokenization through the registry.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class RegistryTest {

	@Test
	void testLanguagePackGrammars() throws IOException {
		final var reg = new Registry();
		final var count = new AtomicInteger();
		Files.walkFileTree(Paths.get("../org.eclipse.tm4e.language_pack"), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final @Nullable BasicFileAttributes attrs) throws IOException {
				if (file.getFileName().toString().endsWith("tmLanguage.json")) {
					try (var input = Files.newBufferedReader(file)) {
						System.out.println("Parsing [" + file + "]...");
						final var grammar = reg.addGrammar(IGrammarSource.fromFile(file));
						count.incrementAndGet();
						assertThat(grammar.getScopeName()).isNotBlank();
						assertThat(grammar.getFileTypes()).isNotNull();
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		System.out.println("Successfully parsed " + count.intValue() + " grammars.");
		assertThat(count).as("Number of grammars found.").hasValueGreaterThan(10);
	}

	@Test
	void testLoadingDependenciesOfEqualRules() {
		assertLoadingDependenciesOfEqualRules("fixture.first", "fixture.second");
	}

	@Test
	void testLoadingDependenciesOfEqualRulesInReverseOrder() {
		assertLoadingDependenciesOfEqualRules("fixture.second", "fixture.first");
	}

	private void assertLoadingDependenciesOfEqualRules(final String firstInclude, final String secondInclude) {
		// The identical #comments includes must resolve against each grammar's own repository.
		final var grammars = Map.of(
				"fixture.host", """
					{ "scopeName": "fixture.host",
					  "patterns": [ { "include": "%s" }, { "include": "%s" } ] }
					""".formatted(firstInclude, secondInclude),
				"fixture.first", """
					{ "scopeName": "fixture.first",
					  "patterns": [ { "include": "#comments" } ],
					  "repository": { "comments": { "match": "#.*", "name": "comment.first" } } }
					""",
				"fixture.second", """
					{ "scopeName": "fixture.second",
					  "patterns": [ { "include": "#comments" } ],
					  "repository": { "comments": { "patterns": [ { "include": "fixture.guest" } ] } } }
					""",
				"fixture.guest", """
					{ "scopeName": "fixture.guest",
					  "patterns": [ { "match": "\\\\w+", "name": "guest.word" } ] }
					""");
		final var requestedScopes = new ArrayList<String>();
		// A fresh registry for each order prevents cached dependencies from hiding a missed include.
		final var registry = new Registry(new IRegistryOptions() {
			@Override
			public @Nullable IGrammarSource getGrammarSource(final String scopeName) {
				requestedScopes.add(scopeName);
				final var content = grammars.get(scopeName);
				return content == null ? null : IGrammarSource.fromString(ContentType.JSON, content);
			}
		});
		final var grammar = castNonNull(registry.loadGrammar("fixture.host"));
		assertThat(requestedScopes).contains("fixture.guest");
		assertThat(grammar.tokenizeLine("hello").getTokens()).singleElement().satisfies(
				token -> assertThat(token.getScopes()).containsExactly("fixture.host", "guest.word"));
	}

	@Test
	void testLoadingRecursiveIncludes() {
		// begin/end consumes input during tokenization, while dependency collection still follows a cycle.
		// The word rule after the back-reference must remain reachable when that cycle is stopped.
		final var source = IGrammarSource.fromString(ContentType.JSON, """
			{ "scopeName": "fixture.recursive",
			  "patterns": [ { "include": "#loop" } ],
			  "repository": { "loop": { "begin": "[(]", "end": "[)]", "patterns": [
			    { "include": "#loop" },
			    { "match": "\\\\w+", "name": "recursive.word" }
			  ] } } }
			""");
		final var registry = new Registry(new IRegistryOptions() {
			@Override
			public @Nullable IGrammarSource getGrammarSource(final String scopeName) {
				return "fixture.recursive".equals(scopeName) ? source : null;
			}
		});
		final var grammar = castNonNull(registry.loadGrammar("fixture.recursive"));
		final var tokens = grammar.tokenizeLine("((hello))").getTokens();
		// tokenizeLine preserves a separate token for each delimiter around the nested word.
		assertThat(tokens).hasSize(5);
		assertThat(tokens[2].getStartIndex()).isEqualTo(2);
		assertThat(tokens[2].getEndIndex()).isEqualTo(7);
		assertThat(tokens[2].getScopes()).containsExactly("fixture.recursive", "recursive.word");
	}

	@Test
	void testLoadingUnknownGrammar() {
		final var reg = new Registry();
		assertThat(reg.grammarForScopeName("undefined")).isNull();
		assertThat(reg.loadGrammar("undefined")).isNull();
	}
}
