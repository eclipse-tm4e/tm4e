/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
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
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import static org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.tm4e.languageconfiguration.internal.model.CursorConfiguration;
import org.junit.jupiter.api.Test;

class TextUtilsTest {

	private static MockDocument newDoc(final String txt) {
		return new MockDocument("", txt);
	}

	@Test
	void testIsEnter() {
		assertTrue(isEnter(newDoc(""), new DocumentCommand() {
			{
				text = "\n";
			}
		}));
		assertTrue(isEnter(newDoc(""), new DocumentCommand() {
			{
				text = "\r\n";
			}
		}));
		assertFalse(isEnter(newDoc(""), new DocumentCommand() {
			{
				text = "word";
			}
		}));
	}

	@Test
	void testGetIndentationFromWhitespace() {
		assertEquals("\t\t", getIndentationFromWhitespace("\t\t", new CursorConfiguration(false, 0)));
		assertEquals("\t\t", getIndentationFromWhitespace("\t\t", new CursorConfiguration(true, 2)));
		assertEquals("\t\t", getIndentationFromWhitespace("\t\t ", new CursorConfiguration(true, 2)));
		assertEquals("\t\t  ", getIndentationFromWhitespace("\t\t  ", new CursorConfiguration(true, 2)));
		assertEquals("\t\t  ", getIndentationFromWhitespace("\t\t   ", new CursorConfiguration(true, 2)));
		assertEquals("", getIndentationFromWhitespace(" \t ", new CursorConfiguration(true, 2)));
	}

	@Test
	void testGetLeadingWhitespace() throws Exception {
		final var indent = "\t \t ";
		assertEquals(indent, getLeadingWhitespace(newDoc(indent + "hello"), 0));
		assertEquals(indent, getLeadingWhitespace(newDoc("hello\n" + indent + "world"), 1));
		assertEquals(indent, getIndentationAtPosition(newDoc(indent + "hello"), indent.length() + 2));
	}

	@Test
	void testIndentationAtPosition() throws Exception {
		final var indent = "\t \t ";
		assertEquals("", getIndentationAtPosition(newDoc(indent + "hello"), 0));
		assertEquals("\t", getIndentationAtPosition(newDoc(indent + "hello"), 1));
		assertEquals("\t ", getIndentationAtPosition(newDoc(indent + "hello"), 2));
	}

	@Test
	void testIsBlankLine() {
		assertTrue(isBlankLine(newDoc(""), 0));
		assertTrue(isBlankLine(newDoc(" "), 0));
		assertTrue(isBlankLine(newDoc(" \t"), 0));
		assertTrue(isBlankLine(newDoc("\n\n"), 1));
		assertTrue(isBlankLine(newDoc("\n \n"), 1));
		assertTrue(isBlankLine(newDoc("\n \t\n"), 1));

		assertFalse(isBlankLine(newDoc("word"), 0));
		assertFalse(isBlankLine(newDoc("\nword\n"), 1));
	}

	@Test
	void testIsEmptyLine() {
		assertTrue(isEmptyLine(newDoc(""), 0));
		assertFalse(isEmptyLine(newDoc(" "), 0));
		assertFalse(isEmptyLine(newDoc(" \t"), 0));
		assertTrue(isEmptyLine(newDoc("\n\n"), 1));
		assertFalse(isEmptyLine(newDoc("\n \n"), 1));
		assertFalse(isEmptyLine(newDoc("\n \t\n"), 1));

		assertFalse(isEmptyLine(newDoc("word"), 0));
		assertFalse(isEmptyLine(newDoc("\nword\n"), 1));
	}

	@Test
	void testReplaceIndent() {
		assertEquals("  ", replaceIndent("\t\t", 2, "  ").toString());

		assertEquals("", replaceIndent("\t\t", 2, "").toString().toString());
		assertEquals("foo  ", replaceIndent("foo  ", 2, "").toString());
		assertEquals("foo", replaceIndent(" \t foo", 2, "").toString());
		assertEquals("foo\nbar", replaceIndent(" foo\n bar", 2, "").toString());
		assertEquals("foo\nbar", replaceIndent("  foo\n\tbar", 2, "").toString());
		assertEquals("foo\nbar", replaceIndent(" foo\n\tbar", 2, "").toString());
		assertEquals("foo\n\tbar", replaceIndent("\tfoo\n\t\tbar", 2, "").toString());
		assertEquals("foo\n\tbar", replaceIndent("\tfoo\n  \tbar", 2, "").toString());

		assertEquals("  ", replaceIndent("\t\t", 2, "  ").toString());
		assertEquals("  foo  ", replaceIndent("foo  ", 2, "  ").toString());
		assertEquals("  foo", replaceIndent(" \t foo", 2, "  ").toString());
		assertEquals("  foo\n  bar", replaceIndent(" foo\n bar", 2, "  ").toString());
		assertEquals("  foo\n  bar", replaceIndent("  foo\n\tbar", 2, "  ").toString());
		assertEquals("  foo\n  bar", replaceIndent(" foo\n\tbar", 2, "  ").toString());
		assertEquals("  foo\n  \tbar", replaceIndent("\tfoo\n\t\tbar", 2, "  ").toString());
		assertEquals("  foo\n  \tbar", replaceIndent("\tfoo\n  \tbar", 2, "  ").toString());

		assertEquals("  \n  \n", replaceIndent("\n\n", 2, "  ").toString());
		assertEquals("  foo\n  bar\n", replaceIndent("\tfoo\n\tbar\n", 2, "  ").toString());

	}
}
