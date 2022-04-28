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
package org.eclipse.tm4e.core.theme;

/**
 * Font style definitions.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/theme.ts#L153">
 *      github.com/microsoft/vscode-textmate/blob/master/src/theme.ts</a>
 */
public class FontStyle {

	public static final int NotSet = -1;

	// This can are bit-flags, so it can be `Italic | Bold`
	public static final int None = 0;
	public static final int Italic = 1;
	public static final int Bold = 2;
	public static final int Underline = 4;
	public static final int Strikethrough = 8;

}
