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
import java.util.List;

import org.eclipse.tm4e.core.internal.theme.reader.ThemeReader;
import org.junit.jupiter.api.Test;

/**
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/tests/themes.test.ts</a>
 *
 */
public class ThemeParsingTest {

	@Test
	public void testCanParse() throws Exception {
		final var actual = parseTheme(("{'settings': [" +
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
				"]}").replace('\'', '"'));

		final var expected = new ParsedThemeRule[] {
				new ParsedThemeRule("", null, 0, FontStyle.NotSet, "#F8F8F2", "#272822"),
				new ParsedThemeRule("source", null, 1, FontStyle.NotSet, null, "#100000"),
				new ParsedThemeRule("something", null, 1, FontStyle.NotSet, null, "#100000"),
				new ParsedThemeRule("bar", null, 2, FontStyle.NotSet, null, "#010000"),
				new ParsedThemeRule("baz", null, 2, FontStyle.NotSet, null, "#010000"),
				new ParsedThemeRule("bar", Arrays.asList("selector", "source.css"), 3, FontStyle.Bold, null, null),
				new ParsedThemeRule("constant", null, 4, FontStyle.Italic, "#ff0000", null),
				new ParsedThemeRule("constant.numeric", null, 5, FontStyle.NotSet, "#00ff00", null),
				new ParsedThemeRule("constant.numeric.hex", null, 6, FontStyle.Bold, null, null),
				new ParsedThemeRule("constant.numeric.oct", null, 7,
						FontStyle.Bold | FontStyle.Italic | FontStyle.Underline, null, null),
				new ParsedThemeRule("constant.numeric.bin", null, 8, FontStyle.Bold | FontStyle.Strikethrough, null,
						null),
				new ParsedThemeRule("constant.numeric.dec", null, 9, FontStyle.None, "#0000ff", null),
				new ParsedThemeRule("foo", null, 10, FontStyle.None, "#CFA", null), };

		assertArrayEquals(expected, actual.toArray());
	}

	private List<ParsedThemeRule> parseTheme(String theme) throws Exception {
		return Theme.parseTheme(ThemeReader.readThemeSync("theme.json", new ByteArrayInputStream(theme.getBytes())));
	}
}
