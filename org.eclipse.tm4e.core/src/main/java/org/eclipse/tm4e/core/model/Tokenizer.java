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
 */
package org.eclipse.tm4e.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.model.DecodeMap;
import org.eclipse.tm4e.core.internal.model.TMTokenDecodeData;

public class Tokenizer implements ITokenizationSupport {

	private final IGrammar grammar;
	private final DecodeMap decodeMap = new DecodeMap();

	public Tokenizer(final IGrammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public TMState getInitialState() {
		return new TMState(null, null);
	}

	@Override
	public LineTokens tokenize(final String line, @Nullable final TMState state) {
		return tokenize(line, state, null, null);
	}

	@Override
	public LineTokens tokenize(final String line, @Nullable final TMState state, @Nullable final Integer offsetDeltaOrNull,
			@Nullable final Integer stopAtOffset) {
		/*
		 Do not attempt to tokenize if a line has over 20k or
		 if the rule stack contains more than 100 rules (indicator of broken grammar that forgets to pop rules)

		 if (line.length >= 20000 || depth(state.ruleStack) > 100) {
		   return new RawLineTokens(
		     [new Token(offsetDelta, '')],
		     [new ModeTransition(offsetDelta, this._modeId)],
		     offsetDelta,
		     state
		   );
		 }
		*/
		final int offsetDelta = offsetDeltaOrNull == null ? 0 : offsetDeltaOrNull;
		final var freshState = state != null ? state.clone() : getInitialState();
		final var textMateResult = grammar.tokenizeLine(line, freshState.getRuleStack());
		freshState.setRuleStack(textMateResult.getRuleStack());

		// Create the result early and fill in the tokens later
		final var tokens = new ArrayList<TMToken>();
		String lastTokenType = null;
		for (int tokenIndex = 0, len = textMateResult.getTokens().length; tokenIndex < len; tokenIndex++) {
			final var token = textMateResult.getTokens()[tokenIndex];
			final int tokenStartIndex = token.getStartIndex();
			final var tokenType = decodeTextMateToken(this.decodeMap, token.getScopes().toArray(String[]::new));

			// do not push a new token if the type is exactly the same (also helps with ligatures)
			if (!tokenType.equals(lastTokenType)) {
				tokens.add(new TMToken(tokenStartIndex + offsetDelta, tokenType));
				lastTokenType = tokenType;
			}
		}
		return new LineTokens(tokens, offsetDelta + line.length(), freshState);

	}

	private String decodeTextMateToken(final DecodeMap decodeMap, final String[] scopes) {
		final String[] prevTokenScopes = decodeMap.prevToken.scopes;
		final int prevTokenScopesLength = prevTokenScopes.length;
		final var prevTokenScopeTokensMaps = decodeMap.prevToken.scopeTokensMaps;

		final var scopeTokensMaps = new LinkedHashMap<Integer, Map<Integer, Boolean>>();
		Map<Integer, Boolean> prevScopeTokensMaps = new LinkedHashMap<>();
		boolean sameAsPrev = true;
		for (int level = 1/* deliberately skip scope 0 */; level < scopes.length; level++) {
			final String scope = scopes[level];

			if (sameAsPrev) {
				if (level < prevTokenScopesLength && prevTokenScopes[level].equals(scope)) {
					prevScopeTokensMaps = prevTokenScopeTokensMaps.get(level);
					scopeTokensMaps.put(level, prevScopeTokensMaps);
					continue;
				}
				sameAsPrev = false;
			}

			final int[] tokens = decodeMap.getTokenIds(scope);
			prevScopeTokensMaps = new LinkedHashMap<>(prevScopeTokensMaps);
			for (final int token : tokens) {
				prevScopeTokensMaps.put(token, true);
			}
			scopeTokensMaps.put(level, prevScopeTokensMaps);
		}

		decodeMap.prevToken = new TMTokenDecodeData(scopes, scopeTokensMaps);
		return decodeMap.getToken(prevScopeTokensMaps);
	}
}
