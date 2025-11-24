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
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 * - Sebastian Thomschke - add splitToArray/List methods
 */
package org.eclipse.tm4e.core.internal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/utils.ts">
 *      github.com/microsoft/vscode-textmate/blob/main/src/utils.ts</a>
 */
public final class StringUtils {

	private static final Pattern RRGGBB = Pattern.compile("^#[0-9a-f]{6}", Pattern.CASE_INSENSITIVE);
	private static final Pattern RRGGBBAA = Pattern.compile("^#[0-9a-f]{8}", Pattern.CASE_INSENSITIVE);
	private static final Pattern RGB = Pattern.compile("^#[0-9a-f]{3}", Pattern.CASE_INSENSITIVE);
	private static final Pattern RGBA = Pattern.compile("^#[0-9a-f]{4}", Pattern.CASE_INSENSITIVE);

	public static int strcmp(final String a, final String b) {
		final int result = a.compareTo(b);
		if (result < 0) {
			return -1;
		} else if (result > 0) {
			return 1;
		}
		return 0;
	}

	public static int strArrCmp(final @Nullable List<String> a, final @Nullable List<String> b) {
		if (a == null && b == null) {
			return 0;
		}
		if (a == null) {
			return -1;
		}
		if (b == null) {
			return 1;
		}
		final int len1 = a.size();
		final int len2 = b.size();
		if (len1 == len2) {
			for (int i = 0; i < len1; i++) {
				final int res = strcmp(a.get(i), b.get(i));
				if (res != 0) {
					return res;
				}
			}
			return 0;
		}
		return len1 - len2;
	}

	public static boolean isValidHexColor(final CharSequence hex) {
		if (hex.isEmpty()) {
			return false;
		}

		if (RRGGBB.matcher(hex).matches()) {
			// #rrggbb
			return true;
		}

		if (RRGGBBAA.matcher(hex).matches()) {
			// #rrggbbaa
			return true;
		}

		if (RGB.matcher(hex).matches()) {
			// #rgb
			return true;
		}

		if (RGBA.matcher(hex).matches()) {
			// #rgba
			return true;
		}

		return false;
	}

	private static volatile @Nullable Pattern CONTAINS_RTL;

	private static Pattern makeContainsRtl() {
		return Pattern.compile(
				"(?:[\\u05BE\\u05C0\\u05C3\\u05C6\\u05D0-\\u05F4\\u0608\\u060B\\u060D\\u061B-\\u064A"
						+ "\\u066D-\\u066F\\u0671-\\u06D5\\u06E5\\u06E6\\u06EE\\u06EF\\u06FA-\\u0710"
						+ "\\u0712-\\u072F\\u074D-\\u07A5\\u07B1-\\u07EA\\u07F4\\u07F5\\u07FA"
						+ "\\u07FE-\\u0815\\u081A\\u0824\\u0828\\u0830-\\u0858\\u085E-\\u088E"
						+ "\\u08A0-\\u08C9\\u200F\\uFB1D\\uFB1F-\\uFB28\\uFB2A-\\uFD3D"
						+ "\\uFD50-\\uFDC7\\uFDF0-\\uFDFC\\uFE70-\\uFEFC]"
						+ "|\\uD802[\\uDC00-\\uDD1B\\uDD20-\\uDE00\\uDE10-\\uDE35\\uDE40-\\uDEE4"
						+ "\\uDEEB-\\uDF35\\uDF40-\\uDFFF]"
						+ "|\\uD803[\\uDC00-\\uDD23\\uDE80-\\uDEA9\\uDEAD-\\uDF45\\uDF51-\\uDF81"
						+ "\\uDF86-\\uDFF6]"
						+ "|\\uD83A[\\uDC00-\\uDCCF\\uDD00-\\uDD43\\uDD4B-\\uDFFF]"
						+ "|\\uD83B[\\uDC00-\\uDEBB])");
	}

	/**
	 * Returns true if `str` contains any Unicode character that is classified as "R" or "AL".
	 */
	public static boolean containsRTL(String str) {
		Pattern p = CONTAINS_RTL;
		if (p == null) {
			p = CONTAINS_RTL = makeContainsRtl();
		}
		return p.matcher(str).find();
	}

	/* **************************************************************
	 * From here on: custom tm4e code - not from upstream
	 * **************************************************************/

	private static final List<String> LIST_WITH_EMPTY_STRING = List.of("");

	public static boolean isNullOrEmpty(final @Nullable String txt) {
		return txt == null || txt.isEmpty();
	}

	public static String nullToEmpty(final @Nullable String txt) {
		return txt == null ? "" : txt;
	}

	/**
	 * Very fast String splitting.
	 *
	 * 7.5 times faster than {@link String#split(String)} and 2.5 times faster than {@link com.google.common.base.Splitter}.
	 */
	@SuppressWarnings("javadoc")
	public static String[] splitToArray(final String line, final char separator) {
		return splitToArray(line, separator, -1);
	}

	/**
	 * Very fast String splitting.
	 *
	 * 7.5 times faster than {@link String#split(String)} and 2.5 times faster than {@link com.google.common.base.Splitter}.
	 */
	@SuppressWarnings("javadoc")
	public static String[] splitToArray(final String line, final char separator, final int limit) {
		if (line.isEmpty())
			return new String[] { "" };

		var tmp = new String[8];
		int count = 0;
		int start = 0;
		int end = line.indexOf(separator, 0);
		while (end >= 0) {
			if (count == tmp.length) { // check if array needs resize
				final var tmp2 = new String[tmp.length + (tmp.length >> 1)];
				System.arraycopy(tmp, 0, tmp2, 0, count);
				tmp = tmp2;
			}
			tmp[count] = line.substring(start, end);
			count++;
			start = end + 1;
			if (count == limit)
				break;
			end = line.indexOf(separator, start);
		}
		if (count == tmp.length) { // check if array needs resize
			final var tmp2 = new String[tmp.length + 1];
			System.arraycopy(tmp, 0, tmp2, 0, count);
			tmp = tmp2;
		}
		tmp[count] = line.substring(start);
		count++;

		if (count == tmp.length) {
			return tmp;
		}
		final var result = new String[count];
		System.arraycopy(tmp, 0, result, 0, count);
		return result;
	}

	/**
	 * Very fast String splitting.
	 */
	public static List<String> splitToList(final String line, final char separator) {
		if (line.isEmpty())
			return LIST_WITH_EMPTY_STRING;

		final var result = new ArrayList<String>(8);
		int start = 0;
		int end = line.indexOf(separator, 0);
		while (end >= 0) {
			result.add(line.substring(start, end));
			start = end + 1;
			end = line.indexOf(separator, start);
		}
		result.add(line.substring(start));
		return result;
	}

	public static String substringBefore(final String searchIn, final char searchFor, final String defaultValue) {
		final int foundAt = searchIn.indexOf(searchFor);
		return foundAt == -1 ? defaultValue : searchIn.substring(0, foundAt);
	}

	/**
	 * @return "{SimpleClassName}{...fields...}"
	 */
	public static String toString(final @Nullable Object object, final Consumer<StringBuilder> fieldsBuilder) {
		if (object == null)
			return "null";
		final var sb = new StringBuilder(object.getClass().getSimpleName());
		sb.append('{');
		fieldsBuilder.accept(sb);
		sb.append('}');
		return sb.toString();
	}

	private StringUtils() {
	}
}
