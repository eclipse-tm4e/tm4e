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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.theme.FontStyle;

/**
 * Metadata for {@link StackElement}.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/master/src/metadata.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/metadata.ts</a>
 */
public final class StackElementMetadata {

	/**
	 * Content should be referenced statically
	 */
	private StackElementMetadata() {
	}

	static String toBinaryStr(final int metadata) {
		return new StringBuilder(Integer.toBinaryString(metadata))
				.insert(0, "0".repeat(Integer.numberOfLeadingZeros(metadata)))
				.toString();
	}

	static int getLanguageId(final int metadata) {
		return (metadata & MetadataConsts.LANGUAGEID_MASK) >>> MetadataConsts.LANGUAGEID_OFFSET;
	}

	static int getTokenType(final int metadata) {
		return (metadata & MetadataConsts.TOKEN_TYPE_MASK) >>> MetadataConsts.TOKEN_TYPE_OFFSET;
	}

	static boolean containsBalancedBrackets(final int metadata) {
		return (metadata & MetadataConsts.BALANCED_BRACKETS_MASK) != 0;
	}

	static int getFontStyle(final int metadata) {
		return (metadata & MetadataConsts.FONT_STYLE_MASK) >>> MetadataConsts.FONT_STYLE_OFFSET;
	}

	public static int getForeground(final int metadata) {
		return (metadata & MetadataConsts.FOREGROUND_MASK) >>> MetadataConsts.FOREGROUND_OFFSET;
	}

	static int getBackground(final int metadata) {
		return (metadata & MetadataConsts.BACKGROUND_MASK) >>> MetadataConsts.BACKGROUND_OFFSET;
	}

	/**
	 * Updates the fields in `metadata`.
	 * A value of `0`, `NotSet` or `null` indicates that the corresponding field should be left as is.
	 */
	static int set(final int metadata, final int languageId, final /*OptionalStandardTokenType*/ int tokenType,
			@Nullable Boolean containsBalancedBrackets, final int fontStyle, final int foreground, int background) {
		final var _languageId = languageId == 0 ? getLanguageId(metadata) : languageId;
		final var _tokenType = tokenType == OptionalStandardTokenType.NotSet ? getTokenType(metadata) : tokenType;
		final var _containsBalancedBracketsBit = (containsBalancedBrackets == null ? containsBalancedBrackets(metadata)
				: containsBalancedBrackets) ? 1 : 0;
		final var _fontStyle = fontStyle == FontStyle.NotSet ? getFontStyle(metadata) : fontStyle;
		final var _foreground = foreground == 0 ? getForeground(metadata) : foreground;
		final var _background = background == 0 ? getBackground(metadata) : background;

		return ((_languageId << MetadataConsts.LANGUAGEID_OFFSET)
				| (_tokenType << MetadataConsts.TOKEN_TYPE_OFFSET)
				| (_containsBalancedBracketsBit << MetadataConsts.BALANCED_BRACKETS_OFFSET)
				| (_fontStyle << MetadataConsts.FONT_STYLE_OFFSET)
				| (_foreground << MetadataConsts.FOREGROUND_OFFSET)
				| (_background << MetadataConsts.BACKGROUND_OFFSET)) >>> 0;
	}
}
