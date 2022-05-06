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

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.tm4e.core.internal.theme.ColorMap;
import org.eclipse.tm4e.core.internal.theme.FontStyle;
import org.eclipse.tm4e.core.internal.theme.ParsedThemeRule;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.internal.theme.ThemeTrieElement;
import org.eclipse.tm4e.core.internal.theme.ThemeTrieElementRule;
import org.eclipse.tm4e.core.internal.theme.reader.ThemeReader;
import org.eclipse.tm4e.core.internal.utils.CompareUtils;
import org.junit.jupiter.api.Test;

/**
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts</a>
 */
public class ThemeResolvingTest {

	private static final int NOT_SET = 0;
	private static final ThemeTrieElementRule NOTSET_THEME_TRIE_ELEMENT_RULE = new ThemeTrieElementRule(0, null,
			FontStyle.NotSet, NOT_SET, NOT_SET);
	private static final ThemeTrieElement NOTSET_THEME_TRIE_ELEMENT = new ThemeTrieElement(
			NOTSET_THEME_TRIE_ELEMENT_RULE);

	private static void assertStrArrCmp(String testCase, List<String> a, List<String> b, int expected) {
		assertEquals(expected, CompareUtils.strArrCmp(a, b), testCase);
	}

	private static Theme createTheme(ParsedThemeRule... rules) {
		return Theme.createFromParsedTheme(List.of(rules), null);
	}

	private static List<ParsedThemeRule> parseTheme(String theme) throws Exception {
		return Theme.parseTheme(ThemeReader.readThemeSync("theme.json", new ByteArrayInputStream(theme.getBytes())));
	}

	@Test
	public void testThemeParsingCanParse() throws Exception {
		var actual = parseTheme(("{" +
				"'settings': [" +
				"{ 'settings': { 'foreground': '#F8F8F2', 'background': '#272822' } }," +
				"{ 'scope': 'source, something', 'settings': { 'background': '#100000' } }," +
				"{ 'scope': ['bar', 'baz'], 'settings': { 'background': '#010000' } }," +
				"{ 'scope': 'source.css selector bar', 'settings': { 'fontStyle': 'bold' } }," +
				"{ 'scope': 'constant', 'settings': { 'fontStyle': 'italic', 'foreground': '#ff0000' } }," +
				"{ 'scope': 'constant.numeric', 'settings': { 'foreground': '#00ff00' } }," +
				"{ 'scope': 'constant.numeric.hex', 'settings': { 'fontStyle': 'bold' } }," +
				"{ 'scope': 'constant.numeric.oct', 'settings': { 'fontStyle': 'bold italic underline' } }," +
				"{ 'scope': 'constant.numeric.bin', 'settings': { 'fontStyle': 'bold strikethrough' } }," +
				"{ 'scope': 'constant.numeric.dec', 'settings': { 'fontStyle': '', 'foreground': '#0000ff' } }," +
				"{ 'scope': 'foo', 'settings': { 'fontStyle': '', 'foreground': '#CFA' } }" +
				"]" +
				"}").replace('\'', '"'));

		var expected = List.of(
				new ParsedThemeRule("", null, 0, FontStyle.NotSet, "#F8F8F2", "#272822"),
				new ParsedThemeRule("source", null, 1, FontStyle.NotSet, null, "#100000"),
				new ParsedThemeRule("something", null, 1, FontStyle.NotSet, null, "#100000"),
				new ParsedThemeRule("bar", null, 2, FontStyle.NotSet, null, "#010000"),
				new ParsedThemeRule("baz", null, 2, FontStyle.NotSet, null, "#010000"),
				new ParsedThemeRule("bar", List.of("selector", "source.css"), 3, FontStyle.Bold, null, null),
				new ParsedThemeRule("constant", null, 4, FontStyle.Italic, "#ff0000", null),
				new ParsedThemeRule("constant.numeric", null, 5, FontStyle.NotSet, "#00ff00", null),
				new ParsedThemeRule("constant.numeric.hex", null, 6, FontStyle.Bold, null, null),
				new ParsedThemeRule("constant.numeric.oct", null, 7,
						FontStyle.Bold | FontStyle.Italic | FontStyle.Underline, null, null),
				new ParsedThemeRule("constant.numeric.bin", null, 8, FontStyle.Bold | FontStyle.Strikethrough, null,
						null),
				new ParsedThemeRule("constant.numeric.dec", null, 9, FontStyle.None, "#0000ff", null),
				new ParsedThemeRule("foo", null, 10, FontStyle.None, "#CFA", null));

		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	public void testStrcmpWorks() {
		var actual = Arrays.asList("bar", "z", "zu", "a", "ab", "");
		actual.sort(CompareUtils::strcmp);

		var expected = List.of("", "a", "ab", "bar", "z", "zu");
		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	public void testStrArrCmpWorks() {
		assertStrArrCmp("001", null, null, 0);
		assertStrArrCmp("002", null, Collections.emptyList(), -1);
		assertStrArrCmp("003", null, List.of("a"), -1);
		assertStrArrCmp("004", Collections.emptyList(), null, 1);
		assertStrArrCmp("005", List.of("a"), null, 1);
		assertStrArrCmp("006", Collections.emptyList(), Collections.emptyList(), 0);
		assertStrArrCmp("007", Collections.emptyList(), List.of("a"), -1);
		assertStrArrCmp("008", List.of("a"), Collections.emptyList(), 1);
		assertStrArrCmp("009", List.of("a"), List.of("a"), 0);
		assertStrArrCmp("010", List.of("a", "b"), List.of("a"), 1);
		assertStrArrCmp("011", List.of("a"), List.of("a", "b"), -1);
		assertStrArrCmp("012", List.of("a", "b"), List.of("a", "b"), 0);
		assertStrArrCmp("013", List.of("a", "b"), List.of("a", "c"), -1);
		assertStrArrCmp("014", List.of("a", "c"), List.of("a", "b"), 1);
	}

	@Test
	public void testAlwaysHasDefaults() {
		var actual = createTheme();
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#000000");
		int _B = colorMap.getId("#ffffff");
		var expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B), NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults1() {
		var actual = createTheme(new ParsedThemeRule("", null, -1, FontStyle.NotSet, null, null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#000000");
		int _B = colorMap.getId("#ffffff");
		var expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B), NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults2() {
		Theme actual = createTheme(new ParsedThemeRule("", null, -1, FontStyle.None, null, null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#000000");
		int _B = colorMap.getId("#ffffff");
		Theme expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B), NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults3() {
		var actual = createTheme(new ParsedThemeRule("", null, -1, FontStyle.Bold, null, null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#000000");
		int _B = colorMap.getId("#ffffff");
		var expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.Bold, _A, _B), NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults4() {
		var actual = createTheme(new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#ff0000", null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#ff0000");
		int _B = colorMap.getId("#ffffff");
		var expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults5() {
		var actual = createTheme(
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, null, "#ff0000"),
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#00ff00", null),
				new ParsedThemeRule("", null, -1, FontStyle.Bold, null, null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#00ff00");
		int _B = colorMap.getId("#ff0000");
		var expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.Bold, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	@Test
	public void testCanMergeIncomingDefaults() {
		var actual = createTheme(
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, null, "#ff0000"),
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#00ff00", null),
				new ParsedThemeRule("", null, -1, FontStyle.Bold, null, null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#00ff00");
		int _B = colorMap.getId("#ff0000");
		var expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.Bold, _A, _B),
				NOTSET_THEME_TRIE_ELEMENT);
		assertEquals(actual, expected);
	}

	@Test
	public void testDefaultsAreInherited() {
		Theme actual = createTheme(
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
				new ParsedThemeRule("var", null, -1, FontStyle.NotSet, "#ff0000", null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#ff0000");

		var map = new HashMap<String, ThemeTrieElement>();
		map.put("var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.NotSet, _C, NOT_SET)));

		Theme expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, Collections.emptyList(), map));
		assertEquals(actual, expected);
	}

	@Test
	public void testSameRulesGetMerged() {
		var actual = createTheme(
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
				new ParsedThemeRule("var", null, 1, FontStyle.Bold, null, null),
				new ParsedThemeRule("var", null, 0, FontStyle.NotSet, "#ff0000", null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#ff0000");

		var map = new HashMap<String, ThemeTrieElement>();
		map.put("var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.Bold, _C, NOT_SET)));

		var expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, Collections.emptyList(), map));
		assertEquals(actual, expected);
	}

	@Test
	public void testRulesAreInherited1() {
		var actual = createTheme(
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
				new ParsedThemeRule("var", null, -1, FontStyle.Bold, "#ff0000", null),
				new ParsedThemeRule("var.identifier", null, -1, FontStyle.NotSet, "#00ff00", null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#ff0000");
		int _D = colorMap.getId("#00ff00");

		var map1_1 = new HashMap<String, ThemeTrieElement>();
		map1_1.put("identifier", new ThemeTrieElement(new ThemeTrieElementRule(2, null, FontStyle.Bold, _D, NOT_SET)));
		var map1 = new HashMap<String, ThemeTrieElement>();
		map1.put("var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.Bold, _C, NOT_SET),
				Collections.emptyList(), map1_1));

		var expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, Collections.emptyList(), map1));
		assertEquals(actual, expected);
	}

	@Test
	public void testRulesAreInherited2() {
		var actual = createTheme(
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
				new ParsedThemeRule("var", null, -1, FontStyle.Bold, "#ff0000", null),
				new ParsedThemeRule("var.identifier", null, -1, FontStyle.NotSet, "#00ff00", null),
				new ParsedThemeRule("constant", null, 4, FontStyle.Italic, "#100000", null),
				new ParsedThemeRule("constant.numeric", null, 5, FontStyle.NotSet, "#200000", null),
				new ParsedThemeRule("constant.numeric.hex", null, 6, FontStyle.Bold, null, null),
				new ParsedThemeRule("constant.numeric.oct", null, 7,
						FontStyle.Bold | FontStyle.Italic | FontStyle.Underline, null, null),
				new ParsedThemeRule("constant.numeric.dec", null, 8, FontStyle.None, "#300000", null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#100000");
		int _D = colorMap.getId("#200000");
		int _E = colorMap.getId("#300000");
		int _F = colorMap.getId("#ff0000");
		int _G = colorMap.getId("#00ff00");

		var mapOfVar = new HashMap<String, ThemeTrieElement>();
		mapOfVar.put("identifier",
				new ThemeTrieElement(new ThemeTrieElementRule(2, null, FontStyle.Bold, _G, NOT_SET)));

		var mapOfNumeric = new HashMap<String, ThemeTrieElement>();
		mapOfNumeric.put("hex", new ThemeTrieElement(new ThemeTrieElementRule(3, null, FontStyle.Bold, _D, NOT_SET)));
		mapOfNumeric.put("oct", new ThemeTrieElement(new ThemeTrieElementRule(3, null,
				FontStyle.Bold | FontStyle.Italic | FontStyle.Underline, _D, NOT_SET)));
		mapOfNumeric.put("dec", new ThemeTrieElement(new ThemeTrieElementRule(3, null, FontStyle.None, _E, NOT_SET)));

		var mapOfConstant = new HashMap<String, ThemeTrieElement>();
		mapOfConstant.put("numeric", new ThemeTrieElement(new ThemeTrieElementRule(2, null,
				FontStyle.Italic, _D, NOT_SET), Collections.emptyList(), mapOfNumeric));

		var mapRoot = new HashMap<String, ThemeTrieElement>();
		mapRoot.put("var", new ThemeTrieElement(new ThemeTrieElementRule(1, null,
				FontStyle.Bold, _F, NOT_SET), Collections.emptyList(), mapOfVar));
		mapRoot.put("constant", new ThemeTrieElement(new ThemeTrieElementRule(1, null,
				FontStyle.Italic, _C, NOT_SET), Collections.emptyList(), mapOfConstant));

		var expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, Collections.emptyList(), mapRoot));
		assertEquals(actual, expected);
	}

	@Test
	public void testRulesWithParentScopes() {
		var actual = createTheme(
				new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
				new ParsedThemeRule("var", null, -1, FontStyle.Bold, "#100000", null),
				new ParsedThemeRule("var.identifier", null, -1, FontStyle.NotSet, "#200000", null),
				new ParsedThemeRule("var", List.of("source.css"), 1, FontStyle.Italic, "#300000", null),
				new ParsedThemeRule("var", List.of("source.css"), 2, FontStyle.Underline, null, null));
		var colorMap = new ColorMap();
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#100000");
		int _D = colorMap.getId("#300000");
		int _E = colorMap.getId("#200000");

		var mapOfVar = new HashMap<String, ThemeTrieElement>();
		mapOfVar.put("identifier",
				new ThemeTrieElement(new ThemeTrieElementRule(2, null, FontStyle.Bold, _E, NOT_SET), List.of(
						new ThemeTrieElementRule(1, List.of("source.css"), FontStyle.Underline, _D, NOT_SET))));

		var mapRoot = new HashMap<String, ThemeTrieElement>();
		mapRoot.put("var",
				new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.Bold, _C, NOT_SET), List.of(
						new ThemeTrieElementRule(1, List.of("source.css"), FontStyle.Underline, _D, NOT_SET)),
						mapOfVar));

		var expected = new Theme(colorMap,
				new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(NOTSET_THEME_TRIE_ELEMENT_RULE, Collections.emptyList(), mapRoot));
		assertEquals(actual, expected);
	}

	@Test
	public void testIssue_38_ignores_rules_with_invalid_colors() throws Exception {
		var actual = parseTheme(("{" +
				"'settings': [{" +
				"	'settings': {" +
				"		'background': '#222222'," +
				"		'foreground': '#cccccc'" +
				"	}" +
				"}, {" +
				"	'name': 'Variable'," +
				"	'scope': 'variable'," +
				"	'settings': {" +
				"		'fontStyle': ''" +
				"	}" +
				"}, {" +
				"	'name': 'Function argument'," +
				"	'scope': 'variable.parameter'," +
				"	'settings': {" +
				"		'fontStyle': 'italic'," +
				"		'foreground': ''" +
				"	}" +
				"}, {" +
				"	'name': 'Library variable'," +
				"	'scope': 'support.other.variable'," +
				"	'settings': {" +
				"		'fontStyle': ''" +
				"	}" +
				"}, {" +
				"	'name': 'Function argument'," +
				"	'scope': 'variable.other'," +
				"	'settings': {" +
				"		'foreground': ''," +
				"		'fontStyle': 'normal'" +
				"	}" +
				"}, {" +
				"	'name': 'Coffeescript Function argument'," +
				"	'scope': 'variable.parameter.function.coffee'," +
				"	'settings': {" +
				"		'foreground': '#F9D423'," +
				"		'fontStyle': 'italic'" +
				"	}" +
				"}]" +
				"}").replace('\'', '"'));

		var expected = List.of(
				new ParsedThemeRule("", null, 0, FontStyle.NotSet, "#cccccc", "#222222"),
				new ParsedThemeRule("variable", null, 1, FontStyle.None, null, null),
				new ParsedThemeRule("variable.parameter", null, 2, FontStyle.Italic, null, null),
				new ParsedThemeRule("support.other.variable", null, 3, FontStyle.None, null, null),
				new ParsedThemeRule("variable.other", null, 4, FontStyle.None, null, null),
				new ParsedThemeRule("variable.parameter.function.coffee", null, 5, FontStyle.Italic, "#F9D423", null));

		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	public void testIssue_35_Trailing_comma_in_a_tmTheme_scope_selector() throws Exception {
		var actual = parseTheme(("{" +
				"'settings': [{" +
				"	'settings': {" +
				"		'background': '#25292C'," +
				"		'foreground': '#EFEFEF'" +
				"	}" +
				"}, {" +
				"	'name': 'CSS at-rule keyword control'," +
				"	'scope': '" +
				"		meta.at-rule.return.scss,\n" +
				"		meta.at-rule.return.scss punctuation.definition,\n" +
				"		meta.at-rule.else.scss,\n" +
				"		meta.at-rule.else.scss punctuation.definition,\n" +
				"		meta.at-rule.if.scss,\n" +
				"		meta.at-rule.if.scss punctuation.definition\n" +
				"	'," +
				"	'settings': {" +
				"		'foreground': '#CC7832'" +
				"	}" +
				"}]" +
				"}").replace('\'', '"'));

		var expected = List.of(
				new ParsedThemeRule("", null, 0, FontStyle.NotSet, "#EFEFEF", "#25292C"),
				new ParsedThemeRule("meta.at-rule.return.scss", null, 1, FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", List.of("meta.at-rule.return.scss"), 1,
						FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("meta.at-rule.else.scss", null, 1, FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", List.of("meta.at-rule.else.scss"), 1,
						FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("meta.at-rule.if.scss", null, 1, FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", List.of("meta.at-rule.if.scss"), 1,
						FontStyle.NotSet, "#CC7832", null));

		assertArrayEquals(expected.toArray(), actual.toArray());
	}
}
