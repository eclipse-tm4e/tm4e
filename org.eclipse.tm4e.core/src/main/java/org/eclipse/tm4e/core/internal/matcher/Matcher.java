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
package org.eclipse.tm4e.core.internal.matcher;

import static java.lang.System.Logger.Level.*;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Matcher utilities.
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/matcher.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/matcher.ts</a>
 */
public final class Matcher<T> {

	private static final Logger LOGGER = System.getLogger(Matcher.class.getName());

	public static List<MatcherWithPriority<List<String>>> createMatchers(final String expression) {
		return new Matcher<>(expression, IMatchesName.NAME_MATCHER).results;
	}

	private final List<MatcherWithPriority<T>> results = new ArrayList<>();
	private final Tokenizer tokenizer;
	private final IMatchesName<T> matchesName;

	@Nullable
	private String token;

	private Matcher(final String expression, final IMatchesName<T> matchesName) {
		tokenizer = new Tokenizer(expression);
		this.matchesName = matchesName;

		// defining local token variable for annotation-based null analysis
		var token = this.token = tokenizer.next();
		while (token != null) {
			int priority = 0;
			if (token.length() == 2 && token.charAt(1) == ':') {
				switch (token.charAt(0)) {
				case 'R':
					priority = 1;
					break;
				case 'L':
					priority = -1;
					break;
				default:
					LOGGER.log(WARNING, "Unknown priority %s in scope selector %s", token, expression);
				}
				this.token = tokenizer.next();
			}
			final Predicate<T> matcher = parseConjunction();
			results.add(new MatcherWithPriority<>(matcher, priority));
			if (!",".equals(this.token)) {
				break;
			}
			token = this.token = tokenizer.next();
		}
	}

	private Predicate<T> parseInnerExpression() {
		final var matchers = new ArrayList<Predicate<T>>();
		Predicate<T> matcher = parseConjunction();
		while (true) {
			matchers.add(matcher);
			if ("|".equals(token) || ",".equals(token)) {
				do {
					token = tokenizer.next();
				} while ("|".equals(token) || ",".equals(token)); // ignore subsequent commas
			} else {
				break;
			}
			matcher = parseConjunction();
		}

		// some (or)
		return matcherInput -> {
			// same as 'matchers.stream().anyMatch(m -> m.test(matcherInput))' but more memory friendly
			for (final Predicate<T> matcher1 : matchers) {
				if (matcher1.test(matcherInput)) {
					return true;
				}
			}
			return false;
		};
	}

	private Predicate<T> parseConjunction() {
		final var matchers = new ArrayList<Predicate<T>>();
		Predicate<T> matcher = parseOperand();
		while (matcher != null) {
			matchers.add(matcher);
			matcher = parseOperand();
		}

		// every (and)
		return matcherInput -> {
			// same as 'matchers.stream().allMatch(m -> m.test(matcherInput))' but more memory friendly
			for (final Predicate<T> matcher1 : matchers) {
				if (!matcher1.test(matcherInput)) {
					return false;
				}
			}
			return true;
		};
	}

	@Nullable
	private Predicate<T> parseOperand() {
		if ("-".equals(token)) {
			token = tokenizer.next();
			final var expressionToNegate = parseOperand();
			return matcherInput -> expressionToNegate != null && !expressionToNegate.test(matcherInput);
		}

		if ("(".equals(token)) {
			token = tokenizer.next();
			final var expressionInParents = parseInnerExpression();
			if (")".equals(token)) {
				token = tokenizer.next();
			}
			return expressionInParents;
		}

		// defining local token variable for annotation-based null analysis
		var token = this.token;
		if (token != null && isIdentifier(token)) {
			final var identifiers = new ArrayList<String>();
			do {
				identifiers.add(token);
				token = this.token = tokenizer.next();
			} while (token != null && isIdentifier(token));
			return matcherInput -> matchesName.match(identifiers, matcherInput);
		}
		return null;
	}

	/**
	 * https://github.com/microsoft/vscode-textmate/blob/master/src/matcher.ts#L89
	 */
	private boolean isIdentifier(String token) {
		if (token.isEmpty())
			return false;

		/* Aprox. 2-3 times faster than:
		 * static final Pattern IDENTIFIER_REGEXP = Pattern.compile("[\\w\\.:]+");
		 * IDENTIFIER_REGEXP.matcher(token).matches();
		 *
		 * Aprox. 10% faster than:
		 * token.chars().allMatch(ch -> ... )
		 */
		for (int i = 0; i < token.length(); i++) {
			final char ch = token.charAt(i);
			if (ch == '.' || ch == ':' || ch == '_'
					|| (ch >= 'a' && ch <= 'z')
					|| (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9'))
				continue;
			return false;
		}
		return true;
	}

	private static final class Tokenizer {

		/**
		 * https://github.com/microsoft/vscode-textmate/blob/master/src/matcher.ts#L94
		 */
		private static final Pattern TOKEN_PATTERN = Pattern
				.compile("([LR]:|[\\w\\.:][\\w\\.:\\-]*|[\\,\\|\\-\\(\\)])");

		private final java.util.regex.Matcher regex;

		private Tokenizer(final String input) {
			regex = TOKEN_PATTERN.matcher(input);
		}

		@Nullable
		private String next() {
			if (regex.find()) {
				return regex.group();
			}
			return null;
		}
	}
}