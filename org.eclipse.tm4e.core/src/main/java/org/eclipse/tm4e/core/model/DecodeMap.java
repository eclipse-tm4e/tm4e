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
package org.eclipse.tm4e.core.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Splitter;

final class DecodeMap {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final Splitter BY_DOT_SPLITTER = Splitter.on('.');

	private int lastAssignedId = 0;
	private final Map<String /* scope */, int @Nullable[] /* ids */ > scopeToTokenIds = new LinkedHashMap<>();
	private final Map<String /* token */, @Nullable Integer /* id */ > tokenToTokenId = new LinkedHashMap<>();
	private final Map<Integer /* id */, String /* id */ > tokenIdToToken = new LinkedHashMap<>();
	TMTokenDecodeData prevToken = new TMTokenDecodeData(EMPTY_STRING_ARRAY,
			new LinkedHashMap<>());

	public int[] getTokenIds(final String scope) {
		int[] tokens = this.scopeToTokenIds.get(scope);
		if (tokens != null) {
			return tokens;
		}
		final String[] tmpTokens = BY_DOT_SPLITTER.splitToStream(scope).toArray(String[]::new);

		tokens = new int[tmpTokens.length];
		for (int i = 0; i < tmpTokens.length; i++) {
			final String token = tmpTokens[i];
			Integer tokenId = this.tokenToTokenId.get(token);
			if (tokenId == null) {
				tokenId = (++this.lastAssignedId);
				this.tokenToTokenId.put(token, tokenId);
				this.tokenIdToToken.put(tokenId, token);
			}
			tokens[i] = tokenId;
		}

		this.scopeToTokenIds.put(scope, tokens);
		return tokens;
	}

	public String getToken(final Map<Integer, Boolean> tokenMap) {
		final StringBuilder result = new StringBuilder();
		boolean isFirst = true;
		for (int i = 1; i <= this.lastAssignedId; i++) {
			if (tokenMap.containsKey(i)) {
				if (isFirst) {
					isFirst = false;
					result.append(this.tokenIdToToken.get(i));
				} else {
					result.append('.');
					result.append(this.tokenIdToToken.get(i));
				}
			}
		}
		return result.toString();
	}
}
