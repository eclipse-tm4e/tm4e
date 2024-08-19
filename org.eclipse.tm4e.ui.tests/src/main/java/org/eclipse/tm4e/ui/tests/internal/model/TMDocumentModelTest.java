/**
 * Copyright (c) 2019 Red Hat Inc., and others
 * Copyright (c) 2023 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 * - Sebastian Thomschke (Vegard IT GmbH) - add test cases
 */
package org.eclipse.tm4e.ui.tests.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.text.Document;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.model.ModelTokensChangedEvent;
import org.eclipse.tm4e.core.model.Range;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.ui.internal.model.TMDocumentModel;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TMDocumentModelTest {

	private static final String LF = "\n";
	private static IGrammar grammar;

	@BeforeAll
	static void beforeAll() {
		final var grammarPath = "/grammars/TypeScript.tmLanguage.json";
		final var grammarSource = TMDocumentModelTest.class.getResource(grammarPath) != null
				? IGrammarSource.fromResource(TMDocumentModelTest.class, grammarPath)
				: IGrammarSource.fromFile(Path.of("." + grammarPath));
		grammar = new Registry().addGrammar(grammarSource);
	}

	private final Document doc = new Document();
	private final TMDocumentModel model = new TMDocumentModel(doc);

	private void assertDocHasLines(final Iterable<String> lines) {
		assertEquals(String.join(LF, lines), doc.get());
		assertEquals(model.getDocument().getNumberOfLines(), model.getNumberOfLines());
	}

	private void assertRange(final ModelTokensChangedEvent e, final int startLineNumber, final int endLineNumber) {
		assertEquals(List.of(new Range(startLineNumber, endLineNumber)), e.ranges);
	}

	private ModelTokensChangedEvent awaitModelChangedEvent(final TMDocumentModel model, final List<String> initialLines,
			final ThrowingRunnable action) throws Throwable {

		// prepare document
		{
			final var signal = new CountDownLatch(1);
			final ModelTokensChangedEvent.Listener listener = e -> signal.countDown();
			model.addModelTokensChangedListener(listener);
			model.getDocument().set(String.join(LF, initialLines));
			assertTrue(signal.await(2, TimeUnit.SECONDS));
			Thread.sleep(TestUtils.isCI() ? 500 : 50);
			model.removeModelTokensChangedListener(listener);
		}

		assertEquals(model.getDocument().getNumberOfLines(), model.getNumberOfLines());

		// test
		final var event = new AtomicReference<ModelTokensChangedEvent>();
		final var signal = new CountDownLatch(1);
		final ModelTokensChangedEvent.Listener listener = e -> {
			event.set(e);
			signal.countDown();
		};
		model.addModelTokensChangedListener(listener);
		action.run();
		assertTrue(signal.await(2, TimeUnit.SECONDS));
		model.removeModelTokensChangedListener(listener);
		return event.get();
	}

	@BeforeEach
	void setup() {
		model.setGrammar(grammar);
	}

	@AfterEach
	void tearDown() {
		model.dispose();
	}

	/**
	 * Tests appending a few new lines to the end of the document
	 */
	@Test
	void testAppend2Lines() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLength(), 0, LF + "//comment3" + LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.add("//comment3");
		expectedLines.add("");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 4);
	}

	/**
	 * Tests appending a new line char to the end of the document
	 */
	@Test
	void testAppendEmptyLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLength(), 0, LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.add("");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 3);
	}

	/**
	 * Tests appending a some chars to the end of the document
	 */
	@Test
	void testAppendText() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLength(), 0, "XY"));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//comment2XY");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	/**
	 * Tests appending a new text line to the end of the document
	 */
	@Test
	void testAppendTextLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLength(), 0, LF + "//comment3"));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.add("//comment3");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 3);
	}

	/**
	 * Tests appending a some chars to the end of a line
	 */
	@Test
	void testAppendTextToLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(initialLines.get(0).length(), 0, "X"));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(0, "//comment1X");
		assertDocHasLines(expectedLines);

		assertRange(event, 1, 1);
	}

	/**
	 * Tests inserting/pasting two line after the first line
	 */
	@Test
	void testInsert2Lines() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1), 0, "//commentX" + LF + "//commentY" + LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.add(1, "//commentX");
		expectedLines.add(2, "//commentY");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 3);
	}

	/**
	 * Tests inserting/pasting some text with new line chars that spawns two lines
	 */
	@Test
	void testInsert2LinesPartially() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1) + 2, 0, "X" + LF + "//Y"));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.add(1, "//X");
		expectedLines.set(2, "//Ycomment2");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 3);
	}

	/**
	 * Tests inserting a line break in the middle of a line
	 */
	@Test
	void testInsertEmptyLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1) + 4, 0, LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//co");
		expectedLines.add(2, "mment2");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 3);
	}

	/**
	 * Tests inserting '/*' at the beginning of the second line which invalidates all following lines
	 */
	@Test
	void testInsertMultiLineCommentStartToken() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3",
				"//comment4");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1), 0, "/*"));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "/*" + initialLines.get(1));
		assertDocHasLines(expectedLines);

		assertRange(event, 2, doc.getNumberOfLines());
	}

	/**
	 * Tests splitting a line
	 */
	@Test
	void testInsertNewLineCharInLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1) + 4, 0, LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//co");
		expectedLines.add(2, "mment2");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 3);
	}

	/**
	 * Tests inserting some chars into a line
	 */
	@Test
	void testInsertText() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1) + 2, 0, "XY"));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//XYcomment2");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	/**
	 * Tests inserting a line after the first line
	 */
	@Test
	void testInsertTextLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1), 0, "//commentX" + LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.add(1, "//commentX");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	/**
	 * Tests inserting a new line char at the beginning of the document.
	 */
	@Test
	void testPrefixEmptyLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(0, 0, LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.add(0, "");
		assertDocHasLines(expectedLines);

		assertRange(event, 1, 2); // second line is part of changed range as it's startState changed
	}

	/**
	 * Tests inserting a new line at the beginning of the document.
	 */
	@Test
	void testPrefixText() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(0, 0, "//"));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(0, "////comment1");
		assertDocHasLines(expectedLines);

		assertRange(event, 1, 1);
	}

	/**
	 * Tests inserting a new line at the beginning of the document.
	 */
	@Test
	void testPrefixTextLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(0, 0, "//comment0" + LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.add(0, "//comment0");
		assertDocHasLines(expectedLines);

		assertRange(event, 1, 2); // second line is part of changed range as it's startState changed
	}

	/**
	 * Tests removing the second and the third line
	 */
	@Test
	void testRemove2TextLines() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3",
				"//comment4");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1),
						2 * (initialLines.get(1).length() + LF.length()), // length to remove
						null));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.remove(2);
		expectedLines.remove(1);
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	@Test
	void testRemoveLastChar() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLength() - 1, 1, null));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//comment");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	@Test
	void testRemoveLastCharOfLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(initialLines.get(0).length() - 1, 1, null));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(0, "//comment");
		assertDocHasLines(expectedLines);

		assertRange(event, 1, 1);
	}

	/**
	 * Tests joining two lines
	 */
	@Test
	void testRemoveNewLineChar() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(
						doc.getLineOffset(2) - 1,
						LF.length(), // length to remove
						null));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, expectedLines.get(1) + expectedLines.get(2));
		expectedLines.remove(2);
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	/**
	 * Tests removing some chars of a line
	 */
	@Test
	void testRemoveText() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1) + 2, 2, null));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//mment2");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	/**
	 * Tests removing the second line
	 */
	@Test
	void testRemoveTextLine() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(
						doc.getLineOffset(1),
						initialLines.get(1).length() + LF.length(), // length to remove
						null));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.remove(1);
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	/**
	 * Tests replacing the second single comment line
	 */
	@Test
	void testReplace1Line() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3",
				"//comment4");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1),
						initialLines.get(1).length() + LF.length(), // length to replace
						"//commentX" + LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//commentX");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	/**
	 * Tests replacing the second single comment line with a multi-line comment which invalidates following lines
	 */
	@Test
	void testReplace1LineWithMultilineComment() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3",
				"//comment4");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1),
						initialLines.get(1).length() + LF.length(), // length to replace
						"/*commentX" + LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "/*commentX");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 4);
	}

	@Test
	void testReplace2LinesPartially() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3",
				"//comment4",
				"//comment5");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1) + "//comm".length(),
						("XXX" + LF + "//YYY").length(),
						"XXX" + LF + "//YYY"));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//commXXX");
		expectedLines.set(2, "//YYYmment3");
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 3);
	}

	/**
	 * Tests replacing the second and third single comment lines with a one line
	 */
	@Test
	void testReplace2LinesWith1Line() throws Throwable {
		final var initialLines = List.of(
				"//comment1",
				"//comment2",
				"//comment3",
				"//comment4",
				"//comment5");

		final var event = awaitModelChangedEvent(model, initialLines,
				() -> doc.replace(doc.getLineOffset(1),
						2 * (initialLines.get(1).length() + LF.length()), // length to replace
						"//commentX" + LF));

		final var expectedLines = new ArrayList<>(initialLines);
		expectedLines.set(1, "//commentX");
		expectedLines.remove(2);
		assertDocHasLines(expectedLines);

		assertRange(event, 2, 2);
	}

	@Test
	void testSetDocumentContent() throws Throwable {
		final var event1 = awaitModelChangedEvent(model, Collections.emptyList(), () -> doc.set("a"));
		assertRange(event1, 1, doc.getNumberOfLines());
		assertRange(event1, 1, 1);

		final var event2 = awaitModelChangedEvent(model, Collections.emptyList(), () -> doc.set("a\nb"));
		assertRange(event2, 1, doc.getNumberOfLines());
		assertRange(event2, 1, 2);

		final var event3 = awaitModelChangedEvent(model, Collections.emptyList(), () -> doc.set("a\nb\nc"));
		assertRange(event3, 1, doc.getNumberOfLines());
		assertRange(event3, 1, 3);
	}
}
