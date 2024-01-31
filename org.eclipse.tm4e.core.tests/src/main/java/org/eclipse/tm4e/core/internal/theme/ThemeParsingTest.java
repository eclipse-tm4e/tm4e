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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/09effd8b7429b71010e0fa34ea2e16e622692946/src/tests/themes.test.ts#L286">
 *      github.com/microsoft/vscode-textmate/blob/main/src/tests/themes.test.ts</a>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ThemeParsingTest extends AbstractThemeTest {

	@Test
	@Order(1)
	@DisplayName("Theme parsing can parse")
	public void testCanParse() throws Exception {
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

		final var expected = new ParsedThemeRule[] {
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
			new ParsedThemeRule("foo", null, 10, None, "#CFA", null),
		};

		assertArrayEquals(expected, actual.toArray());
	}
}
