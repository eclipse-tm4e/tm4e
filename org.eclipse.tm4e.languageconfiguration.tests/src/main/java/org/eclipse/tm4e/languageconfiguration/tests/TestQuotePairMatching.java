/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
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
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.assertj.core.api.Assertions.*;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationCharacterPairMatcher;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for quote pair matching based on TextMate scopes and language configuration.
 *
 * These tests exercise LanguageConfigurationCharacterPairMatcher directly on a Java file, using
 * the TM model and grammar provided by the language pack.
 */
public class TestQuotePairMatching {

	@AfterEach
	public void tearDown() throws Exception {
		TestUtils.closeEditor(UI.getActivePage().getActiveEditor());
		TestUtils.assertNoTM4EThreadsRunning();
	}

	/**
	 * Verifies that a single string literal with escaped quotes is matched correctly from both sides
	 * and that an escaped inner quote does not form its own surrounding pair.
	 */
	@Test
	public void testDoubleQuoteMatchingInString() throws Exception {
		final IEditorDescriptor genericEditorDescr = TestUtils.assertHasGenericEditor();

		final var tempFile = TestUtils.createTempFile(".java");
		final String source = """
			class X {
			  String s = "file \\\"test.txt\\\" not found";
			}
			""";
		try (var out = new FileOutputStream(tempFile)) {
			out.write(source.getBytes(StandardCharsets.UTF_8));
		}

		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), tempFile.toURI(), genericEditorDescr.getId(), true);
		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		// ensure TM model and grammar are ready for this document
		TestUtils.waitForModelReady(document, 10_000);

		final var matcher = new LanguageConfigurationCharacterPairMatcher();
		final String text = document.get();

		final int openingQuote = text.indexOf('"');
		final int closingQuote = text.lastIndexOf('"');
		final int innerQuote = text.indexOf("\\\"") + 1; // position of inner "

		// caret after opening quote
		final IRegion regionAfterOpening = matcher.match(document, openingQuote + 1);
		assertThat(regionAfterOpening).isNotNull();
		assertThat(regionAfterOpening.getOffset()).isEqualTo(openingQuote);
		assertThat(regionAfterOpening.getOffset() + regionAfterOpening.getLength() - 1).isEqualTo(closingQuote);

		// caret after closing quote
		final IRegion regionAfterClosing = matcher.match(document, closingQuote + 1);
		assertThat(regionAfterClosing).isNotNull();
		assertThat(regionAfterClosing.getOffset()).isEqualTo(openingQuote);
		assertThat(regionAfterClosing.getOffset() + regionAfterClosing.getLength() - 1).isEqualTo(closingQuote);

		// caret after inner escaped quote should not match a pair
		final IRegion regionAfterInner = matcher.match(document, innerQuote + 1);
		assertThat(regionAfterInner).isNull();
	}

	/**
	 * Verifies that when two string literals appear on the same line, placing the caret after the
	 * closing quote of the first string highlights the corresponding opening quote of that string
	 * and does not treat this closing quote as the opening quote of the following string.
	 */
	@Test
	public void testQuoteMatchingAtEndOfFirstStringOnLine() throws Exception {
		final IEditorDescriptor genericEditorDescr = TestUtils.assertHasGenericEditor();

		final var tempFile = TestUtils.createTempFile(".java");
		final String source = """
			class X {
			  String s1 = "foo"; String s2 = "bar";
			}
			""";
		try (var out = new FileOutputStream(tempFile)) {
			out.write(source.getBytes(StandardCharsets.UTF_8));
		}

		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), tempFile.toURI(), genericEditorDescr.getId(), true);
		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		TestUtils.waitForModelReady(document, 10_000);

		final var matcher = new LanguageConfigurationCharacterPairMatcher();
		final String text = document.get();

		final int firstOpening = text.indexOf('"');
		final int firstClosing = text.indexOf('"', firstOpening + 1);
		final int secondOpening = text.indexOf('"', firstClosing + 1);
		final int secondClosing = text.indexOf('"', secondOpening + 1);

		// caret after closing quote of the first string
		final IRegion region = matcher.match(document, firstClosing + 1);
		assertThat(region).isNotNull();
		assertThat(region.getOffset()).isEqualTo(firstOpening);
		assertThat(region.getOffset() + region.getLength() - 1).isEqualTo(firstClosing);

		// sanity-check second string is not included
		assertThat(region.getOffset()).isNotEqualTo(secondOpening);
		assertThat(region.getOffset() + region.getLength() - 1).isNotEqualTo(secondClosing);
	}

	/**
	 * Verifies that a closing parenthesis inside a string literal (e.g. {@code "1) Welcome"}) is
	 * not treated as the structural peer for the surrounding call's opening parenthesis, while the
	 * real call-closing parenthesis still matches correctly.
	 */
	@Test
	public void testParenInsideStringIsNotMatchedAsCallClosingParen() throws Exception {
		final IEditorDescriptor genericEditorDescr = TestUtils.assertHasGenericEditor();

		final var tempFile = TestUtils.createTempFile(".java");
		final String source = """
			class X {
			  void log() {
			    System.out.println("1) Welcome");
			  }
			}
			""";
		try (var out = new FileOutputStream(tempFile)) {
			out.write(source.getBytes(StandardCharsets.UTF_8));
		}

		final ITextEditor editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), tempFile.toURI(),
				genericEditorDescr.getId(), true);
		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		TestUtils.waitForModelReady(document, 10_000);

		final var matcher = new LanguageConfigurationCharacterPairMatcher();
		final String text = document.get();

		final int printlnIndex = text.indexOf("System.out.println");
		final int callOpeningParen = text.indexOf('(', printlnIndex);
		final int innerParenInString = text.indexOf("1) Welcome");
		final int innerClosingParen = text.indexOf(')', innerParenInString);
		final int callClosingParen = text.indexOf(");", innerClosingParen);

		// caret after inner ')' inside the string literal should NOT match the call opening '('
		final IRegion innerRegion = matcher.match(document, innerClosingParen + 1);
		assertThat(innerRegion).isNull();

		// caret after the real call-closing ')' should, if matching is enabled for this language,
		// match the call opening '(' rather than the inner one
		final IRegion callRegion = matcher.match(document, callClosingParen + 1);
		if (callRegion != null) {
			assertThat(callRegion.getOffset()).isEqualTo(callOpeningParen);
			assertThat(callRegion.getOffset() + callRegion.getLength() - 1).isEqualTo(callClosingParen);
		}
	}

	/**
	 * Verifies that for calls like {@code System.out.println("Hello  World" /*)&#42;/);} the call parentheses
	 * are preferred over the string quotes when the caret is placed after the opening parenthesis, i.e. the
	 * matching pair is {@code println( ... )} and not the surrounding string quotes.
	 */
	@Test
	public void testCallParenPreferredOverQuotesWithCommentedParen() throws Exception {
		final IEditorDescriptor genericEditorDescr = TestUtils.assertHasGenericEditor();

		final var tempFile = TestUtils.createTempFile(".java");
		final String source = """
			class X {
			  void log() {
			    System.out.println("Hello  World" /*)*/);
			  }
			}
			""";
		try (var out = new FileOutputStream(tempFile)) {
			out.write(source.getBytes(StandardCharsets.UTF_8));
		}

		final ITextEditor editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), tempFile.toURI(),
				genericEditorDescr.getId(), true);
		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		TestUtils.waitForModelReady(document, 10_000);

		final var matcher = new LanguageConfigurationCharacterPairMatcher();
		final String text = document.get();

		final int printlnIndex = text.indexOf("System.out.println");
		final int callOpeningParen = text.indexOf('(', printlnIndex);
		final int callClosingParen = text.lastIndexOf(')');
		final int closingQuote = text.indexOf('"', text.indexOf("Hello  World") + "Hello  World".length());

		// caret after opening '(' should match the real call-closing ')', not the closing quote
		final IRegion regionAtOpening = matcher.match(document, callOpeningParen + 1);
		assertThat(regionAtOpening).isNotNull();
		final int regionStart = regionAtOpening.getOffset();
		final int regionEnd = regionAtOpening.getOffset() + regionAtOpening.getLength() - 1;

		assertThat(document.getChar(regionStart)).isEqualTo('(');
		assertThat(document.getChar(regionEnd)).isEqualTo(')');
		assertThat(regionEnd).isEqualTo(callClosingParen);
		assertThat(regionEnd).isNotEqualTo(closingQuote);
	}

	/**
	 * Verifies that in a TypeScript call like
	 * {@code res.end('Hello World' /*)&#42;/);} the closing parenthesis inside the block comment
	 * is not used as the structural peer for the call's opening parenthesis, and that the real
	 * call-closing parenthesis before the semicolon is the one that is paired with {@code res.end(}.
	 */
	@Test
	public void testTsResEndParenWithCommentedParen() throws Exception {
		final IEditorDescriptor genericEditorDescr = TestUtils.assertHasGenericEditor();

		final var tempFile = TestUtils.createTempFile(".ts");
		final String source = """
			import * as http from 'http';

			const server = http.createServer((req: any, res: any) => {
			  res.statusCode = 200;
			  res.setHeader('Content-Type', 'text/plain');
			  res.end('Hello World');
			  res.end('Hello World' /*)*/);
			  res.end('Hello :) World');
			});
			""";
		try (var out = new FileOutputStream(tempFile)) {
			out.write(source.getBytes(StandardCharsets.UTF_8));
		}

		final ITextEditor editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), tempFile.toURI(),
				genericEditorDescr.getId(), true);
		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		TestUtils.waitForModelReady(document, 10_000);

		final var matcher = new LanguageConfigurationCharacterPairMatcher();
		final String text = document.get();

		final int resEndIndex = text.indexOf("res.end('Hello World' /*)*/);");
		assertThat(resEndIndex).isGreaterThanOrEqualTo(0);

		final int callOpeningParen = text.indexOf('(', resEndIndex);
		final int callClosingParen = text.indexOf(");", resEndIndex);
		final int commentStart = text.indexOf("/*)*/", resEndIndex);
		final int innerCommentParen = text.indexOf(')', commentStart);

		// caret after the commented ')' must NOT match the res.end '('
		final IRegion regionAtCommentParen = matcher.match(document, innerCommentParen + 1);
		assertThat(regionAtCommentParen)
				.as("commented ')' must not be treated as structural closing paren")
				.isNull();

		// caret after the real call-closing ')' should match the res.end '('
		final IRegion regionAtCallClosing = matcher.match(document, callClosingParen + 1);
		if (regionAtCallClosing != null) {
			assertThat(regionAtCallClosing.getOffset()).isEqualTo(callOpeningParen);
			assertThat(regionAtCallClosing.getOffset() + regionAtCallClosing.getLength() - 1)
					.isEqualTo(callClosingParen);
		}
	}
}
