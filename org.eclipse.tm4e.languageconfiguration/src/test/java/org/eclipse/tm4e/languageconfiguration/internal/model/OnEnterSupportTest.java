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
 */
package org.eclipse.tm4e.languageconfiguration.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterSupport;
import org.junit.jupiter.api.Test;

/**
 * {@link OnEnterSupport} tests.
 */
public class OnEnterSupportTest {

	@Test
	public void useBrackets() {
		final var support = new UseBracketsTest();

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

	private class UseBracketsTest extends OnEnterSupport {

		UseBracketsTest() {
			super(Arrays.asList(new CharacterPair("(", ")"), new CharacterPair("begin", "end")), null);
		}

		public void testIndentAction(final String beforeText, final String afterText, final IndentAction expected) {
			final EnterAction actual = super.onEnter(beforeText, afterText);
			if (expected == IndentAction.None) {
				assertNull(actual);
			} else {
				assertNotNull(actual);
				assertEquals(expected, actual.indentAction);
			}
		}
	}

	@Test
	public void regExpRules() {
		final var support = new RegExpRulesTest();

		support.testIndentAction("\t/**", " */", IndentAction.IndentOutdent, " * ");
		support.testIndentAction("\t/**", "", IndentAction.None, " * ");
		support.testIndentAction("\t/** * / * / * /", "", IndentAction.None, " * ");
		support.testIndentAction("\t/** /*", "", IndentAction.None, " * ");
		support.testIndentAction("/**", "", IndentAction.None, " * ");
		support.testIndentAction("\t/**/", "", null, null);
		support.testIndentAction("\t/***/", "", null, null);
		support.testIndentAction("\t/*******/", "", null, null);
		support.testIndentAction("\t/** * * * * */", "", null, null);
		support.testIndentAction("\t/** */", "", null, null);
		support.testIndentAction("\t/** asdfg */", "", null, null);
		support.testIndentAction("\t/* asdfg */", "", null, null);
		support.testIndentAction("\t/* asdfg */", "", null, null);
		support.testIndentAction("\t/** asdfg */", "", null, null);
		support.testIndentAction("*/", "", null, null);
		support.testIndentAction("\t/*", "", null, null);
		support.testIndentAction("\t*", "", null, null);
		support.testIndentAction("\t *", "", IndentAction.None, "* ");
		support.testIndentAction("\t */", "", IndentAction.None, null, 1);
		support.testIndentAction("\t * */", "", IndentAction.None, null, 1);
		support.testIndentAction("\t * * / * / * / */", "", null, null);
		support.testIndentAction("\t * ", "", IndentAction.None, "* ");
		support.testIndentAction(" * ", "", IndentAction.None, "* ");
		support.testIndentAction(" * asdfsfagadfg", "", IndentAction.None, "* ");
		support.testIndentAction(" * asdfsfagadfg * * * ", "", IndentAction.None, "* ");
		support.testIndentAction(" * /*", "", IndentAction.None, "* ");
		support.testIndentAction(" * asdfsfagadfg * / * / * /", "", IndentAction.None, "* ");
		support.testIndentAction(" * asdfsfagadfg * / * / * /*", "", IndentAction.None, "* ");
		support.testIndentAction(" */", "", IndentAction.None, null, 1);
		support.testIndentAction("\t */", "", IndentAction.None, null, 1);
		support.testIndentAction("\t\t */", "", IndentAction.None, null, 1);
		support.testIndentAction("   */", "", IndentAction.None, null, 1);
		support.testIndentAction("     */", "", IndentAction.None, null, 1);
		support.testIndentAction("\t     */", "", IndentAction.None, null, 1);
		support.testIndentAction(
				" *--------------------------------------------------------------------------------------------*/", "",
				IndentAction.None, null, 1);
	}

	private static final class RegExpRulesTest extends OnEnterSupport {

		RegExpRulesTest() {
			super(null, List.of(
					new OnEnterRule("^\\s*\\/\\*\\*(?!\\/)([^\\*]|\\*(?!\\/))*$", "^\\s*\\*\\/$",
							new EnterAction(IndentAction.IndentOutdent).withAppendText(" * ")),
					new OnEnterRule("^\\s*\\/\\*\\*(?!\\/)([^\\*]|\\*(?!\\/))*$", null,
							new EnterAction(IndentAction.None).withAppendText(" * ")),
					new OnEnterRule("^(\\t|(\\ \\ ))*\\ \\*(\\ ([^\\*]|\\*(?!\\/))*)?$", null,
							new EnterAction(IndentAction.None).withAppendText("* ")),
					new OnEnterRule("^(\\t|(\\ \\ ))*\\ \\*\\/\\s*$", null,
							new EnterAction(IndentAction.None).withRemoveText(1)),
					new OnEnterRule("^(\\t|(\\ \\ ))*\\ \\*[^/]*\\*\\/\\s*$", null,
							new EnterAction(IndentAction.None).withRemoveText(1))));
		}

		void testIndentAction(final String beforeText, final String afterText,
				@Nullable final IndentAction expectedIndentAction, @Nullable final String expectedAppendText) {
			testIndentAction(beforeText, afterText, expectedIndentAction, expectedAppendText, 0);
		}

		void testIndentAction(final String beforeText, final String afterText,
				@Nullable final IndentAction expectedIndentAction, @Nullable final String expectedAppendText,
				final int removeText) {
			final EnterAction actual = super.onEnter(beforeText, afterText);
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
}
