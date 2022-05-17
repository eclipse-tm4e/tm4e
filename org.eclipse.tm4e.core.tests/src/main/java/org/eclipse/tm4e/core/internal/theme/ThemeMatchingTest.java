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

import org.eclipse.tm4e.core.internal.grammar.AttributedScopeStack;
import org.eclipse.tm4e.core.internal.grammar.BasicScopeAttributes;
import org.eclipse.tm4e.core.internal.grammar.tokenattrs.EncodedTokenAttributes;
import org.junit.jupiter.api.Test;

/**
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts</a>
 */
class ThemeMatchingTest extends AbstractThemeTest {

	/**
	 * test: Theme matching gives higher priority to deeper matches
	 */
	@Test
	void testGivesHigherPriorityToDeeperMatches() throws Exception {
		final Theme theme = createTheme("""
			{"settings": [
				{ "settings": { "foreground": "#100000", "background": "#200000" } },
				{ "scope": "punctuation.definition.string.begin.html", "settings": { "foreground": "#300000" } },
				{ "scope": "meta.tag punctuation.definition.string", "settings": { "foreground": "#400000" } }
			]}""");

		final ColorMap colorMap = new ColorMap();
		final int _A = colorMap.getId("#100000");
		final int _B = colorMap.getId("#200000");
		final int _C = colorMap.getId("#400000");
		final int _D = colorMap.getId("#300000");

		assertMatch(theme, "punctuation.definition.string.begin.html",
			new ThemeTrieElementRule(5, null, NotSet, _D, _NOT_SET),
			new ThemeTrieElementRule(3, list("meta.tag"), NotSet, _C, _NOT_SET));
	}

	/**
	 * test: Theme matching gives higher priority to parent matches 1
	 */
	@Test
	void testGivesHigherPriorityToParentMatches1() throws Exception {
		final Theme theme = createTheme("""
			{"settings": [
				{ "settings": { "foreground": "#100000", "background": "#200000" } },
				{ "scope": "c a", "settings": { "foreground": "#300000" } },
				{ "scope": "d a.b", "settings": { "foreground": "#400000" } },
				{ "scope": "a", "settings": { "foreground": "#500000" } }
			]}""");

		final ColorMap colorMap = new ColorMap();
		final int _A = colorMap.getId("#100000");
		final int _B = colorMap.getId("#200000");
		final int _C = colorMap.getId("#500000");
		final int _D = colorMap.getId("#300000");
		final int _E = colorMap.getId("#400000");

		assertMatch(theme, "a.b",
			new ThemeTrieElementRule(2, list("d"), NotSet, _E, _NOT_SET),
			new ThemeTrieElementRule(1, list("c"), NotSet, _D, _NOT_SET),
			new ThemeTrieElementRule(1, null, NotSet, _C, _NOT_SET));
	}

	/**
	 * test: Theme matching gives higher priority to parent matches 2
	 */
	@Test
	void testGivesHigherPriorityToParentMatches2() throws Exception {
		final Theme theme = createTheme("""
			{"settings": [
				{ "settings": { "foreground": "#100000", "background": "#200000" } },
				{ "scope": "meta.tag entity", "settings": { "foreground": "#300000" } },
				{ "scope": "meta.selector.css entity.name.tag", "settings": { "foreground": "#400000" } },
				{ "scope": "entity", "settings": { "foreground": "#500000" } }
			]}""");

		final var root = new AttributedScopeStack(null, "text.html.cshtml", 0);
		final var parent = new AttributedScopeStack(root, "meta.tag.structure.any.html", 0);
		final int r = AttributedScopeStack.mergeMetadata(0, parent,
			new BasicScopeAttributes("entity.name.tag.structure.any.html", 0, 0,
				theme.match("entity.name.tag.structure.any.html")));
		final String color = theme.getColor(EncodedTokenAttributes.getForeground(r));
		assertEquals("#300000", color);
	}

	/**
	 * test: Theme matching can match
	 */
	@Test
	void testCanMatch() throws Exception {
		final Theme theme = createTheme("""
			{"settings": [
				{ "settings": { "foreground": "#F8F8F2", "background": "#272822" } },
				{ "scope": "source, something", "settings": { "background": "#100000" } },
				{ "scope": ["bar", "baz"], "settings": { "background": "#200000" } },
				{ "scope": "source.css selector bar", "settings": { "fontStyle": "bold" } },
				{ "scope": "constant", "settings": { "fontStyle": "italic", "foreground": "#300000" } },
				{ "scope": "constant.numeric", "settings": { "foreground": "#400000" } },
				{ "scope": "constant.numeric.hex", "settings": { "fontStyle": "bold" } },
				{ "scope": "constant.numeric.oct", "settings": { "fontStyle": "bold italic underline" } },
				{ "scope": "constant.numeric.dec", "settings": { "fontStyle": "", "foreground": "#500000" } },
				{ "scope": "storage.object.bar", "settings": { "fontStyle": "", "foreground": "#600000" } }
			]}""");

		final ColorMap colorMap = new ColorMap();
		final int _A = colorMap.getId("#F8F8F2");
		final int _B = colorMap.getId("#272822");
		final int _C = colorMap.getId("#200000");
		final int _D = colorMap.getId("#300000");
		final int _E = colorMap.getId("#400000");
		final int _F = colorMap.getId("#500000");
		final int _G = colorMap.getId("#100000");
		final int _H = colorMap.getId("#600000");

		// matches defaults
		assertNoMatch(theme, "");
		assertNoMatch(theme, "bazz");
		assertNoMatch(theme, "asdfg");

		// matches source
		assertSimpleMatch(theme, "source", 1, NotSet, _NOT_SET, _G);
		assertSimpleMatch(theme, "source.ts", 1, NotSet, _NOT_SET, _G);
		assertSimpleMatch(theme, "source.tss", 1, NotSet, _NOT_SET, _G);

		// matches something
		assertSimpleMatch(theme, "something", 1, NotSet, _NOT_SET, _G);
		assertSimpleMatch(theme, "something.ts", 1, NotSet, _NOT_SET, _G);
		assertSimpleMatch(theme, "something.tss", 1, NotSet, _NOT_SET, _G);

		// matches baz
		assertSimpleMatch(theme, "baz", 1, NotSet, _NOT_SET, _C);
		assertSimpleMatch(theme, "baz.ts", 1, NotSet, _NOT_SET, _C);
		assertSimpleMatch(theme, "baz.tss", 1, NotSet, _NOT_SET, _C);

		// matches constant
		assertSimpleMatch(theme, "constant", 1, Italic, _D, _NOT_SET);
		assertSimpleMatch(theme, "constant.string", 1, Italic, _D, _NOT_SET);
		assertSimpleMatch(theme, "constant.hex", 1, Italic, _D, _NOT_SET);

		// matches constant.numeric
		assertSimpleMatch(theme, "constant.numeric", 2, Italic, _E, _NOT_SET);
		assertSimpleMatch(theme, "constant.numeric.baz", 2, Italic, _E, _NOT_SET);

		// matches constant.numeric.hex
		assertSimpleMatch(theme, "constant.numeric.hex", 3, Bold, _E, _NOT_SET);
		assertSimpleMatch(theme, "constant.numeric.hex.baz", 3, Bold, _E, _NOT_SET);

		// matches constant.numeric.oct
		assertSimpleMatch(theme, "constant.numeric.oct", 3, Bold | Italic | Underline, _E, _NOT_SET);
		assertSimpleMatch(theme, "constant.numeric.oct.baz", 3, Bold | Italic | Underline, _E, _NOT_SET);

		// matches constant.numeric.dec
		assertSimpleMatch(theme, "constant.numeric.dec", 3, None, _F, _NOT_SET);
		assertSimpleMatch(theme, "constant.numeric.dec.baz", 3, None, _F, _NOT_SET);

		// matches storage.object.bar
		assertSimpleMatch(theme, "storage.object.bar", 3, None, _H, _NOT_SET);
		assertSimpleMatch(theme, "storage.object.bar.baz", 3, None, _H, _NOT_SET);

		// does not match storage.object.bar
		assertSimpleMatch(theme, "storage.object.bart", 0, NotSet, _NOT_SET, _NOT_SET);
		assertSimpleMatch(theme, "storage.object", 0, NotSet, _NOT_SET, _NOT_SET);
		assertSimpleMatch(theme, "storage", 0, NotSet, _NOT_SET, _NOT_SET);

		assertMatch(theme, "bar",
			new ThemeTrieElementRule(1, list("selector", "source.css"), Bold, _NOT_SET, _C),
			new ThemeTrieElementRule(1, null, NotSet, _NOT_SET, _C));
	}

	/**
	 * test: theme matching Microsoft/vscode#23460
	 */
	@Test
	void testMicrosoft_vscode_23460() throws Exception {
		final Theme theme = createTheme("""
			{"settings": [
				{
					"settings": {
						"foreground": "#aec2e0",
						"background": "#14191f"
					}
				}, {
					"name": "JSON String",
					"scope": "meta.structure.dictionary.json string.quoted.double.json",
					"settings": {
						"foreground": "#FF410D"
					}
				}, {
					"scope": "meta.structure.dictionary.json string.quoted.double.json",
					"settings": {
						"foreground": "#ffffff"
					}
				}, {
					"scope": "meta.structure.dictionary.value.json string.quoted.double.json",
					"settings": {
						"foreground": "#FF410D"
					}
				}
			]}""");

		final ColorMap colorMap = new ColorMap();
		final int _NOT_SET = 0;
		final int _A = colorMap.getId("#aec2e0");
		final int _B = colorMap.getId("#14191f");
		final int _C = colorMap.getId("#FF410D");
		final int _D = colorMap.getId("#ffffff");

		// string.quoted.double.json
		// meta.structure.dictionary.value.json
		// meta.structure.dictionary.json
		// source.json
		assertMatch(theme, "string.quoted.double.json",
			new ThemeTrieElementRule(4, list("meta.structure.dictionary.value.json"), NotSet, _C, _NOT_SET),
			new ThemeTrieElementRule(4, list("meta.structure.dictionary.json"), NotSet, _D, _NOT_SET),
			new ThemeTrieElementRule(0, null, NotSet, _NOT_SET, _NOT_SET));

		final var parent3 = new AttributedScopeStack(null, "source.json", 0);
		final var parent2 = new AttributedScopeStack(parent3, "meta.structure.dictionary.json", 0);
		final var parent1 = new AttributedScopeStack(parent2, "meta.structure.dictionary.value.json", 0);

		final int r = AttributedScopeStack.mergeMetadata(
			0,
			parent1,
			new BasicScopeAttributes("string.quoted.double.json", 0, 0, theme.match("string.quoted.double.json")));
		final String color = theme.getColor(EncodedTokenAttributes.getForeground(r));
		assertEquals("#FF410D", color);
	}

	private void assertMatch(final Theme theme, final String scopeName, final ThemeTrieElementRule... expected) {
		final var actual = theme.match(scopeName);
		assertArrayEquals(expected, actual.toArray(), "when matching <<" + scopeName + ">>");
	}

	private void assertSimpleMatch(final Theme theme, final String scopeName, final int scopeDepth, final int fontStyle,
		final int foreground, final int background) {
		assertMatch(theme, scopeName,
			new ThemeTrieElementRule(scopeDepth, null, fontStyle, foreground, background));
	}

	private void assertNoMatch(final Theme theme, final String scopeName) {
		assertMatch(theme, scopeName,
			new ThemeTrieElementRule(0, null, NotSet, 0, 0 /*_NOT_SET, _NOT_SET*/));
	}
}
