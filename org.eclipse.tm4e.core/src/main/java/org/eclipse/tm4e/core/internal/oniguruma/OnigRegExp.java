/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/atom/node-oniguruma
 * Initial copyright Copyright (c) 2013 GitHub Inc.
 * Initial license: MIT
 *
 * Contributors:
 * - GitHub Inc.: Initial code, written in JavaScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 * - Fabio Zadrozny <fabiofz@gmail.com> - Convert uniqueId to Object (for identity compare)
 * - Fabio Zadrozny <fabiofz@gmail.com> - Fix recursion error on creation of OnigRegExp with unicode chars
 */
package org.eclipse.tm4e.core.internal.oniguruma;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.internal.utils.StringUtils;
import org.jcodings.specific.NonStrictUTF8Encoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.joni.Syntax;
import org.joni.WarnCallback;
import org.joni.exception.SyntaxException;

/**
 * @see <a href="https://github.com/atom/node-oniguruma/blob/master/src/onig-reg-exp.cc">
 *      github.com/atom/node-oniguruma/blob/master/src/onig-reg-exp.cc</a>
 */
public final class OnigRegExp {
	private static final Logger LOGGER = System.getLogger(OnigRegExp.class.getName());

	/**
	 * {@link WarnCallback} which is used if log level is at least Level.WARNING.
	 */
	private static final WarnCallback LOGGER_WARN_CALLBACK = message -> LOGGER.log(Level.WARNING, message);

	private @Nullable OnigString lastSearchString;
	private int lastSearchPosition = -1;
	private @Nullable OnigResult lastSearchResult;

	private final String pattern;
	private final Regex regex;

	private final boolean hasGAnchor;

	/**
	 * @throws TMException if parsing fails
	 */
	public OnigRegExp(final String pattern) {
		this(pattern, false);
	}

	/**
	 * @throws TMException if parsing fails
	 */
	public OnigRegExp(final String pattern, final boolean ignoreCase) {
		hasGAnchor = pattern.contains("\\G");

		Regex regex;
		try {
			regex = parsePattern(pattern, ignoreCase);
		} catch (final SyntaxException ex) {
			try {
				regex = parsePattern(rewritePatternIfRequired(pattern), ignoreCase);
			} catch (final SyntaxException unused) {
				throw new TMException("Parsing regex pattern \"" + pattern + "\" failed with " + ex, ex);
			}
		}

		this.pattern = pattern;
		this.regex = regex;
	}

	private Regex parsePattern(final String pattern, final boolean ignoreCase) throws SyntaxException {
		int options = Option.CAPTURE_GROUP;
		if (ignoreCase)
			options |= Option.IGNORECASE;
		final byte[] patternBytes = pattern.getBytes(StandardCharsets.UTF_8);
		return new Regex(patternBytes, 0, patternBytes.length, options, NonStrictUTF8Encoding.INSTANCE, Syntax.RUBY,
				LOGGER.isLoggable(Level.WARNING) ? LOGGER_WARN_CALLBACK : WarnCallback.NONE);
	}

	/**
	 * Rewrites the given pattern to work around limitations of the Joni library, which does not support variable-length lookbehinds.
	 *
	 * Strategy:
	 * <ul>
	 * <li>Fixed-length lookbehind (positive or negative) is supported by Joni → leave unchanged.</li>
	 * <li>Variable-length POSITIVE lookbehind at the pattern start → rewritten by <em>consuming</em> the context:<br/>
	 * <code>(?&lt;=X)Y</code> ⇒ <code>(?:X)Y</code><br/>
	 * Trade-off: the overall match (<code>group(0)</code>) shifts left and now includes X, but capture groups remain intact.</li>
	 * <li>Variable-length NEGATIVE lookbehind → no safe generic rewrite (changing it would alter semantics).
	 * These are left unchanged, except for a handful of known safe special cases handled explicitly.</li>
	 * </ul>
	 *
	 * @see <a href="https://github.com/eclipse-tm4e/tm4e/issues/677">github.com/eclipse-tm4e/tm4e/issues/677</a>
	 */
	private String rewritePatternIfRequired(final String pattern) {
		if (pattern.isEmpty())
			return pattern;

		// --- Positive lookbehinds --------------------------------------------------
		// Joni supports fixed-length positive lookbehind, but not variable-length.
		// If the pattern starts with (?<=...), inspect its body:
		//  - Fixed-length → keep as-is.
		//  - Variable-length → rewrite by consuming the context: (?<=X)Y  ==>  (?:X)Y
		//    (group numbering is preserved; only group(0) widens to include X).
		if (pattern.startsWith("(?<=")) {
			final int close = findBalancedGroupEnd(pattern, 0); // index of ')' closing (?<=...)
			if (close > 0) {
				final String body = pattern.substring("(?<=".length(), close);

				if (isFixedLength(body))
					return pattern; // supported as-is

				// Variable-length positive lookbehind: consume the prefix so the pattern runs on Joni.
				return "(?:" + body + ")" + pattern.substring(close + 1);
			}
			// Unbalanced (?<=... → leave unchanged
		}

		// --- Negative lookbehinds --------------------------------------------------
		// We intentionally DO NOT apply a generic rewrite for variable-length NEGATIVE lookbehind:
		//   - Fixed-length negative LB is Joni-compatible → leave unchanged.
		//   - Variable-length negative LB has no semantics-preserving generic rewrite.
		// If you encounter real-world cases, add targeted rewrites below.

		// Used in csharp.tmLanguage.json: (?<!\.\s*) ==> (?<!\.)\s*
		// Rationale: move the variable portion (\s*) out of the lookbehind while keeping intent.
		final var negLB = "(?<!\\.\\s*)";
		if (pattern.startsWith(negLB))
			return "(?<!\\.)\\s*" + pattern.substring(negLB.length());

		return pattern;
	}

	/**
	 * Returns the index of the ')' that closes the group whose '(' is at 'start'.
	 * Assumes 'start' points at '(' of a construct like (?<=...) or (?<!...).
	 * Returns -1 if unbalanced.
	 */
	private int findBalancedGroupEnd(final String str, final int start) {
		int depth = 0;
		boolean escaped = false;
		for (int idx = start; idx < str.length(); idx++) {
			final char c = str.charAt(idx);
			if (escaped) {
				escaped = false;
				continue;
			}
			if (c == '\\') {
				escaped = true;
				continue;
			}
			if (c == '(') {
				depth++;
			} else if (c == ')') {
				depth--;
				if (depth == 0)
					return idx;
			}
		}
		return -1;
	}

	/**
	 * Fixed-length detector for a lookbehind body.
	 * Returns false if it contains obvious variable-length features:
	 * - unescaped *, +, ?, or alternation '|'
	 * - {m,} (open upper bound) or {m,n} with m != n
	 * If unsure, returns false.
	 */
	private boolean isFixedLength(final String body) {
		boolean escaped = false;
		for (int idx = 0; idx < body.length(); idx++) {
			final char ch = body.charAt(idx);
			if (escaped) {
				escaped = false;
				continue;
			}
			if (ch == '\\') {
				escaped = true;
				continue;
			}
			if (ch == '*' || ch == '+' || ch == '?' || ch == '|')
				return false;
			if (ch == '{') {
				int j = idx + 1;
				while (j < body.length() && Character.isDigit(body.charAt(j))) {
					j++;
				}
				if (j == idx + 1)
					return false; // not {m
				int m;
				try {
					m = Integer.parseInt(body.substring(idx + 1, j));
				} catch (final NumberFormatException e) {
					return false;
				}
				int n = m;
				if (j < body.length() && body.charAt(j) == ',') {
					j++;
					int k = j;
					while (k < body.length() && Character.isDigit(body.charAt(k))) {
						k++;
					}
					if (k == j)
						return false; // {m,}
					try {
						n = Integer.parseInt(body.substring(j, k));
					} catch (final NumberFormatException e) {
						return false;
					}
					if (m != n)
						return false; // {m,n} with m != n
					j = k;
				}
				final int close = body.indexOf('}', j);
				if (close < 0)
					return false; // malformed
				idx = close;
			}
		}
		return true;
	}

	/**
	 * @return null if not found
	 */
	public @Nullable OnigResult search(final OnigString str, final int startPosition) {
		if (hasGAnchor) {
			// Should not use caching, because the regular expression
			// targets the current search position (\G)
			return search(str.bytesUTF8, startPosition, str.bytesCount);
		}

		final var lastSearchResult0 = this.lastSearchResult;
		if (lastSearchString == str
				&& lastSearchPosition <= startPosition
				&& (lastSearchResult0 == null || lastSearchResult0.locationAt(0) >= startPosition)) {
			return lastSearchResult0;
		}

		lastSearchString = str;
		lastSearchPosition = startPosition;
		lastSearchResult = search(str.bytesUTF8, startPosition, str.bytesCount);
		return lastSearchResult;
	}

	private @Nullable OnigResult search(final byte[] data, final int startPosition, final int end) {
		final Matcher matcher = regex.matcher(data);
		final int status = matcher.search(startPosition, end, Option.DEFAULT);
		if (status != Matcher.FAILED) {
			final Region region = matcher.getEagerRegion();
			return new OnigResult(region, -1);
		}
		return null;
	}

	public String pattern() {
		return pattern;
	}

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> {
			sb.append("pattern=").append(pattern);
		});
	}
}
