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

/**
 * Font style definitions.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/f4e28ea43bacb883afa78a466e95a6bdd7b92129/src/theme.ts#L153">
 *      github.com/microsoft/vscode-textmate/blob/master/src/theme.ts</a>
 */
public final class FontStyle {

	public static final int NotSet = -1;

	// This can are bit-flags, so it can be `Italic | Bold`
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
		if ((fontStyle & Italic) == Italic) {
			style.append("italic ");
		}
		if ((fontStyle & Bold) == Bold) {
			style.append("bold ");
		}
		if ((fontStyle & Underline) == Underline) {
			style.append("underline ");
		}
		if ((fontStyle & Strikethrough) == Strikethrough) {
			style.append("strikethrough ");
		}
		if (style.isEmpty()) {
			return "none";
		}
		style.setLength(style.length() - 1);
		return style.toString();
	}

	private FontStyle() {
	}
}
