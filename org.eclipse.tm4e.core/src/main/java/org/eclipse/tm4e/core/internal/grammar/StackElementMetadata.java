/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.grammar;

import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.theme.FontStyle;

/**
 *
 * Metadata for {@link StackElement}.
 *
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

	static int getLanguageId(int metadata) {
		return (metadata & MetadataConsts.LANGUAGEID_MASK) >>> MetadataConsts.LANGUAGEID_OFFSET;
	}

	static int getTokenType(int metadata) {
		return (metadata & MetadataConsts.TOKEN_TYPE_MASK) >>> MetadataConsts.TOKEN_TYPE_OFFSET;
	}

	static int getFontStyle(int metadata) {
		return (metadata & MetadataConsts.FONT_STYLE_MASK) >>> MetadataConsts.FONT_STYLE_OFFSET;
	}

	public static int getForeground(int metadata) {
		return (metadata & MetadataConsts.FOREGROUND_MASK) >>> MetadataConsts.FOREGROUND_OFFSET;
	}

	static int getBackground(int metadata) {
		return (metadata & MetadataConsts.BACKGROUND_MASK) >>> MetadataConsts.BACKGROUND_OFFSET;
	}

	static int set(int metadata, int languageId, int tokenType, int fontStyle, int foreground, int background) {
		languageId = languageId == 0 ? StackElementMetadata.getLanguageId(metadata) : languageId;
		tokenType = tokenType == StandardTokenType.Other ? StackElementMetadata.getTokenType(metadata) : tokenType;
		fontStyle = fontStyle == FontStyle.NotSet ? StackElementMetadata.getFontStyle(metadata) : fontStyle;
		foreground = foreground == 0 ? StackElementMetadata.getForeground(metadata) : foreground;
		background = background == 0 ? StackElementMetadata.getBackground(metadata) : background;
		return ((languageId << MetadataConsts.LANGUAGEID_OFFSET) | (tokenType << MetadataConsts.TOKEN_TYPE_OFFSET)
				| (fontStyle << MetadataConsts.FONT_STYLE_OFFSET) | (foreground << MetadataConsts.FOREGROUND_OFFSET)
				| (background << MetadataConsts.BACKGROUND_OFFSET)) >>> 0;
	}

}
