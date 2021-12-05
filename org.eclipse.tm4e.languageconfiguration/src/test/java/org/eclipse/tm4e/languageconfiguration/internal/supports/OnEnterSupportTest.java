/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction.IndentAction;
import org.junit.jupiter.api.Test;

/**
 * {@link OnEnterSupport} tests.
 *
 */
public class OnEnterSupportTest {

	@Test
	public void useBrackets() {
		UseBracketsTest support = new UseBracketsTest();

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

		public UseBracketsTest() {
			super(Arrays.asList(new CharacterPair("(", ")"), new CharacterPair("begin", "end")), null);
		}

		public void testIndentAction(String beforeText, String afterText, IndentAction expected) {
			EnterAction actual = super.onEnter("", beforeText, afterText);
			if (expected == IndentAction.None) {
				assertNull(actual);
			} else {
				assertEquals(expected, actual.getIndentAction());
			}
		}
	}

	@Test
	public void regExpRules() {
		RegExpRulesTest support = new RegExpRulesTest();

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

	private class RegExpRulesTest extends OnEnterSupport {

		public RegExpRulesTest() {
			super(null,
					Arrays.asList(
							new OnEnterRule("^\\s*\\/\\*\\*(?!\\/)([^\\*]|\\*(?!\\/))*$", "^\\s*\\*\\/$",
									new EnterAction(IndentAction.IndentOutdent).setAppendText(" * ")),
							new OnEnterRule("^\\s*\\/\\*\\*(?!\\/)([^\\*]|\\*(?!\\/))*$", null,
									new EnterAction(IndentAction.None).setAppendText(" * ")),
							new OnEnterRule("^(\\t|(\\ \\ ))*\\ \\*(\\ ([^\\*]|\\*(?!\\/))*)?$", null,
									new EnterAction(IndentAction.None).setAppendText("* ")),
							new OnEnterRule("^(\\t|(\\ \\ ))*\\ \\*\\/\\s*$", null,
									new EnterAction(IndentAction.None).setRemoveText(1)),
							new OnEnterRule("^(\\t|(\\ \\ ))*\\ \\*[^/]*\\*\\/\\s*$", null,
									new EnterAction(IndentAction.None).setRemoveText(1))));
		}

		public void testIndentAction(String beforeText, String afterText, IndentAction expectedIndentAction,
				String expectedAppendText) {
			testIndentAction(beforeText, afterText, expectedIndentAction, expectedAppendText, 0);
		}

		public void testIndentAction(String beforeText, String afterText, IndentAction expectedIndentAction,
				String expectedAppendText, int removeText) {
			EnterAction actual = super.onEnter("", beforeText, afterText);
			if (expectedIndentAction == null) {
				assertNull(actual, "isNull:" + beforeText);
			} else {
				assertNotNull(actual, "isNotNull:" + beforeText);
				assertEquals(expectedIndentAction, actual.getIndentAction(), "indentAction:" + beforeText);
				if (expectedAppendText != null) {
					assertEquals(expectedAppendText, actual.getAppendText(), "appendText:" + beforeText);
				}
				if (removeText != 0) {
					assertEquals(removeText, actual.getRemoveText(), "removeText:" + beforeText);
				}
			}
		}
	}
}
