/**
 * Copyright (c) 2026 Vegard IT GmbH and others.
 * This program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0/
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
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.GrammarDefinition;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.ui.IImportWizard;
import org.junit.jupiter.api.Test;

/** Verifies wizard creation, repeated and conflicting imports, and recovery from missing grammar files. */
class GrammarImportLifecycleTest {

	@Test
	void standaloneImportWizardCanBeCreatedFromItsExtension() throws Exception {
		createImportWizard().dispose();
	}

	@Test
	void reimportingAFileWithAChangedScopeShowsAWizardError() throws Exception {
		final var file = createGrammar();
		final var registry = TMEclipseRegistryPlugin.getGrammarRegistryManager();
		final var original = new GrammarDefinition("source.test", file.toString());
		try {
			final var session = registry.newEditSession();
			session.registerGrammarDefinition(original);
			session.save();
			Files.writeString(file, "{\"scopeName\":\"source.changed\",\"patterns\":[]}");
			final var wizard = createImportWizard();
			final var shell = new Shell();
			try {
				wizard.addPages();
				final var page = wizard.getPages()[0];
				page.createControl(shell);
				// Set the private input to exercise real page validation without adding a test-only wizard API.
				final var fileInput = page.getClass().getDeclaredField("grammarFileText");
				fileInput.setAccessible(true);
				((Text) fileInput.get(page)).setText(file.toString());
				assertThat(page.isPageComplete()).isTrue();
				assertThat(wizard.performFinish()).isFalse();
				assertThat(page.getErrorMessage()).contains("different scope", "Remove the old import first");
				assertThat(registry.getDefinitions()).filteredOn(def -> def.getURI().equals(file.toUri())).containsExactly(original);
			} finally {
				wizard.dispose();
				shell.dispose();
			}
		} finally {
			final var cleanup = registry.newEditSession();
			cleanup.unregisterGrammarDefinition(original);
			cleanup.save();
			Files.deleteIfExists(file);
		}
	}

	@Test
	void reimportingTheSameFileDoesNotAddAnotherDefinition() throws Exception {
		final var file = createGrammar();
		try {
			final var session = TMEclipseRegistryPlugin.getGrammarRegistryManager().newEditSession();
			final var scope = "source." + UUID.randomUUID();
			final var original = new GrammarDefinition(scope, file.toString());
			session.registerGrammarDefinition(original);
			session.registerGrammarDefinition(new GrammarDefinition(scope, file.toString()));
			assertThat(session.getDefinitions()).filteredOn(def -> def.getURI().equals(file.toUri())).containsExactly(original);
		} finally {
			Files.deleteIfExists(file);
		}
	}

	@Test
	void deletingALoadedFileStopsReturningItsCachedGrammarAndRestoringItRecovers() throws Exception {
		final var file = createGrammar();
		final var originalTimestamp = Files.getLastModifiedTime(file);
		try {
			final var session = TMEclipseRegistryPlugin.getGrammarRegistryManager().newEditSession();
			final var definition = new GrammarDefinition("source." + UUID.randomUUID(), file.toString());
			session.registerGrammarDefinition(definition);
			assertThat(session.getGrammarForScope(definition.getScope())).isNotNull();
			Files.delete(file);
			// Exercise the real timestamp check after its throttle, rather than clearing the cache in the test.
			Thread.sleep(5100);
			assertThatThrownBy(() -> session.getGrammarForScope(definition.getScope())).isInstanceOf(TMException.class);
			Files.writeString(file, "{\"scopeName\":\"source.test\",\"name\":\"restored\",\"patterns\":[]}");
			// Restoring a backup may keep its old timestamp. A failed load must still force a fresh read.
			Files.setLastModifiedTime(file, originalTimestamp);
			assertThat(session.getGrammarForScope(definition.getScope())).isNotNull().extracting(IGrammar::getName).isEqualTo("restored");
		} finally {
			Files.deleteIfExists(file);
		}
	}

	@Test
	void restoringALoadedDependencyWithItsOldTimestampUsesItsNewRules() throws Exception {
		final var rootFile = createGrammar();
		final var dependencyFile = createGrammar();
		try {
			final var scope = "source." + UUID.randomUUID();
			final var dependencyScope = scope + ".dependency";
			Files.writeString(rootFile, "{\"scopeName\":\"" + scope + "\",\"patterns\":[{\"include\":\"" + dependencyScope + "\"}]}");
			Files.writeString(dependencyFile, "{\"scopeName\":\"" + dependencyScope
					+ "\",\"patterns\":[{\"match\":\"x\",\"name\":\"keyword.original\"}]}");
			final var originalTimestamp = Files.getLastModifiedTime(dependencyFile);
			final var session = TMEclipseRegistryPlugin.getGrammarRegistryManager().newEditSession();
			final var root = new GrammarDefinition(scope, rootFile.toString());
			session.registerGrammarDefinition(root);
			session.registerGrammarDefinition(new GrammarDefinition(dependencyScope, dependencyFile.toString()));
			assertThat(session.getGrammarForScope(root.getScope())).isNotNull()
					.extracting(grammar -> grammar.tokenizeLine("x").getTokens()[0].getScopes())
					.isEqualTo(List.of(scope, "keyword.original"));

			Files.delete(dependencyFile);
			// A root reload checks its dependencies again. Wait for the real timestamp throttle without clearing their caches.
			Files.setLastModifiedTime(rootFile, FileTime.fromMillis(Files.getLastModifiedTime(rootFile).toMillis() + 60_000));
			Thread.sleep(5100);
			assertThatThrownBy(() -> session.getGrammarForScope(root.getScope())).isInstanceOf(TMException.class);
			Files.writeString(dependencyFile, "{\"scopeName\":\"" + dependencyScope
					+ "\",\"patterns\":[{\"match\":\"x\",\"name\":\"keyword.restored\"}]}");
			// No direct dependency lookup: recovery must work through the root even when a backup restores the old timestamp.
			Files.setLastModifiedTime(dependencyFile, originalTimestamp);
			assertThat(session.getGrammarForScope(root.getScope())).isNotNull()
					.extracting(grammar -> grammar.tokenizeLine("x").getTokens()[0].getScopes())
					.isEqualTo(List.of(scope, "keyword.restored"));
		} finally {
			Files.deleteIfExists(rootFile);
			Files.deleteIfExists(dependencyFile);
		}
	}

	@Test
	void missingImportDoesNotAbortExtensionFallback() throws Exception {
		final var missing = createGrammar();
		final var valid = createGrammar();
		try {
			final var session = TMEclipseRegistryPlugin.getGrammarRegistryManager().newEditSession();
			final var extension = "tm4etest" + UUID.randomUUID().toString().replace("-", "");
			Files.writeString(valid, "{\"scopeName\":\"source.valid\",\"fileTypes\":[\"" + extension + "\"],\"patterns\":[]}");
			Files.delete(missing);
			session.registerGrammarDefinition(new GrammarDefinition("source." + UUID.randomUUID(), missing.toString()));
			// Contributed definitions follow all imports, independent of hash-map iteration order.
			// Only the contribution identity changes; the fixture still reads its temporary file from disk.
			session.registerGrammarDefinition(new GrammarDefinition("source." + UUID.randomUUID(), valid.toString()) {
				@Override
				public String getPluginId() {
					return "org.eclipse.tm4e.ui.tests";
				}
			});
			assertThat(session.getGrammarForFileExtension(extension)).isNotNull();
		} finally {
			Files.deleteIfExists(missing);
			Files.deleteIfExists(valid);
		}
	}

	private static IImportWizard createImportWizard() throws Exception {
		for (final var element : Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.importWizards")) {
			if ("org.eclipse.tm4e.ui.wizards.TextMateGrammarWizard".equals(element.getAttribute("id"))) {
				return (IImportWizard) element.createExecutableExtension("class");
			}
		}
		throw new AssertionError("Grammar import wizard is not registered");
	}

	private static Path createGrammar() throws Exception {
		final var file = Files.createTempFile("tm4e-import-lifecycle-", ".json");
		Files.writeString(file, "{\"scopeName\":\"source.test\",\"patterns\":[]}");
		return file;
	}
}
