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

import static org.eclipse.tm4e.core.internal.theme.FontStyle.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * {@link StackElementMetadata} tests same than vscode-textmate.
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/tests/grammar.test.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/tests/grammar.test.ts</a>
 */
public class StackElementMetadataTest {

	@Test
	public void testWorks() {
		final int value = StackElementMetadata.set(0, 1, OptionalStandardTokenType.RegEx, null, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);
	}

	@Test
	public void testCanOverwriteLanguageId() {
		int value = StackElementMetadata.set(0, 1, OptionalStandardTokenType.RegEx, null, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = StackElementMetadata.set(value, 2, OptionalStandardTokenType.NotSet, null, NotSet, 0, 0);
		assertEquals(value, 2, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);
	}

	@Test
	public void testCanOverwriteTokenType() {
		int value = StackElementMetadata.set(0, 1, OptionalStandardTokenType.RegEx, null, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = StackElementMetadata.set(value, 0, OptionalStandardTokenType.Comment, null, NotSet, 0, 0);
		assertEquals(value, 1, StandardTokenType.Comment, false, Underline | Bold, 101, 102);
	}

	@Test
	public void testCanOverwriteFontStyle() {
		int value = StackElementMetadata.set(0, 1, OptionalStandardTokenType.RegEx, null, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = StackElementMetadata.set(value, 0, OptionalStandardTokenType.NotSet, null, None, 0, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, false, None, 101, 102);
	}

	@Test
	public void testCanOverwriteFontStyleWithStrikethrough() {
		int value = StackElementMetadata.set(0, 1, OptionalStandardTokenType.RegEx, null, Strikethrough, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Strikethrough, 101, 102);

		value = StackElementMetadata.set(value, 0, OptionalStandardTokenType.NotSet, null, None, 0, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, false, None, 101, 102);
	}

	@Test
	public void testCanOverwriteForeground() {
		int value = StackElementMetadata.set(0, 1, OptionalStandardTokenType.RegEx, null, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = StackElementMetadata.set(value, 0, OptionalStandardTokenType.NotSet, null, NotSet, 5, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 5, 102);
	}

	@Test
	public void testCanOverwriteBackground() {
		int value = StackElementMetadata.set(0, 1, OptionalStandardTokenType.RegEx, null, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = StackElementMetadata.set(value, 0, OptionalStandardTokenType.NotSet, null, NotSet, 0, 7);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 7);
	}

	@Test
	public void testCanWorkAtMaxValues() {
		final int maxLangId = 255;
		final int maxTokenType = StandardTokenType.Comment | StandardTokenType.Other | StandardTokenType.RegEx
				| StandardTokenType.String;
		final int maxFontStyle = Bold | Italic | Underline;
		final int maxForeground = 511;
		final int maxBackground = 254;

		final int value = StackElementMetadata.set(0, maxLangId, maxTokenType, true, maxFontStyle, maxForeground,
				maxBackground);
		assertEquals(value, maxLangId, maxTokenType, true, maxFontStyle, maxForeground, maxBackground);
	}

	private static void assertEquals(final int metadata, final int languageId, final int /*StandardTokenType*/ tokenType,
			final boolean containsBalancedBrackets, final int /*FontStyle*/ fontStyle, final int foreground, final int background) {
		final String actual = "{\n" +
				"languageId: " + StackElementMetadata.getLanguageId(metadata) + ",\n" +
				"tokenType: " + StackElementMetadata.getTokenType(metadata) + ",\n" +
				"containsBalancedBrackets: " + StackElementMetadata.containsBalancedBrackets(metadata) + ",\n" +
				"fontStyle: " + StackElementMetadata.getFontStyle(metadata) + ",\n" +
				"foreground: " + StackElementMetadata.getForeground(metadata) + ",\n" +
				"background: " + StackElementMetadata.getBackground(metadata) + ",\n" +
				"}";

		final String expected = "{\n" +
				"languageId: " + languageId + ",\n" +
				"tokenType: " + tokenType + ",\n" +
				"containsBalancedBrackets: " + containsBalancedBrackets + ",\n" +
				"fontStyle: " + fontStyle + ",\n" +
				"foreground: " + foreground + ",\n" +
				"background: " + background + ",\n" +
				"}";

		Assertions.assertEquals(expected, actual, "equals for " + StackElementMetadata.toBinaryStr(metadata));
	}
}
