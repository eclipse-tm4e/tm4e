/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Sebastian Thomschke - added isBold/isItalic/isStrikethrough/isUnderline
 */
package org.eclipse.tm4e.core.internal.theme;

/**
 * Font style definitions.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/theme.ts#L306">
 *      https://github.com/microsoft/vscode-textmate/blob/main/src/theme.ts</a>
 */
public final class FontStyle {

	public static final int NotSet = -1;

	// These are bit-flags, so it can be `Italic | Bold`
	public static final int None = 0;
	public static final int Italic = 1;
	public static final int Bold = 2;
	public static final int Underline = 4;
	public static final int Strikethrough = 8;

	public static String fontStyleToString(final int fontStyle) {
		if (fontStyle == NotSet) {
			return "not set";
		}
		if (fontStyle == None) {
			return "none";
		}

		final var style = new StringBuilder();
		if (isItalic(fontStyle)) {
			style.append("italic ");
		}
		if (isBold(fontStyle)) {
			style.append("bold ");
		}
		if (isUnderline(fontStyle)) {
			style.append("underline ");
		}
		if (isStrikethrough(fontStyle)) {
			style.append("strikethrough ");
		}
		if (style.isEmpty()) {
			return "none";
		}
		style.setLength(style.length() - 1);
		return style.toString();
	}

	public static boolean isBold(final int fontStyle) {
		return (fontStyle & Bold) == Bold;
	}

	public static boolean isItalic(final int fontStyle) {
		return (fontStyle & Italic) == Italic;
	}

	public static boolean isUnderline(final int fontStyle) {
		return (fontStyle & Underline) == Underline;
	}

	public static boolean isStrikethrough(final int fontStyle) {
		return (fontStyle & Strikethrough) == Strikethrough;
	}

	private FontStyle() {
	}
}
