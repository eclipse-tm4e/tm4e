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
package org.eclipse.tm4e.core.theme;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.tm4e.core.internal.grammar.ScopeListElement;
import org.eclipse.tm4e.core.internal.grammar.ScopeMetadata;
import org.eclipse.tm4e.core.internal.grammar.StackElementMetadata;
import org.eclipse.tm4e.core.internal.theme.reader.ThemeReader;
import org.junit.jupiter.api.Test;

/**
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts</a>
 *
 */
class ThemeMatchingTest {

	@Test
	void testGivesHigherPriorityToDeeperMatches() throws Exception {
		Theme theme = loadTheme("{" +
			"\"settings\": ["+
						"{ \"settings\": { \"foreground\": \"#100000\", \"background\": \"#200000\" } },"+
						"{ \"scope\": \"punctuation.definition.string.begin.html\", \"settings\": { \"foreground\": \"#300000\" } },"+
						"{ \"scope\": \"meta.tag punctuation.definition.string\", \"settings\": { \"foreground\": \"#400000\" } }"+
			"]"+
		"}");


		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#100000");
		int _B = colorMap.getId("#200000");
		int _C = colorMap.getId("#400000");
		int _D = colorMap.getId("#300000");

		List<ThemeTrieElementRule> actual = theme.match("punctuation.definition.string.begin.html");

		assertArrayEquals(
				new ThemeTrieElementRule[] { new ThemeTrieElementRule(5, null, FontStyle.NotSet, _D, _NOT_SET),
						new ThemeTrieElementRule(3, Arrays.asList("meta.tag"), FontStyle.NotSet, _C, _NOT_SET) },
				actual.toArray());
	}

	@Test
	void testGivesHigherPriorityToParentMatches1() throws Exception {
		Theme theme = loadTheme("{" +
			"\"settings\": ["+
						"{ \"settings\": { \"foreground\": \"#100000\", \"background\": \"#200000\" } },"+
						"{ \"scope\": \"c a\", \"settings\": { \"foreground\": \"#300000\" } },"+
						"{ \"scope\": \"d a.b\", \"settings\": { \"foreground\": \"#400000\" } },"+
						"{ \"scope\": \"a\", \"settings\": { \"foreground\": \"#500000\" } }"+
			"]"+
		"}");


		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#100000");
		int _B = colorMap.getId("#200000");
		int _C = colorMap.getId("#500000");
		int _D = colorMap.getId("#300000");
		int _E = colorMap.getId("#400000");

		List<ThemeTrieElementRule> actual = theme.match("a.b");

		assertArrayEquals(new ThemeTrieElementRule[] {
				new ThemeTrieElementRule(2, Arrays.asList("d"), FontStyle.NotSet, _E, _NOT_SET),
				new ThemeTrieElementRule(1, Arrays.asList("c"), FontStyle.NotSet, _D, _NOT_SET),
				new ThemeTrieElementRule(1, null, FontStyle.NotSet, _C, _NOT_SET) }, actual.toArray());
	}

	@Test
	void testGivesHigherPriorityToParentMatches2() throws Exception {
		Theme theme = loadTheme("{" +
			"\"settings\": ["+
						"{ \"settings\": { \"foreground\": \"#100000\", \"background\": \"#200000\" } },"+
						"{ \"scope\": \"meta.tag entity\", \"settings\": { \"foreground\": \"#300000\" } },"+
						"{ \"scope\": \"meta.selector.css entity.name.tag\", \"settings\": { \"foreground\": \"#400000\" } },"+
						"{ \"scope\": \"entity\", \"settings\": { \"foreground\": \"#500000\" } }"+
			"]"+
		"}");


		ScopeListElement root = new ScopeListElement(null, "text.html.cshtml", 0);
		ScopeListElement parent = new ScopeListElement(root, "meta.tag.structure.any.html", 0);
		int r = ScopeListElement.mergeMetadata(0, parent, new ScopeMetadata("entity.name.tag.structure.any.html", 0, 0,
				theme.match("entity.name.tag.structure.any.html")));
		String color = theme.getColor(StackElementMetadata.getForeground(r));
		assertEquals("#300000", color);
	}

	@Test
	void testCanMatch() throws Exception {
		Theme theme = loadTheme("{" +
			"\"settings\": ["+
				"{ \"settings\": { \"foreground\": \"#F8F8F2\", \"background\": \"#272822\" } },"+
				"{ \"scope\": \"source, something\", \"settings\": { \"background\": \"#100000\" } },"+
				"{ \"scope\": [\"bar\", \"baz\"], \"settings\": { \"background\": \"#200000\" } },"+
				"{ \"scope\": \"source.css selector bar\", \"settings\": { \"fontStyle\": \"bold\" } },"+
				"{ \"scope\": \"constant\", \"settings\": { \"fontStyle\": \"italic\", \"foreground\": \"#300000\" } },"+
				"{ \"scope\": \"constant.numeric\", \"settings\": { \"foreground\": \"#400000\" } },"+
				"{ \"scope\": \"constant.numeric.hex\", \"settings\": { \"fontStyle\": \"bold\" } },"+
				"{ \"scope\": \"constant.numeric.oct\", \"settings\": { \"fontStyle\": \"bold italic underline\" } },"+
				"{ \"scope\": \"constant.numeric.dec\", \"settings\": { \"fontStyle\": \"\", \"foreground\": \"#500000\" } },"+
				"{ \"scope\": \"storage.object.bar\", \"settings\": { \"fontStyle\": \"\", \"foreground\": \"#600000\" } }"+
			"]"+
		"}");

		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#F8F8F2");
		int _B = colorMap.getId("#272822");
		int _C = colorMap.getId("#200000");
		int _D = colorMap.getId("#300000");
		int _E = colorMap.getId("#400000");
		int _F = colorMap.getId("#500000");
		int _G = colorMap.getId("#100000");
		int _H = colorMap.getId("#600000");

		// matches defaults
		assertNoMatch(theme, "");
		assertNoMatch(theme, "bazz");
		assertNoMatch(theme, "asdfg");

		// matches source
		assertSimpleMatch(theme, "source", 1, FontStyle.NotSet, _NOT_SET, _G);
		assertSimpleMatch(theme, "source.ts", 1, FontStyle.NotSet, _NOT_SET, _G);
		assertSimpleMatch(theme, "source.tss", 1, FontStyle.NotSet, _NOT_SET, _G);

		// matches something
		assertSimpleMatch(theme, "something", 1, FontStyle.NotSet, _NOT_SET, _G);
		assertSimpleMatch(theme, "something.ts", 1, FontStyle.NotSet, _NOT_SET, _G);
		assertSimpleMatch(theme, "something.tss", 1, FontStyle.NotSet, _NOT_SET, _G);

		// matches baz
		assertSimpleMatch(theme, "baz", 1, FontStyle.NotSet, _NOT_SET, _C);
		assertSimpleMatch(theme, "baz.ts", 1, FontStyle.NotSet, _NOT_SET, _C);
		assertSimpleMatch(theme, "baz.tss", 1, FontStyle.NotSet, _NOT_SET, _C);

		// matches constant
		assertSimpleMatch(theme, "constant", 1, FontStyle.Italic, _D, _NOT_SET);
		assertSimpleMatch(theme, "constant.string", 1, FontStyle.Italic, _D, _NOT_SET);
		assertSimpleMatch(theme, "constant.hex", 1, FontStyle.Italic, _D, _NOT_SET);

		// matches constant.numeric
		assertSimpleMatch(theme, "constant.numeric", 2, FontStyle.Italic, _E, _NOT_SET);
		assertSimpleMatch(theme, "constant.numeric.baz", 2, FontStyle.Italic, _E, _NOT_SET);

		// matches constant.numeric.hex
		assertSimpleMatch(theme, "constant.numeric.hex", 3, FontStyle.Bold, _E, _NOT_SET);
		assertSimpleMatch(theme, "constant.numeric.hex.baz", 3, FontStyle.Bold, _E, _NOT_SET);

		// matches constant.numeric.oct
		assertSimpleMatch(theme, "constant.numeric.oct", 3, FontStyle.Bold | FontStyle.Italic | FontStyle.Underline, _E,
				_NOT_SET);
		assertSimpleMatch(theme, "constant.numeric.oct.baz", 3, FontStyle.Bold | FontStyle.Italic | FontStyle.Underline,
				_E, _NOT_SET);

		// matches constant.numeric.dec
		assertSimpleMatch(theme, "constant.numeric.dec", 3, FontStyle.None, _F, _NOT_SET);
		assertSimpleMatch(theme, "constant.numeric.dec.baz", 3, FontStyle.None, _F, _NOT_SET);

		// matches storage.object.bar
		assertSimpleMatch(theme, "storage.object.bar", 3, FontStyle.None, _H, _NOT_SET);
		assertSimpleMatch(theme, "storage.object.bar.baz", 3, FontStyle.None, _H, _NOT_SET);

		// does not match storage.object.bar
		assertSimpleMatch(theme, "storage.object.bart", 0, FontStyle.NotSet, _NOT_SET, _NOT_SET);
		assertSimpleMatch(theme, "storage.object", 0, FontStyle.NotSet, _NOT_SET, _NOT_SET);
		assertSimpleMatch(theme, "storage", 0, FontStyle.NotSet, _NOT_SET, _NOT_SET);

		assertMatch(theme, "bar",
				new ThemeTrieElementRule[] { new ThemeTrieElementRule(1, Arrays.asList("selector", "source.css"),
						FontStyle.Bold, _NOT_SET, _C),
						new ThemeTrieElementRule(1, null, FontStyle.NotSet, _NOT_SET, _C) });
	}

	@Test
	void testMicrosoft_vscode_23460() throws Exception {
		Theme theme = loadTheme("{" +
			"\"settings\": ["+
				"{" +
					"\"settings\": {"+
						"\"foreground\": \"#aec2e0\","+
						"\"background\": \"#14191f\""+
					"}"+
				"}, {"+
					"\"name\": \"JSON String\","+
					"\"scope\": \"meta.structure.dictionary.json string.quoted.double.json\","+
					"\"settings\": {"+
						"\"foreground\": \"#FF410D\""+
					"}"+
				"}, {"+
					"\"scope\": \"meta.structure.dictionary.json string.quoted.double.json\","+
					"\"settings\": {"+
						"\"foreground\": \"#ffffff\""+
					"}"+
				"},"+
				"{"+
					"\"scope\": \"meta.structure.dictionary.value.json string.quoted.double.json\","+
					"\"settings\": {"+
						"\"foreground\": \"#FF410D\""+
					"}"+
				"}"+
			"]"+
		"}");

		ColorMap colorMap = new ColorMap();
		int _NOT_SET = 0;
		int _A = colorMap.getId("#aec2e0");
		int _B = colorMap.getId("#14191f");
		int _C = colorMap.getId("#FF410D");
		int _D = colorMap.getId("#ffffff");

		// string.quoted.double.json
		// meta.structure.dictionary.value.json
		// meta.structure.dictionary.json
		// source.json
		assertMatch(theme, "string.quoted.double.json", new ThemeTrieElementRule[] {
			new ThemeTrieElementRule(4, Arrays.asList("meta.structure.dictionary.value.json"), FontStyle.NotSet, _C, _NOT_SET),
			new ThemeTrieElementRule(4, Arrays.asList("meta.structure.dictionary.json"), FontStyle.NotSet, _D, _NOT_SET),
			new ThemeTrieElementRule(0, null, FontStyle.NotSet, _NOT_SET, _NOT_SET)
		});

		ScopeListElement parent3 = new ScopeListElement(null, "source.json", 0);
		ScopeListElement parent2 = new ScopeListElement(parent3, "meta.structure.dictionary.json", 0);
		ScopeListElement parent1 = new ScopeListElement(parent2, "meta.structure.dictionary.value.json", 0);

		int r = ScopeListElement.mergeMetadata(
			0,
			parent1,
			new ScopeMetadata("string.quoted.double.json", 0, 0, theme.match("string.quoted.double.json"))
		);
		String color = theme.getColor(StackElementMetadata.getForeground(r));
		assertEquals("#FF410D", color);
	}

	private void assertMatch(Theme theme, String scopeName, ThemeTrieElementRule[] expected) {
		List<ThemeTrieElementRule> actual = theme.match(scopeName);
		assertArrayEquals(expected, actual.toArray(), "when matching <<" + scopeName + ">>");
	}

	private void assertSimpleMatch(Theme theme, String scopeName, int scopeDepth, int fontStyle, int foreground, int background) {
		assertMatch(theme, scopeName, new ThemeTrieElementRule [] {
			new ThemeTrieElementRule(scopeDepth, null, fontStyle, foreground, background)
		});
	}

	private void assertNoMatch(Theme theme, String scopeName) {
		assertMatch(theme, scopeName, new ThemeTrieElementRule [] {
			new ThemeTrieElementRule(0, null, FontStyle.NotSet, 0, 0 /*_NOT_SET, _NOT_SET*/)
		});
	}

	private Theme loadTheme(String theme) throws Exception {
		return Theme.createFromRawTheme(ThemeReader.readThemeSync("theme.json", new ByteArrayInputStream(theme.getBytes())));
	}
}
