/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
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
 *  - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Matcher utilities.
 *
 * @see <a href="https://github.com/Microsoft/vscode-textmate/blob/master/src/matcher.ts">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/matcher.ts</a>
 *
 */
public final class Matcher<T> implements Predicate<T> {

	private static final Pattern IDENTIFIER_REGEXP = Pattern.compile("[\\w\\.:]+");

	public static Collection<MatcherWithPriority<List<String>>> createMatchers(String expression) {
		return createMatchers(expression, IMatchesName.NAME_MATCHER);
	}

	private static <T> Collection<MatcherWithPriority<T>> createMatchers(String selector, IMatchesName<T> matchesName) {
		return new Matcher<>(selector, matchesName).results;
	}

	private final List<MatcherWithPriority<T>> results = new ArrayList<>();
	private final Tokenizer tokenizer;
	private final IMatchesName<T> matchesName;
	private String token;

	private Matcher(String expression, IMatchesName<T> matchesName) {
		this.tokenizer = new Tokenizer(expression);
		this.matchesName = matchesName;

		this.token = tokenizer.next();
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
					// console.log(`Unknown priority ${token} in scope selector`);
				}
				token = tokenizer.next();
			}
			Predicate<T> matcher = parseConjunction();
			if (matcher != null) {
				results.add(new MatcherWithPriority<>(matcher, priority));
			}
			if (!",".equals(token)) {
				break;
			}
			token = tokenizer.next();
		}
	}

	private Predicate<T> parseInnerExpression() {
		List<Predicate<T>> matchers = new ArrayList<>();
		Predicate<T> matcher = parseConjunction();
		while (matcher != null) {
			matchers.add(matcher);
			if ("|".equals(token) || ",".equals(token)) {
				do {
					token = tokenizer.next();
				} while ("|".equals(token) || ",".equals(token)); // ignore subsequent
				// commas
			} else {
				break;
			}
			matcher = parseConjunction();
		}
		// some (or)
		return matcherInput -> {
			for (Predicate<T> matcher1 : matchers) {
				if (matcher1.test(matcherInput)) {
					return true;
				}
			}
			return false;
		};
	}

	private Predicate<T> parseConjunction() {
		List<Predicate<T>> matchers = new ArrayList<>();
		Predicate<T> matcher = parseOperand();
		while (matcher != null) {
			matchers.add(matcher);
			matcher = parseOperand();
		}
		// every (and)
		return matcherInput -> {
			for (Predicate<T> matcher1 : matchers) {
				if (!matcher1.test(matcherInput)) {
					return false;
				}
			}
			return true;
		};
	}

	private Predicate<T> parseOperand() {
		if ("-".equals(token)) {
			token = tokenizer.next();
			Predicate<T> expressionToNegate = parseOperand();
			return matcherInput -> {
				if (expressionToNegate == null) {
					return false;
				}
				return !expressionToNegate.test(matcherInput);
			};
		}
		if ("(".equals(token)) {
			token = tokenizer.next();
			Predicate<T> expressionInParents = parseInnerExpression();
			if (")".equals(token)) {
				token = tokenizer.next();
			}
			return expressionInParents;
		}
		if (isIdentifier(token)) {
			Collection<String> identifiers = new ArrayList<>();
			do {
				identifiers.add(token);
				token = tokenizer.next();
			} while (isIdentifier(token));
			return matcherInput -> Matcher.this.matchesName.match(identifiers, matcherInput);
		}
		return null;
	}

	private boolean isIdentifier(String token) {
		return token != null && IDENTIFIER_REGEXP.matcher(token).matches();
	}

	@Override
	public boolean test(T matcherInput) {
		return false;
	}

	private static final class Tokenizer {

		private static final Pattern REGEXP = Pattern.compile("([LR]:|[\\w\\.:]+|[\\,\\|\\-\\(\\)])");

		private final java.util.regex.Matcher regex;

		private Tokenizer(String input) {
			this.regex = REGEXP.matcher(input);
		}

		private String next() {
			if (regex.find()) {
				return regex.group();
			}
			return null;
		}
	}

}