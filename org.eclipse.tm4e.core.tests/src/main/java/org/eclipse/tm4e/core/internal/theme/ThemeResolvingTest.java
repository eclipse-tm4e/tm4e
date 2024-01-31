/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.theme;

import static org.eclipse.tm4e.core.internal.theme.FontStyle.*;

import java.util.List;

import org.eclipse.tm4e.core.internal.utils.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/09effd8b7429b71010e0fa34ea2e16e622692946/src/tests/themes.test.ts#L323">
 *      github.com/microsoft/vscode-textmate/blob/main/src/tests/themes.test.ts</a>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ThemeResolvingTest extends AbstractThemeTest {

	private static final ThemeTrieElementRule NOTSET_THEME_TRIE_ELEMENT_RULE = new ThemeTrieElementRule(0, null, NotSet, _NOT_SET,
			_NOT_SET);
	private static final ThemeTrieElement NOTSET_THEME_TRIE_ELEMENT = new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE);

	private static void assertStrArrCmp(final String testCase, final List<String> a, final List<String> b, final int expected) {
		assertEquals(expected, StringUtils.strArrCmp(a, b), testCase);
	}

	@Test
	@Order(1)
	@DisplayName("Theme resolving strcmp works")
	public void testStrcmpWorks() {
		final var actual = list("bar", "z", "zu", "a", "ab", "");
		actual.sort(StringUtils::strcmp);

		final var expected = list("", "a", "ab", "bar", "z", "zu");
		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	@Order(2)
	@DisplayName("Theme resolving strArrCmp works")
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

	protected static final Gson THEME_GSON = new GsonBuilder()
			.setExclusionStrategies(new ExclusionStrategy() {
				@Override
				public boolean shouldSkipField(final FieldAttributes f) {
					return f.getDeclaredClass() == Theme.class && f.getName().equals("_cachedMatchRoot"); // ignore the cache objects
				}

				@Override
				public boolean shouldSkipClass(final Class<?> clazz) {
					return false;
				}
			})
			.setPrettyPrinting().create();

	private static void assertThemeEqual(final Theme actual, final Theme expected) {
		// if this fails, we get a nice visual representation of the difference:
		assertEquals(THEME_GSON.toJson(expected), THEME_GSON.toJson(actual));

		// this ensures hashCode/equals are properly implemented:
		assertEquals(expected, actual);
	}

	@Test
	@Order(3)
	@DisplayName("Theme resolving always has defaults")
	public void testAlwaysHasDefaults() {

		final var actual = createTheme();
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
				colorMap,
				StyleAttributes.of(None, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(4)
	@DisplayName("Theme resolving respects incoming defaults 1")
	public void testRespectsIncomingDefaults1() {
		final var actual = createTheme(new ParsedThemeRule("", null, -1, NotSet, null, null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
				colorMap,
				StyleAttributes.of(None, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(5)
	@DisplayName("Theme resolving respects incoming defaults 2")
	public void testRespectsIncomingDefaults2() {
		final Theme actual = createTheme(new ParsedThemeRule("", null, -1, None, null, null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
				colorMap,
				StyleAttributes.of(None, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(6)
	@DisplayName("Theme resolving respects incoming defaults 3")
	public void testRespectsIncomingDefaults3() {
		final var actual = createTheme(new ParsedThemeRule("", null, -1, Bold, null, null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
				colorMap,
				StyleAttributes.of(Bold, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(7)
	@DisplayName("Theme resolving respects incoming defaults 4")
	public void testRespectsIncomingDefaults4() {
		final var actual = createTheme(new ParsedThemeRule("", null, -1, NotSet, "#ff0000", null));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#ff0000");
		final int _B = colorMap.getId("#ffffff");
		final var expected = new Theme(
				colorMap,
				StyleAttributes.of(None, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(8)
	@DisplayName("Theme resolving respects incoming defaults 5")
	public void testRespectsIncomingDefaults5() {
		final var actual = createTheme(new ParsedThemeRule("", null, -1, NotSet, null, "#ff0000"));
		final var colorMap = new ColorMap();
		final int _A = colorMap.getId("#000000");
		final int _B = colorMap.getId("#ff0000");
		final var expected = new Theme(
				colorMap,
				StyleAttributes.of(None, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(9)
	@DisplayName("Theme resolving can merge incoming defaults")
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
				StyleAttributes.of(Bold, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(10)
	@DisplayName("Theme resolving defaults are inherited")
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
				StyleAttributes.of(None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
						"var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, NotSet, _C, _NOT_SET)) //
				)));
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(11)
	@DisplayName("Theme resolving same rules get merged")
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
				StyleAttributes.of(None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
						"var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, Bold, _C, _NOT_SET)) //
				)));
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(12)
	@DisplayName("Theme resolving rules are inherited 1")
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
				StyleAttributes.of(None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
						"var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, Bold, _C, _NOT_SET), list(), map(
								"identifier", new ThemeTrieElement(new ThemeTrieElementRule(2, null, Bold, _D, _NOT_SET)) //
						)) //
				)));
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(13)
	@DisplayName("Theme resolving rules are inherited 2")
	public void testRulesAreInherited2() {
		final var actual = createTheme(
				new ParsedThemeRule("", null, -1, NotSet, "#F8F8F2", "#272822"),
				new ParsedThemeRule("var", null, -1, Bold, "#ff0000", null),
				new ParsedThemeRule("var.identifier", null, -1, NotSet, "#00ff00", null),
				new ParsedThemeRule("constant", null, 4, Italic, "#100000", null),
				new ParsedThemeRule("constant.numeric", null, 5, NotSet, "#200000", null),
				new ParsedThemeRule("constant.numeric.hex", null, 6, Bold, null, null),
				new ParsedThemeRule("constant.numeric.oct", null, 7, Bold | Italic | Underline, null, null),
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
				StyleAttributes.of(None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, list(), map(
						"var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, Bold, _F, _NOT_SET), list(), map(
								"identifier", new ThemeTrieElement(new ThemeTrieElementRule(2, null, Bold, _G, _NOT_SET)) //
						)),
						"constant", new ThemeTrieElement(new ThemeTrieElementRule(1, null, Italic, _C, _NOT_SET), list(), map(
								"numeric",
								new ThemeTrieElement(new ThemeTrieElementRule(2, null, Italic, _D, _NOT_SET), list(), map(
										"hex", new ThemeTrieElement(new ThemeTrieElementRule(3, null, Bold, _D, _NOT_SET)),
										"oct", new ThemeTrieElement(new ThemeTrieElementRule(3, null, Bold | Italic | Underline, _D,
												_NOT_SET)),
										"dec", new ThemeTrieElement(new ThemeTrieElementRule(3, null, None, _E, _NOT_SET)) //
								)))) //
				)));
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(14)
	@DisplayName("Theme resolving rules with parent scopes")
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
				StyleAttributes.of(None, _A, _B),
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
		assertThemeEqual(actual, expected);
	}

	@Test
	@Order(15)
	@DisplayName("Theme resolving issue #38: ignores rules with invalid colors")
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

		final var expected = list(
				new ParsedThemeRule("", null, 0, NotSet, "#cccccc", "#222222"),
				new ParsedThemeRule("variable", null, 1, None, null, null),
				new ParsedThemeRule("variable.parameter", null, 2, Italic, null, null),
				new ParsedThemeRule("support.other.variable", null, 3, None, null, null),
				new ParsedThemeRule("variable.other", null, 4, None, null, null),
				new ParsedThemeRule("variable.parameter.function.coffee", null, 5, Italic, "#F9D423", null));

		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	@Order(16)
	@DisplayName("Theme resolving issue #35: Trailing comma in a tmTheme scope selector")
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

		final var expected = list(
				new ParsedThemeRule("", null, 0, NotSet, "#EFEFEF", "#25292C"),
				new ParsedThemeRule("meta.at-rule.return.scss", null, 1, NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", list("meta.at-rule.return.scss"), 1, NotSet, "#CC7832", null),
				new ParsedThemeRule("meta.at-rule.else.scss", null, 1, NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", list("meta.at-rule.else.scss"), 1, NotSet, "#CC7832", null),
				new ParsedThemeRule("meta.at-rule.if.scss", null, 1, NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", list("meta.at-rule.if.scss"), 1, NotSet, "#CC7832", null));

		assertArrayEquals(expected.toArray(), actual.toArray());
	}
}
