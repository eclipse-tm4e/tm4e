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
package org.eclipse.tm4e.core.internal.theme;

import static org.eclipse.tm4e.core.internal.theme.FontStyle.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.tm4e.core.internal.utils.CompareUtils;
import org.junit.jupiter.api.Test;

/**
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts</a>
 */
public class ThemeResolvingTest extends AbstractThemeTest {

	private static final ThemeTrieElementRule NOTSET_THEME_TRIE_ELEMENT_RULE = new ThemeTrieElementRule(0, null, NotSet,
		_NOT_SET, _NOT_SET);
	private static final ThemeTrieElement NOTSET_THEME_TRIE_ELEMENT = new ThemeTrieElement(
		NOTSET_THEME_TRIE_ELEMENT_RULE);

	private static void assertStrArrCmp(final String testCase, final List<String> a, final List<String> b,
		final int expected) {
		assertEquals(expected, CompareUtils.strArrCmp(a, b), testCase);
	}

	/**
	 * test: Theme parsing can parse
	 */
	@Test
	public void testThemeParsingCanParse() throws Exception {
		final var actual = parseTheme("""
			{ "settings": [
			{ "settings": { "foreground": "#F8F8F2", "background": "#272822" } },
			{ "scope": "source, something", "settings": { "background": "#100000" } },
			{ "scope": ["bar", "baz"], "settings": { "background": "#010000" } },
			{ "scope": "source.css selector bar", "settings": { "fontStyle": "bold" } },
			{ "scope": "constant", "settings": { "fontStyle": "italic", "foreground": "#ff0000" } },
			{ "scope": "constant.numeric", "settings": { "foreground": "#00ff00" } },
			{ "scope": "constant.numeric.hex", "settings": { "fontStyle": "bold" } },
			{ "scope": "constant.numeric.oct", "settings": { "fontStyle": "bold italic underline" } },
			{ "scope": "constant.numeric.bin", "settings": { "fontStyle": "bold strikethrough" } },
			{ "scope": "constant.numeric.dec", "settings": { "fontStyle": "", "foreground": "#0000ff" } },
			{ "scope": "foo", "settings": { "fontStyle": "", "foreground": "#CFA" } }
			]}""");

		final var expected = list(
			new ParsedThemeRule("", null, 0, NotSet, "#F8F8F2", "#272822"),
			new ParsedThemeRule("source", null, 1, NotSet, null, "#100000"),
			new ParsedThemeRule("something", null, 1, NotSet, null, "#100000"),
			new ParsedThemeRule("bar", null, 2, NotSet, null, "#010000"),
			new ParsedThemeRule("baz", null, 2, NotSet, null, "#010000"),
			new ParsedThemeRule("bar", list("selector", "source.css"), 3, Bold, null, null),
			new ParsedThemeRule("constant", null, 4, Italic, "#ff0000", null),
			new ParsedThemeRule("constant.numeric", null, 5, NotSet, "#00ff00", null),
			new ParsedThemeRule("constant.numeric.hex", null, 6, Bold, null, null),
			new ParsedThemeRule("constant.numeric.oct", null, 7, Bold | Italic | Underline, null, null),
			new ParsedThemeRule("constant.numeric.bin", null, 8, Bold | Strikethrough, null, null),
			new ParsedThemeRule("constant.numeric.dec", null, 9, None, "#0000ff", null),
			new ParsedThemeRule("foo", null, 10, None, "#CFA", null));

		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	/**
	 * test: Theme resolving strcmp works
	 */
	@Test
	public void testStrcmpWorks() {
		final var actual = list("bar", "z", "zu", "a", "ab", "");
		actual.sort(CompareUtils::strcmp);

		final var expected = list("", "a", "ab", "bar", "z", "zu");
		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	/**
	 * test: Theme resolving strArrCmp works
	 */
	@Test
	public void testStrArrCmpWorks() {
		assertStrArrCmp("001", null, null, 0);
		assertStrArrCmp("002", null, list(), -1);
		assertStrArrCmp("003", null, list("a"), -1);
		assertStrArrCmp("004", list(), null, 1);
		assertStrArrCmp("005", list("a"), null, 1);
		assertStrArrCmp("006", list(), list(), 0);
		assertStrArrCmp("007", list(), list("a"), -1);
		assertStrArrCmp("008", list("a"), list(), 1);
		assertStrArrCmp("009", list("a"), list("a"), 0);
		assertStrArrCmp("010", list("a", "b"), list("a"), 1);
		assertStrArrCmp("011", list("a"), list("a", "b"), -1);
		assertStrArrCmp("012", list("a", "b"), list("a", "b"), 0);
		assertStrArrCmp("013", list("a", "b"), list("a", "c"), -1);
		assertStrArrCmp("014", list("a", "c"), list("a", "b"), 1);
	}

	/**
	 * test: Theme resolving always has defaults
	 */
	@Test
	public void testAlwaysHasDefaults() {
		final var actual = createTheme();
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving respects incoming defaults 1
	 */
	@Test
	public void testRespectsIncomingDefaults1() {
		final var actual = createTheme(new ParsedThemeRule("", null, -1, NotSet, null, null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving respects incoming defaults 2
	 */
	@Test
	public void testRespectsIncomingDefaults2() {
		final Theme actual = createTheme(new ParsedThemeRule("", null, -1, None, null, null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving respects incoming defaults 3
	 */
	@Test
	public void testRespectsIncomingDefaults3() {
		final var actual = createTheme(new ParsedThemeRule("", null, -1, Bold, null, null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, Bold, _A, _B),
			NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving respects incoming defaults 4
	 */
	@Test
	public void testRespectsIncomingDefaults4() {
		final var actual = createTheme(new ParsedThemeRule("", null, -1, NotSet, "#ff0000", null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#ff0000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving respects incoming defaults 5
	 */
	@Test
	public void testRespectsIncomingDefaults5() {
		final var actual = createTheme(new ParsedThemeRule("", null, -1, NotSet, null, "#ff0000"));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ff0000");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving can merge incoming defaults
	 */
	@Test
	public void testCanMergeIncomingDefaults() {
		final var actual = createTheme(
			new ParsedThemeRule("", null, -1, NotSet, null, "#ff0000"),
			new ParsedThemeRule("", null, -1, NotSet, "#00ff00", null),
			new ParsedThemeRule("", null, -1, Bold, null, null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#00ff00");
		final int _B = colorMap.getId("#ff0000");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, Bold, _A, _B),
			NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving defaults are inherited
	 */
	@Test
	public void testDefaultsAreInherited() {
		final Theme actual = createTheme(
			new ParsedThemeRule("", null, -1, NotSet, "#F8F8F2", "#272822"),
			new ParsedThemeRule("var", null, -1, NotSet, "#ff0000", null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#F8F8F2");
		final int _B = colorMap.getId("#272822");
		final int _C = colorMap.getId("#ff0000");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
				"var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, NotSet, _C, _NOT_SET)) //
			)));
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving same rules get merged
	 */
	@Test
	public void testSameRulesGetMerged() {
		final var actual = createTheme(
			new ParsedThemeRule("", null, -1, NotSet, "#F8F8F2", "#272822"),
			new ParsedThemeRule("var", null, 1, Bold, null, null),
			new ParsedThemeRule("var", null, 0, NotSet, "#ff0000", null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#F8F8F2");
		final int _B = colorMap.getId("#272822");
		final int _C = colorMap.getId("#ff0000");
		final var expected = new Theme(
			colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
				"var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, Bold, _C, _NOT_SET)) //
			)));
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving rules are inherited 1
	 */
	@Test
	public void testRulesAreInherited1() {
		final var actual = createTheme(
			new ParsedThemeRule("", null, -1, NotSet, "#F8F8F2", "#272822"),
			new ParsedThemeRule("var", null, -1, Bold, "#ff0000", null),
			new ParsedThemeRule("var.identifier", null, -1, NotSet, "#00ff00", null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#F8F8F2");
		final int _B = colorMap.getId("#272822");
		final int _C = colorMap.getId("#ff0000");
		final int _D = colorMap.getId("#00ff00");
		final var expected = new Theme(colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
				"var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, Bold, _C, _NOT_SET), list(), map(
					"identifier", new ThemeTrieElement(new ThemeTrieElementRule(2, null, Bold, _D, _NOT_SET)) //
				)) //
			)));
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving rules are inherited 2
	 */
	@Test
	public void testRulesAreInherited2() {
		final var actual = createTheme(
			new ParsedThemeRule("", null, -1, NotSet, "#F8F8F2", "#272822"),
			new ParsedThemeRule("var", null, -1, Bold, "#ff0000", null),
			new ParsedThemeRule("var.identifier", null, -1, NotSet, "#00ff00", null),
			new ParsedThemeRule("constant", null, 4, Italic, "#100000", null),
			new ParsedThemeRule("constant.numeric", null, 5, NotSet, "#200000", null),
			new ParsedThemeRule("constant.numeric.hex", null, 6, Bold, null, null),
			new ParsedThemeRule("constant.numeric.oct", null, 7,
				Bold | Italic | Underline, null, null),
			new ParsedThemeRule("constant.numeric.dec", null, 8, None, "#300000", null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#F8F8F2");
		final int _B = colorMap.getId("#272822");
		final int _C = colorMap.getId("#100000");
		final int _D = colorMap.getId("#200000");
		final int _E = colorMap.getId("#300000");
		final int _F = colorMap.getId("#ff0000");
		final int _G = colorMap.getId("#00ff00");

		final var expected = new Theme(colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
				"var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, Bold, _F, _NOT_SET), list(), map(
					"identifier", new ThemeTrieElement(new ThemeTrieElementRule(2, null, Bold, _G, _NOT_SET)) //
				)),
				"constant", new ThemeTrieElement(new ThemeTrieElementRule(1, null, Italic, _C, _NOT_SET), list(), map(
					"numeric",
					new ThemeTrieElement(new ThemeTrieElementRule(2, null, Italic, _D, _NOT_SET), list(), map(
						"hex", new ThemeTrieElement(new ThemeTrieElementRule(3, null, Bold, _D, _NOT_SET)),
						"oct", new ThemeTrieElement(new ThemeTrieElementRule(3, null,
							Bold | Italic | Underline, _D, _NOT_SET)),
						"dec", new ThemeTrieElement(new ThemeTrieElementRule(3, null, None, _E, _NOT_SET)) //
					)))) //
			)));
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving rules with parent scopes
	 */
	@Test
	public void testRulesWithParentScopes() {
		final var actual = createTheme(
			new ParsedThemeRule("", null, -1, NotSet, "#F8F8F2", "#272822"),
			new ParsedThemeRule("var", null, -1, Bold, "#100000", null),
			new ParsedThemeRule("var.identifier", null, -1, NotSet, "#200000", null),
			new ParsedThemeRule("var", list("source.css"), 1, Italic, "#300000", null),
			new ParsedThemeRule("var", list("source.css"), 2, Underline, null, null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#F8F8F2");
		final int _B = colorMap.getId("#272822");
		final int _C = colorMap.getId("#100000");
		final int _D = colorMap.getId("#300000");
		final int _E = colorMap.getId("#200000");
		final var expected = new Theme(colorMap,
			new ThemeTrieElementRule(0, null, None, _A, _B),
			new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
				"var", new ThemeTrieElement(
					new ThemeTrieElementRule(1, null, Bold, _C, _NOT_SET),
					list(new ThemeTrieElementRule(1, list("source.css"), Underline, _D, _NOT_SET)), map(
						"identifier", new ThemeTrieElement(
							new ThemeTrieElementRule(2, null, Bold, _E, _NOT_SET),
							list(new ThemeTrieElementRule(1, list("source.css"), Underline, _D, _NOT_SET))) //
					) //
				) //
			)));
		assertEquals(actual, expected);
	}

	/**
	 * test: Theme resolving issue #38: ignores rules with invalid colors
	 */
	@Test
	public void testIssue_38_ignores_rules_with_invalid_colors() throws Exception {
		final var actual = parseTheme("""
			{ "settings": [
				{
					"settings": {
						"background": "#222222",
						"foreground": "#cccccc"
					}
				}, {
					"name": "Variable",
					"scope": "variable",
					"settings": {
						"fontStyle": ""
					}
				}, {
					"name": "Function argument",
					"scope": "variable.parameter",
					"settings": {
						"fontStyle": "italic",
						"foreground": ""
					}
				}, {
					"name": "Library variable",
					"scope": "support.other.variable",
					"settings": {
						"fontStyle": ""
					}
				}, {
					"name": "Function argument",
					"scope": "variable.other",
					"settings": {
						"foreground": "",
						"fontStyle": "normal"
					}
				}, {
					"name": "Coffeescript Function argument",
					"scope": "variable.parameter.function.coffee",
					"settings": {
						"foreground": "#F9D423",
						"fontStyle": "italic"
					}
				}
			]}""");

		final var expected = new ParsedThemeRule[] {
			new ParsedThemeRule("", null, 0, NotSet, "#cccccc", "#222222"),
			new ParsedThemeRule("variable", null, 1, None, null, null),
			new ParsedThemeRule("variable.parameter", null, 2, Italic, null, null),
			new ParsedThemeRule("support.other.variable", null, 3, None, null, null),
			new ParsedThemeRule("variable.other", null, 4, None, null, null),
			new ParsedThemeRule("variable.parameter.function.coffee", null, 5, Italic, "#F9D423", null)
		};
		assertArrayEquals(expected, actual.toArray());
	}

	/**
	 * test: Theme resolving issue #35: Trailing comma in a tmTheme scope selector
	 */
	@Test
	public void testIssue_35_Trailing_comma_in_a_tmTheme_scope_selector() throws Exception {
		final var actual = parseTheme("""
			{ "settings": [{
					"settings": {
						"background": "#25292C",
						"foreground": "#EFEFEF"
					}
				}, {
					"name": "CSS at-rule keyword control",
					"scope": "
						meta.at-rule.return.scss,\n
						meta.at-rule.return.scss punctuation.definition,\n
						meta.at-rule.else.scss,\n
						meta.at-rule.else.scss punctuation.definition,\n
						meta.at-rule.if.scss,\n
						meta.at-rule.if.scss punctuation.definition\n
					",
					"settings": {
						"foreground": "#CC7832"
					}
				}
			]}""");

		final var expected = new ParsedThemeRule[] {
			new ParsedThemeRule("", null, 0, NotSet, "#EFEFEF", "#25292C"),
			new ParsedThemeRule("meta.at-rule.return.scss", null, 1, NotSet, "#CC7832", null),
			new ParsedThemeRule("punctuation.definition", list("meta.at-rule.return.scss"), 1, NotSet, "#CC7832", null),
			new ParsedThemeRule("meta.at-rule.else.scss", null, 1, NotSet, "#CC7832", null),
			new ParsedThemeRule("punctuation.definition", list("meta.at-rule.else.scss"), 1, NotSet, "#CC7832", null),
			new ParsedThemeRule("meta.at-rule.if.scss", null, 1, NotSet, "#CC7832", null),
			new ParsedThemeRule("punctuation.definition", list("meta.at-rule.if.scss"), 1, NotSet, "#CC7832", null)
		};
		assertArrayEquals(expected, actual.toArray());
	}
}
