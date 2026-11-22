/**
 * Copyright (c) 2026 Sergii Zolotarov and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import static java.util.function.Predicate.not;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Converts a JavaScript regular expression, as used in VS Code language-configuration.json files, to a Java
 * {@link Pattern}.
 * <p>
 * The syntax of the two languages is almost the same. This class changes only the parts that have a different meaning
 * in Java:
 * <ul>
 * <li>Unicode property names that Java does not know, for example {@code \p{Alphabetic}} and
 * {@code \p{Nonspacing_Mark}}</li>
 * <li>{@code \s} and {@code \S}: in JavaScript, {@code \s} also contains the Unicode space characters</li>
 * <li>an optional capturing group, for example {@code (a)?}, when the expression has a back-reference: in JavaScript, a
 * back-reference to a group that did not match is empty, but in Java it does not match</li>
 * </ul>
 * Other differences are not converted. Java cannot compile most of them, so {@link #toPattern(String, String)} throws an
 * exception and does not give a wrong result.
 */
public final class JavaScriptRegExp {

	/** The characters of the JavaScript {@code \s} that the Java {@code \s} does not contain. */
	private static final String MORE_SPACES = "\\u00A0\\u1680\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000\\uFEFF"; //$NON-NLS-1$

	/** Long names and aliases of the Unicode general categories, which Java knows only by their short names. */
	private static final Map<String, String> PROPERTIES = toMap( //
			"""
				Letter=L Cased_Letter=LC Uppercase_Letter=Lu Lowercase_Letter=Ll
				Titlecase_Letter=Lt Modifier_Letter=Lm Other_Letter=Lo
				Mark=M Combining_Mark=M Nonspacing_Mark=Mn Spacing_Mark=Mc Enclosing_Mark=Me
				Number=N Decimal_Number=Nd digit=Nd Letter_Number=Nl Other_Number=No
				Punctuation=P punct=P Connector_Punctuation=Pc Dash_Punctuation=Pd Open_Punctuation=Ps
				Close_Punctuation=Pe Initial_Punctuation=Pi Final_Punctuation=Pf Other_Punctuation=Po
				Symbol=S Math_Symbol=Sm Currency_Symbol=Sc Modifier_Symbol=Sk Other_Symbol=So
				Separator=Z Space_Separator=Zs Line_Separator=Zl Paragraph_Separator=Zp
				Other=C Control=Cc cntrl=Cc Format=Cf Surrogate=Cs Private_Use=Co Unassigned=Cn
				"""); //$NON-NLS-1$

	/** @param data {@code longName=shortName} pairs, separated by white space */
	private static Map<String, String> toMap(final String data) {
		return Stream.of(data.trim().split("\\s")).filter(not(String::isBlank)).map(entry -> entry.trim().split("="))
				.collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
	}

	/**
	 * @param source the JavaScript regular expression, without the enclosing slashes
	 * @param flags the JavaScript flags, for example {@code "iu"}, or {@code null}
	 *
	 * @throws PatternSyntaxException if Java cannot compile the regular expression
	 */
	public static Pattern toPattern(final String source, final @Nullable String flags) {
		int javaFlags = 0;
		if (flags != null) {
			if (flags.indexOf('i') >= 0) {
				// JavaScript ignores the case of all letters, not only of ASCII letters
				javaFlags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
			}
			if (flags.indexOf('m') >= 0) {
				javaFlags |= Pattern.MULTILINE;
			}
			if (flags.indexOf('s') >= 0) {
				javaFlags |= Pattern.DOTALL;
			}
			// The flags g, y and d change only how JavaScript runs a match, not the syntax.
		}
		return Pattern.compile(convert(source), javaFlags);
	}

	private static String convert(final String source) {
		final boolean hasBackReference = hasBackReference(source);
		final var result = new StringBuilder(source.length() + 16);
		/* for each open group: the position in the result where "(?:" goes if the group is optional, or -1 */
		final var groups = new ArrayDeque<Integer>();
		boolean inClass = false;
		final int length = source.length();
		for (int i = 0; i < length; i++) {
			final char c = source.charAt(i);
			if (c == '\\' && i + 1 < length) {
				final char next = source.charAt(i + 1);
				if ((next == 'p' || next == 'P') && i + 2 < length && source.charAt(i + 2) == '{'
						&& source.indexOf('}', i + 3) > 0) {
					final int end = source.indexOf('}', i + 3);
					result.append('\\').append(next).append('{')
							.append(toJavaPropertyName(source.substring(i + 3, end)))
							.append('}');
					i = end;
				} else if (next == 's') {
					result.append(inClass ? "\\s" + MORE_SPACES : "[\\s" + MORE_SPACES + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					i++;
				} else if (next == 'S') {
					result.append("[^\\s" + MORE_SPACES + "]"); //$NON-NLS-1$ //$NON-NLS-2$
					i++;
				} else {
					// copy the escape sequence as it is, so that an escaped backslash is not read as the start of an escape
					result.append(c).append(next);
					i++;
				}
			} else if (inClass) {
				inClass = c != ']';
				result.append(c);
			} else if (c == '[') {
				inClass = true;
				result.append(c);
			} else if (c == '(') {
				final boolean named = source.startsWith("(?<", i) && !source.startsWith("(?<=", i) //$NON-NLS-1$ //$NON-NLS-2$
						&& !source.startsWith("(?<!", i); //$NON-NLS-1$
				final int nameEnd = named ? source.indexOf('>', i) : -1;
				if (named && nameEnd > 0) {
					result.append(source, i, nameEnd + 1);
					i = nameEnd;
					groups.push(result.length());
				} else if (i + 1 < length && source.charAt(i + 1) == '?') {
					result.append(c);
					groups.push(-1); // not a capturing group
				} else {
					result.append(c);
					groups.push(result.length());
				}
			} else if (c == ')') {
				final int content = groups.isEmpty() ? -1 : groups.pop();
				if (hasBackReference && content >= 0 && i + 1 < length && source.charAt(i + 1) == '?') {
					// (X)? becomes ((?:X)?): the group always takes part in the match, so a back-reference to it can be empty
					result.insert(content, "(?:"); //$NON-NLS-1$
					result.append(")?"); //$NON-NLS-1$
					i++;
					if (i + 1 < length && source.charAt(i + 1) == '?') {
						result.append('?'); // lazy
						i++;
					}
				}
				result.append(c);
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private static boolean hasBackReference(final String source) {
		for (int i = 0; i + 1 < source.length(); i++) {
			if (source.charAt(i) == '\\') {
				final char next = source.charAt(i + 1);
				if (next >= '1' && next <= '9' || next == 'k') {
					return true;
				}
				i++;
			}
		}
		return false;
	}

	private static String toJavaPropertyName(final String name) {
		final int equals = name.indexOf('=');
		if (equals >= 0) {
			final String key = name.substring(0, equals);
			final String value = name.substring(equals + 1);
			if ("General_Category".equals(key) || "gc".equals(key)) { //$NON-NLS-1$ //$NON-NLS-2$
				return "gc=" + PROPERTIES.getOrDefault(value, value); //$NON-NLS-1$
			}
			return name; // Java knows Script= and sc=
		}
		final String javaName = PROPERTIES.get(name);
		if (javaName != null) {
			return javaName;
		}
		if (name.length() <= 2) {
			return name; // the short name of a general category, for example L or Lu
		}
		return "Is" + name; // a binary property, for example Alphabetic or White_Space //$NON-NLS-1$
	}

	private JavaScriptRegExp() {
	}
}
