/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/Microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.oniguruma.OnigCaptureIndex;

public final class RegexSource {

	/**
	 * Helper class, access members statically
	 */
	private RegexSource() {
	}

	private static final Pattern CAPTURING_REGEX_SOURCE = Pattern
			.compile("\\$(\\d+)|\\$\\{(\\d+):\\/(downcase|upcase)}");

	private static final Pattern REGEXP_CHARACTERS = Pattern
			.compile("[\\-\\\\\\{\\}\\*\\+\\?\\|\\^\\$\\.\\,\\[\\]\\(\\)\\#\\s]");

	public static String escapeRegExpCharacters(final String value) {
		final var m = REGEXP_CHARACTERS.matcher(value);
		final var sb = new StringBuilder();
		while (m.find()) {
			m.appendReplacement(sb, "\\\\\\\\" + m.group());
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static boolean hasCaptures(@Nullable final String regexSource) {
		if (regexSource == null) {
			return false;
		}
		return CAPTURING_REGEX_SOURCE.matcher(regexSource).find();
	}

	public static String replaceCaptures(final String regexSource, final String captureSource,
			final OnigCaptureIndex[] captureIndices) {
		final Matcher m = CAPTURING_REGEX_SOURCE.matcher(regexSource);
		final StringBuilder result = new StringBuilder();
		while (m.find()) {
			final String match = m.group();
			final String replacement = getReplacement(match, captureSource, captureIndices);
			m.appendReplacement(result, replacement);
		}
		m.appendTail(result);
		return result.toString();
	}

	private static String getReplacement(final String match, final String captureSource,
			final OnigCaptureIndex[] captureIndices) {
		final int index;
		final String command;
		final int doublePointIndex = match.indexOf(':');
		if (doublePointIndex != -1) {
			index = Integer.parseInt(match.substring(2, doublePointIndex));
			command = match.substring(doublePointIndex + 2, match.length() - 1);
		} else {
			index = Integer.parseInt(match.substring(1));
			command = null;
		}
		final OnigCaptureIndex capture = captureIndices.length > index ? captureIndices[index] : null;
		if (capture != null) {
			String result = captureSource.substring(capture.start, capture.end);
			// Remove leading dots that would make the selector invalid
			while (!result.isEmpty() && result.charAt(0) == '.') {
				result = result.substring(1);
			}
			if ("downcase".equals(command)) {
				return result.toLowerCase();
			}
			if ("upcase".equals(command)) {
				return result.toUpperCase();
			}
			return result;
		}
		return match;
	}
}
