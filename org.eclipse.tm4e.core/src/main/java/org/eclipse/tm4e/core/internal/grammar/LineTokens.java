/**
 * Copyright (c) 2015-2022 Angelo ZERR.
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
package org.eclipse.tm4e.core.internal.grammar;

import static java.lang.System.Logger.Level.*;
import static org.eclipse.tm4e.core.internal.utils.MoreCollections.*;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.lang.System.Logger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.internal.grammar.tokenattrs.EncodedTokenAttributes;
import org.eclipse.tm4e.core.internal.grammar.tokenattrs.OptionalStandardTokenType;
import org.eclipse.tm4e.core.internal.theme.FontStyle;

final class LineTokens {

	private static final Logger LOGGER = System.getLogger(LineTokens.class.getName());

	private static final Deque<IToken> EMPTY_DEQUE = new ArrayDeque<>(0);

	/**
	 * defined only if `LOGGER.isLoggable(TRACE)`.
	 */
	@Nullable
	private final String lineText;

	/**
	 * used only if `emitBinaryTokens` is false.
	 */
	private final Deque<IToken> tokens;

	private final boolean emitBinaryTokens;

	/**
	 * used only if `emitBinaryTokens` is true.
	 */
	private final List<Integer> binaryTokens;

	private int lastTokenEndIndex = 0;

	private final List<TokenTypeMatcher> tokenTypeOverrides;

	@Nullable
	private final BalancedBracketSelectors balancedBracketSelectors;

	LineTokens(final boolean emitBinaryTokens, final String lineText, final List<TokenTypeMatcher> tokenTypeOverrides,
		@Nullable final BalancedBracketSelectors balancedBracketSelectors) {
		this.emitBinaryTokens = emitBinaryTokens;
		this.lineText = LOGGER.isLoggable(TRACE) ? lineText : null; // store line only if it's logged
		if (this.emitBinaryTokens) {
			this.tokens = EMPTY_DEQUE;
			this.binaryTokens = new ArrayList<>();
		} else {
			this.tokens = new ArrayDeque<>();
			this.binaryTokens = Collections.emptyList();
		}
		this.tokenTypeOverrides = tokenTypeOverrides;
		this.balancedBracketSelectors = balancedBracketSelectors;
	}

	void produce(final StateStack stack, final int endIndex) {
		this.produceFromScopes(stack.contentNameScopesList, endIndex);
	}

	void produceFromScopes(final AttributedScopeStack scopesList, final int endIndex) {
		if (this.lastTokenEndIndex >= endIndex) {
			return;
		}

		if (this.emitBinaryTokens) {
			int metadata = scopesList.metadata;
			var containsBalancedBrackets = false;
			final var balancedBracketSelectors = this.balancedBracketSelectors;
			if (balancedBracketSelectors != null && balancedBracketSelectors.matchesAlways()) {
				containsBalancedBrackets = true;
			}

			if (!tokenTypeOverrides.isEmpty() || balancedBracketSelectors != null
				&& !balancedBracketSelectors.matchesAlways() && !balancedBracketSelectors.matchesNever()) {
				// Only generate scope array when required to improve performance
				final var scopes = scopesList.generateScopes();
				for (final var tokenType : tokenTypeOverrides) {
					if (tokenType.matcher.matches(scopes)) {
						metadata = EncodedTokenAttributes.set(
							metadata,
							0,
							tokenType.type, // toOptionalTokenType(tokenType.type),
							null,
							FontStyle.NotSet,
							0,
							0);
					}
				}
				if (balancedBracketSelectors != null) {
					containsBalancedBrackets = balancedBracketSelectors.match(scopes);
				}
			}

			if (containsBalancedBrackets) {
				metadata = EncodedTokenAttributes.set(
					metadata,
					0,
					OptionalStandardTokenType.NotSet,
					containsBalancedBrackets,
					FontStyle.NotSet,
					0,
					0);
			}

			if (!this.binaryTokens.isEmpty() && getLastElement(this.binaryTokens) == metadata) {
				// no need to push a token with the same metadata
				this.lastTokenEndIndex = endIndex;
				return;
			}

			if (LOGGER.isLoggable(TRACE)) {
				final List<String> scopes = scopesList.generateScopes();
				LOGGER.log(TRACE, "  token: |" +
					castNonNull(this.lineText)
						.substring(this.lastTokenEndIndex >= 0 ? this.lastTokenEndIndex : 0, endIndex)
						.replace("\n", "\\n")
					+ '|');
				for (final String scope : scopes) {
					LOGGER.log(TRACE, "      * " + scope);
				}
			}

			this.binaryTokens.add(this.lastTokenEndIndex);
			this.binaryTokens.add(metadata);

			this.lastTokenEndIndex = endIndex;
			return;
		}

		final List<String> scopes = scopesList.generateScopes();

		if (LOGGER.isLoggable(TRACE)) {
			LOGGER.log(TRACE, "  token: |" +
				castNonNull(this.lineText)
					.substring(this.lastTokenEndIndex >= 0 ? this.lastTokenEndIndex : 0, endIndex)
					.replace("\n", "\\n")
				+ '|');
			for (final String scope : scopes) {
				LOGGER.log(TRACE, "      * " + scope);
			}
		}

		this.tokens.add(new Token(
			this.lastTokenEndIndex >= 0 ? this.lastTokenEndIndex : 0,
			endIndex,
			scopes));

		this.lastTokenEndIndex = endIndex;
	}

	IToken[] getResult(final StateStack stack, final int lineLength) {
		if (!this.tokens.isEmpty() && this.tokens.getLast().getStartIndex() == lineLength - 1) {
			// pop produced token for newline
			this.tokens.removeLast();
		}

		if (this.tokens.isEmpty()) {
			this.lastTokenEndIndex = -1;
			this.produce(stack, lineLength);
			this.tokens.getLast().setStartIndex(0);
		}

		return this.tokens.toArray(IToken[]::new);
	}

	int[] getBinaryResult(final StateStack stack, final int lineLength) {
		if (!this.binaryTokens.isEmpty() && this.binaryTokens.get(binaryTokens.size() - 2) == lineLength - 1) {
			// pop produced token for newline
			removeLastElement(this.binaryTokens);
			removeLastElement(this.binaryTokens);
		}

		if (this.binaryTokens.isEmpty()) {
			this.lastTokenEndIndex = -1;
			this.produce(stack, lineLength);
			this.binaryTokens.set(binaryTokens.size() - 2, 0);
		}

		return binaryTokens.stream().mapToInt(Integer::intValue).toArray();
	}
}
