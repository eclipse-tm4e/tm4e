/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
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
 * - Sebastian Thomschke (Vegard IT) - translation and adaptation to Java
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/base/common/strings.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/base/common/strings.ts</a>
 */
public final class Strings {

	/**
	 * Escapes regular expression characters in a given string
	 */
	public static String escapeRegExpCharacters(final String value) {
		return value.replaceAll("[\\-\\\\\\{\\}\\*\\+\\?\\|\\^\\$\\.\\[\\]\\(\\)\\#]", "\\\\$0");
	}

	/**
	 * @returns first index of the string that is not whitespace; or -1 if string is empty or contains only whitespaces.
	 */
	public static int firstNonWhitespaceIndex(final String text) {
		for (int i = 0, len = text.length(); i < len; i++) {
			final char ch = text.charAt(i);
			if (!Character.isWhitespace(ch))
				return i;
		}
		return -1;
	}

	/**
	 * @return the leading whitespace of the string; or the entire string if the string contains only whitespaces.
	 */
	public static String getLeadingWhitespace(final String searchIn) {
		return getLeadingWhitespace(searchIn, 0, searchIn.length());
	}

	/**
	 * @return the leading whitespace of the string; or the entire string if the string contains only whitespaces.
	 */
	public static String getLeadingWhitespace(final String searchIn, final int startAt) {
		return getLeadingWhitespace(searchIn, startAt, searchIn.length());
	}

	/**
	 * @return the leading whitespace of the string; or the entire string if the string contains only whitespaces.
	 */
	public static String getLeadingWhitespace(final String searchIn, final int startAt, final int endAt) {
		for (int i = startAt; i < endAt; i++) {
			final char ch = searchIn.charAt(i);
			if (!Character.isWhitespace(ch))
				return searchIn.substring(startAt, i);
		}
		return searchIn.substring(startAt, endAt);
	}

	/**
	 * @return the last index of the string that is not whitespace; or -1 if string is empty or contains only whitespaces.
	 *         Defaults to starting from the end of the string.
	 */
	public static int lastNonWhitespaceIndex(final String str) {
		return lastNonWhitespaceIndex(str, str.length() - 1);
	}

	/**
	 * @return the last index of the string that is not whitespace; or -1 if string is empty or contains only whitespaces.
	 */
	public static int lastNonWhitespaceIndex(final String str, final int startIndex) {
		for (int i = startIndex; i >= 0; i--) {
			final char ch = str.charAt(i);
			if (!Character.isWhitespace(ch))
				return i;
		}
		return -1;
	}

	private Strings() {
	}
}
