/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Sebastian Thomschke (Vegard IT GmbH) - refactor and extend test cases
 */
package org.eclipse.tm4e.languageconfiguration.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterSupport;
import org.junit.jupiter.api.Test;

/**
 * {@link OnEnterSupport} tests.
 *
 * @see <a href="https://github.com/microsoft/vscode/blob/main/src/vs/editor/test/common/modes/supports/onEnter.test.ts">
 *      https://github.com/microsoft/vscode/blob/main/src/vs/editor/test/common/modes/supports/onEnter.test.ts</a>
 */
class OnEnterSupportTest {

	@Test
	void testUseBrackets() {

		class Support extends OnEnterSupport {
			Support() {
				super(List.of(
						new CharacterPair("(", ")"),
						new CharacterPair("begin", "end")),
						null);
			}

			void testIndentAction(final String beforeText, final String afterText, final IndentAction expected) {
				final EnterAction actual = onEnter("", beforeText, afterText);
				if (expected == IndentAction.None) {
					assertNull(actual);
				} else {
					assertNotNull(actual);
					assertEquals(expected, actual.indentAction);
				}
			}
		}

		final var support = new Support();

		support.testIndentAction("a", "", IndentAction.None);
		support.testIndentAction("", "b", IndentAction.None);
		support.testIndentAction("(", "b", IndentAction.Indent);
		support.testIndentAction("a", ")", IndentAction.None);
		support.testIndentAction("begin", "ending", IndentAction.Indent);
		support.testIndentAction("abegin", "end", IndentAction.None);
		support.testIndentAction("begin", ")", IndentAction.Indent);
		support.testIndentAction("begin", "end", IndentAction.IndentOutdent);
		support.testIndentAction("begin ", " end", IndentAction.IndentOutdent);
		support.testIndentAction(" begin", "end//as", IndentAction.IndentOutdent);
		support.testIndentAction("(", ")", IndentAction.IndentOutdent);
		support.testIndentAction("( ", ")", IndentAction.IndentOutdent);
		support.testIndentAction("a(", ")b", IndentAction.IndentOutdent);

		support.testIndentAction("(", "", IndentAction.Indent);
		support.testIndentAction("(", "foo", IndentAction.Indent);
		support.testIndentAction("begin", "foo", IndentAction.Indent);
		support.testIndentAction("begin", "", IndentAction.Indent);
	}

	@Test
	void testRegExpRules() {

		class Support extends OnEnterSupport {
			Support() {
				super(null, List.of(
						// see https://github.com/microsoft/vscode/blob/main/src/vs/editor/test/common/modes/supports/javascriptOnEnterRules.ts
						new OnEnterRule( // e.g. /** | */
								"^\\s*\\/\\*\\*(?!\\/)([^\\*]|\\*(?!\\/))*$",
								"^\\s*\\*\\/$", null,
								new EnterAction(IndentAction.IndentOutdent, " * ", null)),
						new OnEnterRule( // e.g. /** ...|
								"^\\s*\\/\\*\\*(?!\\/)([^\\*]|\\*(?!\\/))*$",
								null, null,
								new EnterAction(IndentAction.None, " * ", null)),
						new OnEnterRule(
								// e.g.  * ...|
								"^(\\t|(\\ \\ ))*\\ \\*(\\ ([^\\*]|\\*(?!\\/))*)?$",
								null, "(?=^(\\s*(\\/\\*\\*|\\*)).*)(?=(?!(\\s*\\*\\/)))",
								new EnterAction(IndentAction.None, "* ", null)),
						new OnEnterRule( // e.g.  */|
								"^(\\t|(\\ \\ ))*\\ \\*\\/\\s*$",
								null, null,
								new EnterAction(IndentAction.None, null, 1)),
						new OnEnterRule( // e.g.  *-----*/|
								"^(\\t|(\\ \\ ))*\\ \\*[^/]*\\*\\/\\s*$",
								null, null,
								new EnterAction(IndentAction.None, null, 1))));
			}

			void testIndentAction(final String previousLineText, final String beforeText, final String afterText,
					@Nullable final IndentAction expectedIndentAction, @Nullable final String expectedAppendText) {
				testIndentAction(previousLineText, beforeText, afterText, expectedIndentAction, expectedAppendText, 0);
			}

			void testIndentAction(final String previousLineText, final String beforeText, final String afterText,
					@Nullable final IndentAction expectedIndentAction, @Nullable final String expectedAppendText,
					final int removeText) {
				final EnterAction actual = onEnter(previousLineText, beforeText, afterText);
				if (expectedIndentAction == null) {
					assertNull(actual, "isNull:" + beforeText);
				} else {
					assertNotNull(actual, "isNotNull:" + beforeText);
					assertEquals(expectedIndentAction, actual.indentAction, "indentAction:" + beforeText);
					if (expectedAppendText != null) {
						assertEquals(expectedAppendText, actual.appendText, "appendText:" + beforeText);
					}
					if (removeText != 0) {
						assertEquals(removeText, actual.removeText, "removeText:" + beforeText);
					}
				}
			}
		}

		final var support = new Support();

		support.testIndentAction("", "\t/**", " */", IndentAction.IndentOutdent, " * ");
		support.testIndentAction("", "\t/**", "", IndentAction.None, " * ");
		support.testIndentAction("", "\t/** * / * / * /", "", IndentAction.None, " * ");
		support.testIndentAction("", "\t/** /*", "", IndentAction.None, " * ");
		support.testIndentAction("", "/**", "", IndentAction.None, " * ");
		support.testIndentAction("", "\t/**/", "", null, null);
		support.testIndentAction("", "\t/***/", "", null, null);
		support.testIndentAction("", "\t/*******/", "", null, null);
		support.testIndentAction("", "\t/** * * * * */", "", null, null);
		support.testIndentAction("", "\t/** */", "", null, null);
		support.testIndentAction("", "\t/** asdfg */", "", null, null);
		support.testIndentAction("", "\t/* asdfg */", "", null, null);
		support.testIndentAction("", "\t/* asdfg */", "", null, null);
		support.testIndentAction("", "\t/** asdfg */", "", null, null);
		support.testIndentAction("", "*/", "", null, null);
		support.testIndentAction("", "\t/*", "", null, null);
		support.testIndentAction("", "\t*", "", null, null);

		support.testIndentAction("\t/**", "\t *", "", IndentAction.None, "* ");
		support.testIndentAction("\t * something", "\t *", "", IndentAction.None, "* ");
		support.testIndentAction("\t *", "\t *", "", IndentAction.None, "* ");

		support.testIndentAction("", "\t */", "", IndentAction.None, null, 1);
		support.testIndentAction("", "\t * */", "", IndentAction.None, null, 1);
		support.testIndentAction("", "\t * * / * / * / */", "", null, null);

		support.testIndentAction("\t/**", "\t * ", "", IndentAction.None, "* ");
		support.testIndentAction("\t * something", "\t * ", "", IndentAction.None, "* ");
		support.testIndentAction("\t *", "\t * ", "", IndentAction.None, "* ");

		support.testIndentAction("/**", " * ", "", IndentAction.None, "* ");
		support.testIndentAction(" * something", " * ", "", IndentAction.None, "* ");
		support.testIndentAction(" *", " * asdfsfagadfg", "", IndentAction.None, "* ");

		support.testIndentAction("/**", " * asdfsfagadfg * * * ", "", IndentAction.None, "* ");
		support.testIndentAction(" * something", " * asdfsfagadfg * * * ", "", IndentAction.None, "* ");
		support.testIndentAction(" *", " * asdfsfagadfg * * * ", "", IndentAction.None, "* ");

		support.testIndentAction("/**", " * /*", "", IndentAction.None, "* ");
		support.testIndentAction(" * something", " * /*", "", IndentAction.None, "* ");
		support.testIndentAction(" *", " * /*", "", IndentAction.None, "* ");

		support.testIndentAction("/**", " * asdfsfagadfg * / * / * /", "", IndentAction.None, "* ");
		support.testIndentAction(" * something", " * asdfsfagadfg * / * / * /", "", IndentAction.None, "* ");
		support.testIndentAction(" *", " * asdfsfagadfg * / * / * /", "", IndentAction.None, "* ");

		support.testIndentAction("/**", " * asdfsfagadfg * / * / * /*", "", IndentAction.None, "* ");
		support.testIndentAction(" * something", " * asdfsfagadfg * / * / * /*", "", IndentAction.None, "* ");
		support.testIndentAction(" *", " * asdfsfagadfg * / * / * /*", "", IndentAction.None, "* ");

		support.testIndentAction("", " */", "", IndentAction.None, null, 1);
		support.testIndentAction(" */", " * test() {", "", IndentAction.Indent, null, 0);
		support.testIndentAction("", "\t */", "", IndentAction.None, null, 1);
		support.testIndentAction("", "\t\t */", "", IndentAction.None, null, 1);
		support.testIndentAction("", "   */", "", IndentAction.None, null, 1);
		support.testIndentAction("", "     */", "", IndentAction.None, null, 1);
		support.testIndentAction("", "\t     */", "", IndentAction.None, null, 1);
		support.testIndentAction("", " *--------------------------------------------------------------------------------------------*/", "",
				IndentAction.None, null, 1);

		// issue https://github.com/microsoft/vscode/issues/43469
		support.testIndentAction("class A {", "    * test() {", "", IndentAction.Indent, null, 0);
		support.testIndentAction("", "    * test() {", "", IndentAction.Indent, null, 0);
		support.testIndentAction("    ", "    * test() {", "", IndentAction.Indent, null, 0);
		support.testIndentAction("class A {", "  * test() {", "", IndentAction.Indent, null, 0);
		support.testIndentAction("", "  * test() {", "", IndentAction.Indent, null, 0);
		support.testIndentAction("  ", "  * test() {", "", IndentAction.Indent, null, 0);
	}
}
