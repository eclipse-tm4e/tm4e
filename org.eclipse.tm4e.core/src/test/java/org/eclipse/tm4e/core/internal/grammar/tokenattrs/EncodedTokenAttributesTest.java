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
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.grammar.tokenattrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.internal.theme.FontStyle.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * {@link EncodedTokenAttributes} tests same than vscode-textmate.
 *
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/tests/grammar.test.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/tests/grammar.test.ts</a>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EncodedTokenAttributesTest {

	@SuppressWarnings("null")
   private static void assertEquals(final int metadata, final int languageId, final int /*StandardTokenType*/ tokenType,
         final boolean containsBalancedBrackets, final int /*FontStyle*/ fontStyle, final int foreground, final int background) {
		final var actual = "{\n"
				+ "languageId: " + EncodedTokenAttributes.getLanguageId(metadata) + ",\n"
				+ "tokenType: " + EncodedTokenAttributes.getTokenType(metadata) + ",\n"
				+ "containsBalancedBrackets: " + EncodedTokenAttributes.containsBalancedBrackets(metadata) + ",\n"
				+ "fontStyle: " + EncodedTokenAttributes.getFontStyle(metadata) + ",\n"
				+ "foreground: " + EncodedTokenAttributes.getForeground(metadata) + ",\n"
				+ "background: " + EncodedTokenAttributes.getBackground(metadata) + ",\n"
				+ "}";

		final var expected = "{\n"
				+ "languageId: " + languageId + ",\n"
				+ "tokenType: " + tokenType + ",\n"
				+ "containsBalancedBrackets: " + containsBalancedBrackets + ",\n"
				+ "fontStyle: " + fontStyle + ",\n"
				+ "foreground: " + foreground + ",\n"
				+ "background: " + background + ",\n"
				+ "}";

		assertThat(actual)
				.as("equals for " + EncodedTokenAttributes.toBinaryStr(metadata))
				.isEqualTo(expected);
	}

	@Test
	@Order(1)
	@DisplayName("StackElementMetadata works")
	void testWorks() {
		final int value = EncodedTokenAttributes.set(0, 1, OptionalStandardTokenType.RegEx, false, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);
	}

	@Test
	@Order(2)
	@DisplayName("StackElementMetadata can overwrite languageId")
	void testCanOverwriteLanguageId() {
		int value = EncodedTokenAttributes.set(0, 1, OptionalStandardTokenType.RegEx, false, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = EncodedTokenAttributes.set(value, 2, OptionalStandardTokenType.NotSet, false, NotSet, 0, 0);
		assertEquals(value, 2, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);
	}

	@Test
	@Order(3)
	@DisplayName("StackElementMetadata can overwrite tokenType")
	void testCanOverwriteTokenType() {
		int value = EncodedTokenAttributes.set(0, 1, OptionalStandardTokenType.RegEx, false, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = EncodedTokenAttributes.set(value, 0, OptionalStandardTokenType.Comment, false, NotSet, 0, 0);
		assertEquals(value, 1, StandardTokenType.Comment, false, Underline | Bold, 101, 102);
	}

	@Test
	@Order(4)
	@DisplayName("StackElementMetadata can overwrite font style")
	void testCanOverwriteFontStyle() {
		int value = EncodedTokenAttributes.set(0, 1, OptionalStandardTokenType.RegEx, false, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = EncodedTokenAttributes.set(value, 0, OptionalStandardTokenType.NotSet, false, None, 0, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, false, None, 101, 102);
	}

	@Test
	@Order(5)
	@DisplayName("StackElementMetadata can overwrite font style with strikethrough")
	void testCanOverwriteFontStyleWithStrikethrough() {
		int value = EncodedTokenAttributes.set(0, 1, OptionalStandardTokenType.RegEx, false, Strikethrough, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Strikethrough, 101, 102);

		value = EncodedTokenAttributes.set(value, 0, OptionalStandardTokenType.NotSet, false, None, 0, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, false, None, 101, 102);
	}

	@Test
	@Order(6)
	@DisplayName("StackElementMetadata can overwrite foreground")
	void testCanOverwriteForeground() {
		int value = EncodedTokenAttributes.set(0, 1, OptionalStandardTokenType.RegEx, false, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = EncodedTokenAttributes.set(value, 0, OptionalStandardTokenType.NotSet, false, NotSet, 5, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 5, 102);
	}

	@Test
	@Order(7)
	@DisplayName("StackElementMetadata can overwrite background")
	void testCanOverwriteBackground() {
		int value = EncodedTokenAttributes.set(0, 1, OptionalStandardTokenType.RegEx, false, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = EncodedTokenAttributes.set(value, 0, OptionalStandardTokenType.NotSet, false, NotSet, 0, 7);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 7);
	}

	@Test
	@Order(8)
	@DisplayName("StackElementMetadata can overwrite balanced bracket bit")
	void testCanOverwriteBalancedBracketBit() {
		int value = EncodedTokenAttributes.set(0, 1, OptionalStandardTokenType.RegEx, false, Underline | Bold, 101, 102);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);

		value = EncodedTokenAttributes.set(value, 0, OptionalStandardTokenType.NotSet, true, NotSet, 0, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, true, Underline | Bold, 101, 102);

		value = EncodedTokenAttributes.set(value, 0, OptionalStandardTokenType.NotSet, false, NotSet, 0, 0);
		assertEquals(value, 1, StandardTokenType.RegEx, false, Underline | Bold, 101, 102);
	}

	@Test
	@Order(9)
	@DisplayName("StackElementMetadata can work at max values")
	void testCanWorkAtMaxValues() {
		final int maxLangId = 255;
		final int maxTokenType = StandardTokenType.Comment | StandardTokenType.Other | StandardTokenType.RegEx | StandardTokenType.String;
		final int maxFontStyle = Bold | Italic | Underline;
		final int maxForeground = 511;
		final int maxBackground = 254;

		final int value = EncodedTokenAttributes.set(0, maxLangId, maxTokenType, true, maxFontStyle, maxForeground, maxBackground);
		assertEquals(value, maxLangId, maxTokenType, true, maxFontStyle, maxForeground, maxBackground);
	}
}
