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
package org.eclipse.tm4e.core.internal.grammar;

import org.eclipse.tm4e.core.theme.FontStyle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link StackElementMetadata} tests same than vscode-textmate.
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/grammar.test.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/tests/grammar.test.ts</a>
 */
public class GrammarTest {

	@Test
	public void testWorks() {
		int value = StackElementMetadata.set(0, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101,
				102);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101, 102);
	}

	@Test
	public void testCanOverwriteLanguageId() {
		int value = StackElementMetadata.set(0, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101,
				102);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101, 102);

		value = StackElementMetadata.set(value, 2, StandardTokenType.Other, FontStyle.NotSet, 0, 0);
		assertEquals(value, 2, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101, 102);
	}

	@Test
	public void testCanOverwriteTokenType() {
		int value = StackElementMetadata.set(0, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101,
				102);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101, 102);

		value = StackElementMetadata.set(value, 0, StandardTokenType.Comment, FontStyle.NotSet, 0, 0);
		assertEquals(value, 1, StandardTokenType.Comment, FontStyle.Underline | FontStyle.Bold, 101, 102);
	}

	@Test
	public void testCanOverwriteFontStyle() {
		int value = StackElementMetadata.set(0, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101,
				102);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101, 102);

		value = StackElementMetadata.set(value, 0, StandardTokenType.Other, FontStyle.None, 0, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.None, 101, 102);
	}

	@Test
	public void testCanOverwriteForeground() {
		int value = StackElementMetadata.set(0, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101,
				102);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101, 102);

		value = StackElementMetadata.set(value, 0, StandardTokenType.Other, FontStyle.NotSet, 5, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 5, 102);
	}

	@Test
	public void testCanOverwriteBackground() {
		int value = StackElementMetadata.set(0, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101,
				102);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101, 102);

		value = StackElementMetadata.set(value, 0, StandardTokenType.Other, FontStyle.NotSet, 0, 7);
		assertEquals(value, 1, StandardTokenType.RegEx, FontStyle.Underline | FontStyle.Bold, 101, 7);
	}

	@Test
	public void testCanWorkAtMaxValues() {
		int maxLangId = 255;
		int maxTokenType = StandardTokenType.Comment | StandardTokenType.Other | StandardTokenType.RegEx
				| StandardTokenType.String;
		int maxFontStyle = FontStyle.Bold | FontStyle.Italic | FontStyle.Underline;
		int maxForeground = 511;
		int maxBackground = 511;

		int value = StackElementMetadata.set(0, maxLangId, maxTokenType, maxFontStyle, maxForeground, maxBackground);
		assertEquals(value, maxLangId, maxTokenType, maxFontStyle, maxForeground, maxBackground);
	}

	private static void assertEquals(int metadata, int languageId, int tokenType, int fontStyle, int foreground, int background) {
		String actual = "{\n" +
			"languageId: " + StackElementMetadata.getLanguageId(metadata) + ",\n" +
			"tokenType: " + StackElementMetadata.getTokenType(metadata) + ",\n" +
			"fontStyle: " + StackElementMetadata.getFontStyle(metadata) + ",\n" +
			"foreground: " + StackElementMetadata.getForeground(metadata) + ",\n" +
			"background: " + StackElementMetadata.getBackground(metadata) + ",\n" +
		"}";

		String expected = "{\n" +
				"languageId: " + languageId + ",\n" +
				"tokenType: " + tokenType + ",\n" +
				"fontStyle: " + fontStyle + ",\n" +
				"foreground: " + foreground + ",\n" +
				"background: " + background + ",\n" +
			"}";

		Assertions.assertEquals(expected, actual, "equals for " + StackElementMetadata.toBinaryStr(metadata));
	}
}
