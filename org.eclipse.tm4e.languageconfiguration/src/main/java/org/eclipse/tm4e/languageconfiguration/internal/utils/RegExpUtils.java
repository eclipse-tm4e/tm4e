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
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;

/**
 * Regex utilities.
 */
public final class RegExpUtils {

	/**
	 * Escapes regular expression characters in a given string
	 */
	public static String escapeRegExpCharacters(final String value) {
		return value.replaceAll("[\\-\\\\\\{\\}\\*\\+\\?\\|\\^\\$\\.\\[\\]\\(\\)\\#]", "\\\\$0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Create Java Regexp and null otherwise.
	 *
	 * @return Java Regexp and null otherwise.
	 */
	@Nullable
	public static Pattern create(final String regex) {
		try {
			return Pattern.compile(regex);
		} catch (final Exception ex) {
			LanguageConfigurationPlugin.logError("Failed to parse pattern: " + regex, ex);
			return null;
		}
	}

	private RegExpUtils() {
	}
}
