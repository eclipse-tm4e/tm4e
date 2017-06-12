/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.theme;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm4e.core.internal.theme.reader.ThemeReader;
import org.eclipse.tm4e.core.internal.utils.CompareUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts
 *
 */
public class ThemeResolvingTest {

	@Test
	public void testStrcmpWorks() {
		List<String> actual = Arrays.asList("bar", "z", "zu", "a", "ab", "");
		actual.sort(CompareUtils::strcmp);

		List<String> expected = Arrays.asList("", "a", "ab", "bar", "z", "zu");
		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	public void testStrArrCmpWorks() {
		assertStrArrCmp("001", null, null, 0);
		assertStrArrCmp("002", null, Collections.emptyList(), -1);
		assertStrArrCmp("003", null, Arrays.asList("a"), -1);
		assertStrArrCmp("004", Collections.emptyList(), null, 1);
		assertStrArrCmp("005", Arrays.asList("a"), null, 1);
		assertStrArrCmp("006", Collections.emptyList(), Collections.emptyList(), 0);
		assertStrArrCmp("007", Collections.emptyList(), Arrays.asList("a"), -1);
		assertStrArrCmp("008", Arrays.asList("a"), Collections.emptyList(), 1);
		assertStrArrCmp("009", Arrays.asList("a"), Arrays.asList("a"), 0);
		assertStrArrCmp("010", Arrays.asList("a", "b"), Arrays.asList("a"), 1);
		assertStrArrCmp("011", Arrays.asList("a"), Arrays.asList("a", "b"), -1);
		assertStrArrCmp("012", Arrays.asList("a", "b"), Arrays.asList("a", "b"), 0);
		assertStrArrCmp("013", Arrays.asList("a", "b"), Arrays.asList("a", "c"), -1);
		assertStrArrCmp("014", Arrays.asList("a", "c"), Arrays.asList("a", "b"), 1);
	}

	@Test
	public void testAlwaysHasDefaults() {
		Theme actual = Theme.createFromParsedTheme(Collections.emptyList());
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#000000");
		int _B = colorMap.getId("#ffffff");
		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET)));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults1() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, null, null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#000000");
		int _B = colorMap.getId("#ffffff");
		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET)));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults2() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.None, null, null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#000000");
		int _B = colorMap.getId("#ffffff");
		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET)));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults3() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.Bold, null, null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#000000");
		int _B = colorMap.getId("#ffffff");
		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.Bold, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET)));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults4() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#ff0000", null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#ff0000");
		int _B = colorMap.getId("#ffffff");
		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET)));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRespectsIncomingDefaults5() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, null, "#ff0000"),
						new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#00ff00", null),
						new ParsedThemeRule("", null, -1, FontStyle.Bold, null, null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#00ff00");
		int _B = colorMap.getId("#ff0000");
		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.Bold, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET)));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testCanMergeIncomingDefaults() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, null, "#ff0000"),
						new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#00ff00", null),
						new ParsedThemeRule("", null, -1, FontStyle.Bold, null, null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#00ff00");
		int _B = colorMap.getId("#ff0000");
		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.Bold, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET)));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testDefaultsAreInherited() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
						new ParsedThemeRule("var", null, -1, FontStyle.NotSet, "#ff0000", null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#ff0000");

		Map<String, ThemeTrieElement> map = new HashMap<>();
		map.put("var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.NotSet, _C, _NOT_SET)));

		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET),
						Collections.emptyList(), map));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testSameRulesGetMerged() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
						new ParsedThemeRule("var", null, 1, FontStyle.Bold, null, null),
						new ParsedThemeRule("var", null, 0, FontStyle.NotSet, "#ff0000", null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#ff0000");

		Map<String, ThemeTrieElement> map = new HashMap<>();
		map.put("var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.Bold, _C, _NOT_SET)));

		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET),
						Collections.emptyList(), map));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRulesAreInherited1() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
						new ParsedThemeRule("var", null, -1, FontStyle.Bold, "#ff0000", null),
						new ParsedThemeRule("var.identifier", null, -1, FontStyle.NotSet, "#00ff00", null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#ff0000");
		int _D = colorMap.getId("#00ff00");

		Map<String, ThemeTrieElement> map1_1 = new HashMap<>();
		map1_1.put("identifier", new ThemeTrieElement(new ThemeTrieElementRule(2, null, FontStyle.Bold, _D, _NOT_SET)));
		Map<String, ThemeTrieElement> map1 = new HashMap<>();
		map1.put("var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.Bold, _C, _NOT_SET),
				Collections.emptyList(), map1_1));

		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET),
						Collections.emptyList(), map1));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRulesAreInherited2() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
						new ParsedThemeRule("var", null, -1, FontStyle.Bold, "#ff0000", null),
						new ParsedThemeRule("var.identifier", null, -1, FontStyle.NotSet, "#00ff00", null),
						new ParsedThemeRule("constant", null, 4, FontStyle.Italic, "#100000", null),
						new ParsedThemeRule("constant.numeric", null, 5, FontStyle.NotSet, "#200000", null),
						new ParsedThemeRule("constant.numeric.hex", null, 6, FontStyle.Bold, null, null),
						new ParsedThemeRule("constant.numeric.oct", null, 7,
								FontStyle.Bold | FontStyle.Italic | FontStyle.Underline, null, null),
						new ParsedThemeRule("constant.numeric.dec", null, 8, FontStyle.None, "#300000", null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#100000");
		int _D = colorMap.getId("#200000");
		int _E = colorMap.getId("#300000");
		int _F = colorMap.getId("#ff0000");
		int _G = colorMap.getId("#00ff00");

		Map<String, ThemeTrieElement> mapOfVar = new HashMap<>();
		mapOfVar.put("identifier",
				new ThemeTrieElement(new ThemeTrieElementRule(2, null, FontStyle.Bold, _G, _NOT_SET)));

		Map<String, ThemeTrieElement> mapOfNumeric = new HashMap<>();
		mapOfNumeric.put("hex", new ThemeTrieElement(new ThemeTrieElementRule(3, null, FontStyle.Bold, _D, _NOT_SET)));
		mapOfNumeric.put("oct", new ThemeTrieElement(new ThemeTrieElementRule(3, null,
				FontStyle.Bold | FontStyle.Italic | FontStyle.Underline, _D, _NOT_SET)));
		mapOfNumeric.put("dec", new ThemeTrieElement(new ThemeTrieElementRule(3, null, FontStyle.None, _E, _NOT_SET)));

		Map<String, ThemeTrieElement> mapOfConstant = new HashMap<>();
		mapOfConstant.put("numeric",
				new ThemeTrieElement(new ThemeTrieElementRule(2, null, FontStyle.Italic, _D, _NOT_SET),
						Collections.emptyList(), mapOfNumeric));

		Map<String, ThemeTrieElement> mapRoot = new HashMap<>();
		mapRoot.put("var", new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.Bold, _F, _NOT_SET),
				Collections.emptyList(), mapOfVar));
		mapRoot.put("constant", new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.Italic, _C, _NOT_SET),
				Collections.emptyList(), mapOfConstant));

		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET),
						Collections.emptyList(), mapRoot));
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRulesWithParentScopes() {
		Theme actual = Theme.createFromParsedTheme(
				new ArrayList<>(Arrays.asList(new ParsedThemeRule("", null, -1, FontStyle.NotSet, "#F8F8F2", "#272822"),
						new ParsedThemeRule("var", null, -1, FontStyle.Bold, "#100000", null),
						new ParsedThemeRule("var.identifier", null, -1, FontStyle.NotSet, "#200000", null),
						new ParsedThemeRule("var", Arrays.asList("source.css"), 1, FontStyle.Italic, "#300000", null),
						new ParsedThemeRule("var", Arrays.asList("source.css"), 2, FontStyle.Underline, null, null))));
		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#100000");
		int _D = colorMap.getId("#300000");
		int _E = colorMap.getId("#200000");

		Map<String, ThemeTrieElement> mapOfVar = new HashMap<>();
		mapOfVar.put("identifier",
				new ThemeTrieElement(new ThemeTrieElementRule(2, null, FontStyle.Bold, _E, _NOT_SET), Arrays.asList(
						new ThemeTrieElementRule(1, Arrays.asList("source.css"), FontStyle.Underline, _D, _NOT_SET))));

		Map<String, ThemeTrieElement> mapRoot = new HashMap<>();
		mapRoot.put("var",
				new ThemeTrieElement(new ThemeTrieElementRule(1, null, FontStyle.Bold, _C, _NOT_SET), Arrays.asList(
						new ThemeTrieElementRule(1, Arrays.asList("source.css"), FontStyle.Underline, _D, _NOT_SET)),
						mapOfVar));

		Theme expected = new Theme(colorMap, new ThemeTrieElementRule(0, null, FontStyle.None, _A, _B),
				new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET),
						Collections.emptyList(), mapRoot));
		Assert.assertEquals(actual, expected);
	}
	
	@Test
	public void testIssue_38_ignores_rules_with_invalid_colors() throws Exception {
		List<ParsedThemeRule> actual = parseTheme("{" +
			"\"settings\": [{"+
			"	\"settings\": {"+
			"		\"background\": \"#222222\","+
			"		\"foreground\": \"#cccccc\""+
			"	}"+
			"}, {"+
			"	\"name\": \"Variable\","+
			"	\"scope\": \"variable\","+
			"	\"settings\": {"+
			"		\"fontStyle\": \"\""+
			"	}"+
			"}, {"+
			"	\"name\": \"Function argument\","+
			"	\"scope\": \"variable.parameter\","+
			"	\"settings\": {"+
			"		\"fontStyle\": \"italic\","+
			"		\"foreground\": \"\""+
			"	}"+
			"}, {"+
			"	\"name\": \"Library variable\","+
			"	\"scope\": \"support.other.variable\","+
			"	\"settings\": {"+
			"		\"fontStyle\": \"\""+
			"	}"+
			"}, {"+
			"	\"name\": \"Function argument\","+
			"	\"scope\": \"variable.other\","+
			"	\"settings\": {"+
			"		\"foreground\": \"\","+
			"		\"fontStyle\": \"normal\""+
			"	}"+
			"}, {"+
			"	\"name\": \"Coffeescript Function argument\","+
			"	\"scope\": \"variable.parameter.function.coffee\","+
			"	\"settings\": {"+
			"		\"foreground\": \"#F9D423\","+
			"		\"fontStyle\": \"italic\""+
			"	}"+
			"}]"+
		"}");

		List<ParsedThemeRule> expected = Arrays.asList(
			new ParsedThemeRule("", null, 0, FontStyle.NotSet, "#cccccc", "#222222"),
			new ParsedThemeRule("variable", null, 1, FontStyle.None, null, null),
			new ParsedThemeRule("variable.parameter", null, 2, FontStyle.Italic, null, null),
			new ParsedThemeRule("support.other.variable", null, 3, FontStyle.None, null, null),
			new ParsedThemeRule("variable.other", null, 4, FontStyle.None, null, null),
			new ParsedThemeRule("variable.parameter.function.coffee", null, 5, FontStyle.Italic, "#F9D423", null)
		);

		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}
	
	@Test
	public void testIssue_35_Trailing_comma_in_a_tmTheme_scope_selector() throws Exception {
		List<ParsedThemeRule> actual = parseTheme("{" +
			"\"settings\": [{"+
			"	\"settings\": {"+
			"		\"background\": \"#25292C\","+
			"		\"foreground\": \"#EFEFEF\""+
			"	}"+
			"}, {"+
			"	\"name\": \"CSS at-rule keyword control\","+
			"	\"scope\": \""+
			"		meta.at-rule.return.scss,\n"+
			"		meta.at-rule.return.scss punctuation.definition,\n"+
			"		meta.at-rule.else.scss,\n"+
			"		meta.at-rule.else.scss punctuation.definition,\n"+
			"		meta.at-rule.if.scss,\n"+
			"		meta.at-rule.if.scss punctuation.definition\n"+
			"	\","+
			"	\"settings\": {"+
			"		\"foreground\": \"#CC7832\""+
			"	}"+
			"}]"+
		"}");

		List<ParsedThemeRule> expected = Arrays.asList(
				new ParsedThemeRule("", null, 0, FontStyle.NotSet, "#EFEFEF", "#25292C"),
				new ParsedThemeRule("meta.at-rule.return.scss", null, 1, FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", Arrays.asList("meta.at-rule.return.scss"), 1, FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("meta.at-rule.else.scss", null, 1, FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", Arrays.asList("meta.at-rule.else.scss"), 1, FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("meta.at-rule.if.scss", null, 1, FontStyle.NotSet, "#CC7832", null),
				new ParsedThemeRule("punctuation.definition", Arrays.asList("meta.at-rule.if.scss"), 1, FontStyle.NotSet, "#CC7832", null)
		);

		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}
	
	private void assertStrArrCmp(String testCase, List<String> a, List<String> b, int expected) {
		Assert.assertEquals(testCase, expected, CompareUtils.strArrCmp(a, b));
	}
	
	private List<ParsedThemeRule> parseTheme(String theme) throws Exception {
		return Theme.parseTheme(ThemeReader.JSON_PARSER.parse(new ByteArrayInputStream(theme.getBytes())));
	}
}
