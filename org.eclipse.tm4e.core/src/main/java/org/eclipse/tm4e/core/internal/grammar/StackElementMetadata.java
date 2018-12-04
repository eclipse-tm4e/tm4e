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
public class StackElementMetadata {

	public static String toBinaryStr(int metadata) {
		/*
		 * let r = metadata.toString(2); while (r.length < 32) { r = '0' + r; }
		 * return r;
		 */
		// TODO!!!
		return null;
	}

	public static void printMetadata(int metadata) {
		int _languageId = StackElementMetadata.getLanguageId(metadata);
		int _tokenType = StackElementMetadata.getTokenType(metadata);
		int _fontStyle = StackElementMetadata.getFontStyle(metadata);
		int foreground = StackElementMetadata.getForeground(metadata);
		int background = StackElementMetadata.getBackground(metadata);

		// TODO!!!
		/*
		 * console.log({ languageId: languageId, tokenType: tokenType,
		 * fontStyle: fontStyle, foreground: foreground, background: background,
		 * });
		 */
	}

	public static int getLanguageId(int metadata) {
		return (metadata & MetadataConsts.LANGUAGEID_MASK) >>> MetadataConsts.LANGUAGEID_OFFSET;
	}

	public static int getTokenType(int metadata) {
		return (metadata & MetadataConsts.TOKEN_TYPE_MASK) >>> MetadataConsts.TOKEN_TYPE_OFFSET;
	}

	public static int getFontStyle(int metadata) {
		return (metadata & MetadataConsts.FONT_STYLE_MASK) >>> MetadataConsts.FONT_STYLE_OFFSET;
	}

	public static int getForeground(int metadata) {
		return (metadata & MetadataConsts.FOREGROUND_MASK) >>> MetadataConsts.FOREGROUND_OFFSET;
	}

	public static int getBackground(int metadata) {
		return (metadata & MetadataConsts.BACKGROUND_MASK) >>> MetadataConsts.BACKGROUND_OFFSET;
	}

	public static int set(int metadata, int languageId, int tokenType, int fontStyle, int foreground, int background) {
		int _languageId = StackElementMetadata.getLanguageId(metadata);
		int _tokenType = StackElementMetadata.getTokenType(metadata);
		int _fontStyle = StackElementMetadata.getFontStyle(metadata);
		int _foreground = StackElementMetadata.getForeground(metadata);
		int _background = StackElementMetadata.getBackground(metadata);

		if (languageId != 0) {
			_languageId = languageId;
		}
		if (tokenType != StandardTokenType.Other) {
			_tokenType = tokenType;
		}
		if (fontStyle != FontStyle.NotSet) {
			_fontStyle = fontStyle;
		}
		if (foreground != 0) {
			_foreground = foreground;
		}
		if (background != 0) {
			_background = background;
		}

		return ((_languageId << MetadataConsts.LANGUAGEID_OFFSET) | (_tokenType << MetadataConsts.TOKEN_TYPE_OFFSET)
				| (_fontStyle << MetadataConsts.FONT_STYLE_OFFSET) | (_foreground << MetadataConsts.FOREGROUND_OFFSET)
				| (_background << MetadataConsts.BACKGROUND_OFFSET)) >>> 0;
	}

}
