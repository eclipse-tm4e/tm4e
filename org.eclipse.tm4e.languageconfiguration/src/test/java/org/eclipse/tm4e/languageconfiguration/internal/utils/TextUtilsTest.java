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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils.*;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.tm4e.languageconfiguration.internal.model.CursorConfiguration;
import org.junit.jupiter.api.Test;

class TextUtilsTest {

	private static MockDocument newDoc(final String txt) {
		return new MockDocument("", txt);
	}

	@Test
	void testIsEnter() {
		assertThat(isEnter(newDoc(""), new DocumentCommand() {
			{
				text = "\n";
			}
		})).isTrue();
		assertThat(isEnter(newDoc(""), new DocumentCommand() {
			{
				text = "\r\n";
			}
		})).isTrue();
		assertThat(isEnter(newDoc(""), new DocumentCommand() {
			{
				text = "word";
			}
		})).isFalse();
	}

	@Test
	void testGetIndentationFromWhitespace() {
		assertThat(getIndentationFromWhitespace("\t\t", new CursorConfiguration(false, 0))).isEqualTo("\t\t");
		assertThat(getIndentationFromWhitespace("\t\t", new CursorConfiguration(true, 2))).isEqualTo("\t\t");
		assertThat(getIndentationFromWhitespace("\t\t ", new CursorConfiguration(true, 2))).isEqualTo("\t\t");
		assertThat(getIndentationFromWhitespace("\t\t  ", new CursorConfiguration(true, 2))).isEqualTo("\t\t  ");
		assertThat(getIndentationFromWhitespace("\t\t   ", new CursorConfiguration(true, 2))).isEqualTo("\t\t  ");
		assertThat(getIndentationFromWhitespace(" \t ", new CursorConfiguration(true, 2))).isEqualTo("");
	}

	@Test
	void testGetLeadingWhitespace() throws Exception {
		final var indent = "\t \t ";
		assertThat(getLeadingWhitespace(newDoc(indent + "hello"), 0)).isEqualTo(indent);
		assertThat(getLeadingWhitespace(newDoc("hello\n" + indent + "world"), 1)).isEqualTo(indent);
		assertThat(getIndentationAtPosition(newDoc(indent + "hello"), indent.length() + 2)).isEqualTo(indent);
	}

	@Test
	void testIndentationAtPosition() {
		final var indent = "\t \t ";
		assertThat(getIndentationAtPosition(newDoc(indent + "hello"), 0)).isEqualTo("");
		assertThat(getIndentationAtPosition(newDoc(indent + "hello"), 1)).isEqualTo("\t");
		assertThat(getIndentationAtPosition(newDoc(indent + "hello"), 2)).isEqualTo("\t ");
	}

	@Test
	void testIsBlankLine() {
		assertThat(isBlankLine(newDoc(""), 0)).isTrue();
		assertThat(isBlankLine(newDoc(" "), 0)).isTrue();
		assertThat(isBlankLine(newDoc(" \t"), 0)).isTrue();
		assertThat(isBlankLine(newDoc("\n\n"), 1)).isTrue();
		assertThat(isBlankLine(newDoc("\n \n"), 1)).isTrue();
		assertThat(isBlankLine(newDoc("\n \t\n"), 1)).isTrue();

		assertThat(isBlankLine(newDoc("word"), 0)).isFalse();
		assertThat(isBlankLine(newDoc("\nword\n"), 1)).isFalse();
	}

	@Test
	void testIsEmptyLine() {
		assertThat(isEmptyLine(newDoc(""), 0)).isTrue();
		assertThat(isEmptyLine(newDoc(" "), 0)).isFalse();
		assertThat(isEmptyLine(newDoc(" \t"), 0)).isFalse();
		assertThat(isEmptyLine(newDoc("\n\n"), 1)).isTrue();
		assertThat(isEmptyLine(newDoc("\n \n"), 1)).isFalse();
		assertThat(isEmptyLine(newDoc("\n \t\n"), 1)).isFalse();

		assertThat(isEmptyLine(newDoc("word"), 0)).isFalse();
		assertThat(isEmptyLine(newDoc("\nword\n"), 1)).isFalse();
	}

	@Test
	void testReplaceIndent_IndentEmptyLines() {
		assertThat(replaceIndent("\t\t", 2, "", true)).hasToString("");
		assertThat(replaceIndent("foo  ", 2, "", true)).hasToString("foo  ");
		assertThat(replaceIndent(" \t foo", 2, "", true)).hasToString("foo");
		assertThat(replaceIndent(" foo\n bar", 2, "", true)).hasToString("foo\nbar");
		assertThat(replaceIndent("  foo\n\tbar", 2, "", true)).hasToString("foo\nbar");
		assertThat(replaceIndent(" foo\n\tbar", 2, "", true)).hasToString("foo\nbar");
		assertThat(replaceIndent("\tfoo\n\t\tbar", 2, "", true)).hasToString("foo\n\tbar");
		assertThat(replaceIndent("\tfoo\n  \tbar", 2, "", true)).hasToString("foo\n\tbar");

		assertThat(replaceIndent(" foo\r\n bar", 2, "", true)).hasToString("foo\r\nbar");
		assertThat(replaceIndent("  foo\r\n\tbar", 2, "", true)).hasToString("foo\r\nbar");
		assertThat(replaceIndent(" foo\r\n\tbar", 2, "", true)).hasToString("foo\r\nbar");
		assertThat(replaceIndent("\tfoo\r\n\t\tbar", 2, "", true)).hasToString("foo\r\n\tbar");
		assertThat(replaceIndent("\tfoo\r\n  \tbar", 2, "", true)).hasToString("foo\r\n\tbar");

		assertThat(replaceIndent("\t\t", 2, "..", true)).hasToString("..");
		assertThat(replaceIndent("foo  ", 2, "..", true)).hasToString("..foo  ");
		assertThat(replaceIndent(" \t foo", 2, "..", true)).hasToString("..foo");
		assertThat(replaceIndent(" foo\n bar", 2, "..", true)).hasToString("..foo\n..bar");
		assertThat(replaceIndent("  foo\n\tbar", 2, "..", true)).hasToString("..foo\n..bar");
		assertThat(replaceIndent(" foo\n\tbar", 2, "..", true)).hasToString("..foo\n..bar");
		assertThat(replaceIndent("\tfoo\n\t\tbar", 2, "..", true)).hasToString("..foo\n..\tbar");
		assertThat(replaceIndent("\tfoo\n  \tbar", 2, "..", true)).hasToString("..foo\n..\tbar");

		assertThat(replaceIndent("\n", 2, "..", true)).hasToString("..\n");
		assertThat(replaceIndent("\n\n", 2, "..", true)).hasToString("..\n..\n");
		assertThat(replaceIndent("\tfoo\n\tbar\n", 2, "..", true)).hasToString("..foo\n..bar\n");

		assertThat(replaceIndent("\r\n", 2, "..", true)).hasToString("..\r\n");
		assertThat(replaceIndent("\r\n\r\n", 2, "..", true)).hasToString("..\r\n..\r\n");
		assertThat(replaceIndent("\tfoo\r\n\tbar\r\n", 2, "..", true)).hasToString("..foo\r\n..bar\r\n");
	}

	@Test
	void testReplaceIndent_DoNotIndentEmptyLines() {
		assertThat(replaceIndent("\t\t", 2, "", false)).hasToString("");
		assertThat(replaceIndent("foo  ", 2, "", false)).hasToString("foo  ");
		assertThat(replaceIndent(" \t foo", 2, "", false)).hasToString("foo");
		assertThat(replaceIndent(" foo\n bar", 2, "", false)).hasToString("foo\nbar");
		assertThat(replaceIndent("  foo\n\tbar", 2, "", false)).hasToString("foo\nbar");
		assertThat(replaceIndent(" foo\n\tbar", 2, "", false)).hasToString("foo\nbar");
		assertThat(replaceIndent("\tfoo\n\t\tbar", 2, "", false)).hasToString("foo\n\tbar");
		assertThat(replaceIndent("\tfoo\n  \tbar", 2, "", false)).hasToString("foo\n\tbar");

		assertThat(replaceIndent(" foo\r\n bar", 2, "", false)).hasToString("foo\r\nbar");
		assertThat(replaceIndent("  foo\r\n\tbar", 2, "", false)).hasToString("foo\r\nbar");
		assertThat(replaceIndent(" foo\r\n\tbar", 2, "", false)).hasToString("foo\r\nbar");
		assertThat(replaceIndent("\tfoo\r\n\t\tbar", 2, "", false)).hasToString("foo\r\n\tbar");
		assertThat(replaceIndent("\tfoo\r\n  \tbar", 2, "", false)).hasToString("foo\r\n\tbar");

		assertThat(replaceIndent("\t\t", 2, "..", false)).hasToString("");
		assertThat(replaceIndent("foo  ", 2, "..", false)).hasToString("..foo  ");
		assertThat(replaceIndent(" \t foo", 2, "..", false)).hasToString("..foo");
		assertThat(replaceIndent(" foo\n bar", 2, "..", false)).hasToString("..foo\n..bar");
		assertThat(replaceIndent("  foo\n\tbar", 2, "..", false)).hasToString("..foo\n..bar");
		assertThat(replaceIndent(" foo\n\tbar", 2, "..", false)).hasToString("..foo\n..bar");
		assertThat(replaceIndent("\tfoo\n\t\tbar", 2, "..", false)).hasToString("..foo\n..\tbar");
		assertThat(replaceIndent("\tfoo\n  \tbar", 2, "..", false)).hasToString("..foo\n..\tbar");

		assertThat(replaceIndent("\n", 2, "..", false)).hasToString("\n");
		assertThat(replaceIndent("\n\n", 2, "..", false)).hasToString("\n\n");
		assertThat(replaceIndent("\tfoo\n\tbar\n", 2, "..", false)).hasToString("..foo\n..bar\n");

		assertThat(replaceIndent("\r\n", 2, "..", false)).hasToString("\r\n");
		assertThat(replaceIndent("\r\n\r\n", 2, "..", false)).hasToString("\r\n\r\n");
		assertThat(replaceIndent("\tfoo\r\n\tbar\r\n", 2, "..", false)).hasToString("..foo\r\n..bar\r\n");
	}
}
