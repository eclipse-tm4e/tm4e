/**
 * Copyright (c) 2015-2022 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 * - Sebastian Thomschke (Vegard IT) - moved methods to separate class
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/core/indentation.ts">
 *      github.com/microsoft/vscode/src/vs/editor/common/core/indentation.ts</a>
 */
public final class Indentation {

	private static String normalizeIndentationFromWhitespace(final String text, final int indentSize, final boolean insertSpaces) {
		int spacesCnt = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\t') {
				spacesCnt = nextIndentTabStop(spacesCnt, indentSize);
			} else {
				spacesCnt++;
			}
		}

		final var result = new StringBuilder();
		if (!insertSpaces) {
			final int tabsCnt = spacesCnt / indentSize;
			spacesCnt = spacesCnt % indentSize;
			for (int i = 0; i < tabsCnt; i++) {
				result.append('\t');
			}
		}

		for (int i = 0; i < spacesCnt; i++) {
			result.append(' ');
		}

		return result.toString();
	}

	public static String normalizeIndentation(final String text, final int indentSize, final boolean insertSpaces) {
		int firstNonWhitespaceIdx = Strings.firstNonWhitespaceIndex(text);
		if (firstNonWhitespaceIdx == -1) {
			firstNonWhitespaceIdx = text.length();
		}
		return normalizeIndentationFromWhitespace(text.substring(0, firstNonWhitespaceIdx), indentSize, insertSpaces)
				+ text.substring(firstNonWhitespaceIdx);
	}

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/core/cursorColumns.ts#L130">
	 *      github.com/microsoft/vscode/src/vs/editor/common/core/cursorColumns.ts</a>
	 */
	private static int nextIndentTabStop(final int visibleColumn, final int indentSize) {
		return visibleColumn + indentSize - visibleColumn % indentSize;
	}

	private Indentation() {
	}
}
