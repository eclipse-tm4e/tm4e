/**
 * Copyright (c) 2026 Sergii Zolotarov and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.assertj.core.api.Assertions.*;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.tm4e.ui.text.TMPartitions;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.Test;

class WordPatternTest {

	private static IContentType contentType(final String id) {
		return castNonNull(Platform.getContentTypeManager().getContentType(id));
	}

	@Test
	void typeScriptWordContainsDollarSign() {
		final Pattern pattern = castNonNull(
				LanguageConfigurationPlugin.getWordPattern(contentType("org.eclipse.tm4e.language_pack.typescript")));
		assertThat(pattern.matcher("$el").matches()).isTrue();
		assertThat(pattern.matcher("a.b").matches()).isFalse();
	}

	@Test
	void markdownWordUsesUnicodeProperties() {
		// the markdown word pattern uses \p{Alphabetic}, \p{Number} and \p{Nonspacing_Mark} and the flags "ug"
		final Pattern pattern = castNonNull(
				LanguageConfigurationPlugin.getWordPattern(contentType("org.eclipse.tm4e.language_pack.markdown")));
		assertThat(pattern.matcher("héllo").matches()).isTrue();
		assertThat(pattern.matcher("été").matches()).isTrue();
		assertThat(pattern.matcher("a b").matches()).isFalse();
	}

	@Test
	void languageWithoutWordPattern() {
		assertThat(LanguageConfigurationPlugin.getWordPattern(contentType("org.eclipse.tm4e.language_pack.rust"))).isNull();
		assertThat(LanguageConfigurationPlugin.getWordPattern(contentType("org.eclipse.core.runtime.text"))).isNull();
	}

	@Test
	void allWordPatternsCanBeCompiled() {
		var count = 0;
		for (final var definition : LanguageConfigurationRegistryManager.getInstance().getDefinitions()) {
			final var configuration = definition.getLanguageConfiguration();
			if (configuration != null && configuration.getWordPattern() != null) {
				assertThat(configuration.getWordRegExp())
						.describedAs("word pattern of %s", definition.getContentType().getId())
						.isNotNull();
				count++;
			}
		}
		assertThat(count).isPositive();
	}

	@Test
	void wordPatternOfDocument() throws Exception {
		final var project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);
		try {
			final var file = project.getFile("test.ts");
			file.create(new ByteArrayInputStream("const $el = 1;".getBytes(StandardCharsets.UTF_8)), true, null);
			final var manager = FileBuffers.getTextFileBufferManager();
			manager.connect(file.getFullPath(), LocationKind.IFILE, null);
			try {
				final var document = castNonNull(manager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE)).getDocument();
				final Pattern pattern = castNonNull(LanguageConfigurationPlugin.getWordPattern(document));
				assertThat(pattern.matcher("$el").matches()).isTrue();
			} finally {
				manager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
			}
		} finally {
			project.delete(true, null);
		}
	}

	@Test
	void typeScriptWordsAreSeparatedByUnicodeSpaces() {
		final Pattern pattern = castNonNull(
				LanguageConfigurationPlugin.getWordPattern(contentType("org.eclipse.tm4e.language_pack.typescript")));
		assertThat(words(pattern, "foo\u00A0bar\u3000$el")).containsExactly("foo", "bar", "$el");
	}

	@Test
	void markdownLatexWordWithoutEmphasis() {
		// the word pattern ends with a back-reference to an optional group
		final Pattern pattern = castNonNull(LanguageConfigurationPlugin
				.getWordPattern(contentType("org.eclipse.tm4e.language_pack.markdown_latex_combined")));
		assertThat(words(pattern, "hello *bold*")).containsExactly("hello", "*bold*");
	}

	@Test
	void wordPatternIsCached() {
		final var typeScript = contentType("org.eclipse.tm4e.language_pack.typescript");
		assertThat(LanguageConfigurationPlugin.getWordPattern(typeScript))
				.isSameAs(LanguageConfigurationPlugin.getWordPattern(typeScript));
	}

	@Test
	void wordPatternOfEmbeddedLanguage() throws Exception {
		final var project = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		project.create(null);
		project.open(null);
		try {
			final IFile file = project.getFile("embedded.html");
			final String html = """
				<html>
				<body>
				  <p>html-text</p>
				  <script>
				    let js-text;
				  </script>
				</body>
				</html>""";
			file.create(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), true, null);
			final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
			final IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			TestUtils.waitForModelReady(doc, 5_000);
			TestUtils.waitForAndAssertCondition(5_000, () -> TMPartitions.hasPartitioning(doc));

			// in HTML, a word can contain "-", in JavaScript it cannot
			final Pattern htmlPattern = castNonNull(LanguageConfigurationPlugin.getWordPattern(doc, html.indexOf("html-text")));
			assertThat(words(htmlPattern, "html-text")).containsExactly("html-text");
			final Pattern jsPattern = castNonNull(LanguageConfigurationPlugin.getWordPattern(doc, html.indexOf("js-text")));
			assertThat(words(jsPattern, "js-text")).containsExactly("js", "text");
			assertThat(LanguageConfigurationPlugin.getWordPattern(doc)).isSameAs(htmlPattern);
		} finally {
			UI.getActivePage().closeAllEditors(false);
			project.delete(true, true, null);
		}
	}

	private static List<String> words(final Pattern pattern, final String text) {
		final var matcher = pattern.matcher(text);
		final var words = new ArrayList<String>();
		while (matcher.find()) {
			words.add(matcher.group());
		}
		return words;
	}
}
