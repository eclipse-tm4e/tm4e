/**
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.assertj.core.api.Assertions.*;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationCharacterPairMatcher;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests TextMate-aware bracket matching across a large document.
 */
public class TestBracketPairMatching {

	private final LanguageConfigurationCharacterPairMatcher matcher = new LanguageConfigurationCharacterPairMatcher();

	@AfterEach
	public void tearDown() throws Exception {
		matcher.dispose();
		TestUtils.closeEditor(UI.getActivePage().getActiveEditor());
		TestUtils.assertNoTM4EThreadsRunning();
	}

	/**
	 * Verifies that the root braces of a large JSON document match each other while brackets inside strings are skipped.
	 */
	@Test
	public void testFarAwayBracketsInLargeJson() throws Exception {
		final var source = new StringBuilder("{\n  \"items\": [\n");
		for (int i = 0; i < 2_000; i++) {
			source.append("""
					    {
					      "id": %d,
					      "name": "item-%d ]}",
					      "tags": [
					        "a",
					        "b"
					      ],
					      "meta": {
					        "x": %d
					      }
					    }%s
					""".formatted(i, i, i, i < 1_999 ? "," : ""));
		}
		source.append("  ]\n}\n");

		final IDocument document = openJson(source.toString());
		final var model = TMModelManager.INSTANCE.getConnectedModel(document);
		TestUtils.waitForAndAssertCondition(10_000, () -> model.getLineTokens(document.getNumberOfLines() - 2) != null);

		final String text = document.get();
		final int rootOpen = text.indexOf('{');
		final int rootClose = text.lastIndexOf('}');

		final IRegion backward = matcher.match(document, rootClose + 1);
		final IRegion forward = matcher.match(document, rootOpen + 1);

		assertThat(forward).isNotNull();
		assertThat(forward.getOffset()).isEqualTo(rootOpen);
		assertThat(forward.getOffset() + forward.getLength() - 1).isEqualTo(rootClose);
		assertThat(backward).isNotNull();
		assertThat(backward.getOffset()).isEqualTo(rootOpen);
		assertThat(backward.getOffset() + backward.getLength() - 1).isEqualTo(rootClose);

		// moving "}" out of the "item-0 ]}" string closes item 0 early, so the root brace pairs with its original "}"
		final int stringEnd = text.indexOf("]}\"", text.indexOf("item-0 "));
		TestUtils.waitForIdleAfterChange(document, 10_000, () -> document.replace(stringEnd, 3, "]\"}"));
		final IRegion afterEdit = matcher.match(document, rootOpen + 1);
		assertThat(afterEdit).isNotNull();
		assertThat(afterEdit.getOffset() + afterEdit.getLength() - 1).isEqualTo(document.get().indexOf("\n    }") + 5);
	}

	/**
	 * Verifies that brackets match across a single line longer than the scan chunk, in both directions.
	 */
	@Test
	public void testBracketsOnLongLine() throws Exception {
		final var source = new StringBuilder("[");
		for (int i = 0; i < 30_000; i++) {
			source.append("[1,2],");
		}
		source.append("[3]]\n");

		final IDocument document = openJson(source.toString());
		final int rootClose = source.lastIndexOf("]");

		final IRegion forward = matcher.match(document, 1);
		assertThat(forward).isNotNull();
		assertThat(forward.getOffset()).isZero();
		assertThat(forward.getLength()).isEqualTo(rootClose + 1);

		final IRegion backward = matcher.match(document, rootClose + 1);
		assertThat(backward).isNotNull();
		assertThat(backward.getOffset()).isZero();
		assertThat(backward.getLength()).isEqualTo(rootClose + 1);
	}

	private static IDocument openJson(final String content) throws Exception {
		final var tempFile = TestUtils.createTempFile(".json");
		try (var out = new FileOutputStream(tempFile)) {
			out.write(content.getBytes(StandardCharsets.UTF_8));
		}
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), tempFile.toURI(),
				TestUtils.assertHasGenericEditor().getId(), true);
		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TestUtils.waitForModelReady(document, 10_000);
		return document;
	}
}
