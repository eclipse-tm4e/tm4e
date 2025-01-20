/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 * - Sebastian Thomschke (Vegard IT GmbH) - refactor and extend test cases
 */
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.OnEnterRule;
import org.eclipse.tm4e.languageconfiguration.internal.model.RegExPattern;
import org.junit.jupiter.api.Test;

/**
 * {@link OnEnterSupport} tests.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/test/common/modes/supports/onEnter.test.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/test/common/modes/supports/onEnter.test.ts</a>
 */
class OnEnterSupportTest {

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/test/common/modes/supports/javascriptOnEnterRules.ts">
	 *      github.com/microsoft/vscode/blob/main/src/vs/editor/test/common/modes/supports/javascriptOnEnterRules.ts</a>
	 */
	static final List<OnEnterRule> javaScriptOnEnterRules = List.of(
			new OnEnterRule( // e.g. /** | */
					RegExPattern.of("^\\s*\\/\\*\\*(?!\\/)([^\\*]|\\*(?!\\/))*$"),
					RegExPattern.of("^\\s*\\*\\/$"),
					null,
					new EnterAction(IndentAction.IndentOutdent, " * ", null)),
			new OnEnterRule( // e.g. /** ...|
					RegExPattern.of("^\\s*\\/\\*\\*(?!\\/)([^\\*]|\\*(?!\\/))*$"),
					null,
					null,
					new EnterAction(IndentAction.None, " * ", null)),
			new OnEnterRule(
					// e.g.  * ...|
					RegExPattern.of("^(\\t|(\\ \\ ))*\\ \\*(\\ ([^\\*]|\\*(?!\\/))*)?$"),
					null,
					RegExPattern.of("(?=^(\\s*(\\/\\*\\*|\\*)).*)(?=(?!(\\s*\\*\\/)))"),
					new EnterAction(IndentAction.None, "* ", null)),
			new OnEnterRule( // e.g.  */|
					RegExPattern.of("^(\\t|(\\ \\ ))*\\ \\*\\/\\s*$"),
					null,
					null,
					new EnterAction(IndentAction.None, null, 1)),
			new OnEnterRule( // e.g.  *-----*/|
					RegExPattern.of("^(\\t|(\\ \\ ))*\\ \\*[^/]*\\*\\/\\s*$"),
					null,
					null,
					new EnterAction(IndentAction.None, null, 1)));

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
					assertThat(actual).isNull();
				} else {
					assert actual != null;
					assertThat(actual.indentAction).isEqualTo(expected);
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
	void testUseRegExpRules() {

		class Support extends OnEnterSupport {
			Support() {
				super(null, javaScriptOnEnterRules);
			}

			void testIndentAction(final String previousLineText, final String beforeText, final String afterText,
					final @Nullable IndentAction expectedIndentAction, final @Nullable String expectedAppendText) {
				testIndentAction(previousLineText, beforeText, afterText, expectedIndentAction, expectedAppendText, 0);
			}

			@SuppressWarnings("null")
			void testIndentAction(final String previousLineText, final String beforeText, final String afterText,
					final @Nullable IndentAction expectedIndentAction, final @Nullable String expectedAppendText,
					final int removeText) {
				final EnterAction actual = onEnter(previousLineText, beforeText, afterText);
				if (expectedIndentAction == null) {
					assertThat(actual).as("isNull:" + beforeText).isNull();
				} else {
					assertThat(actual).as("isNotNull:" + beforeText).isNotNull();
					assert actual != null;
					assertThat(actual.indentAction).as("indentAction:" + beforeText).isEqualTo(expectedIndentAction);
					if (expectedAppendText != null) {
						assertThat(actual.appendText).as("appendText:" + beforeText).isEqualTo(expectedAppendText);
					}
					if (removeText != 0) {
						assertThat(actual.removeText).as("removeText:" + beforeText).isEqualTo(removeText);
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

	@Test
	void testVscodeIssue43469() {

		class Support extends OnEnterSupport {
			Support() {
				super(null, javaScriptOnEnterRules);
			}

			void testIndentAction(final String beforeText, final String afterText, final @Nullable IndentAction expected) {
				final EnterAction actual = onEnter("", beforeText, afterText);
				if (expected == IndentAction.None) {
					assertThat(actual).isNull();
				} else {
					assert actual != null;
					assertThat(actual.indentAction).isEqualTo(expected);
				}
			}
		}

		final var support = new Support();
		support.testIndentAction("const r = /{/;", "", IndentAction.None);
		support.testIndentAction("const r = /{[0-9]/;", "", IndentAction.None);
		support.testIndentAction("const r = /[a-zA-Z]{/;", "", IndentAction.None);
	}
}
