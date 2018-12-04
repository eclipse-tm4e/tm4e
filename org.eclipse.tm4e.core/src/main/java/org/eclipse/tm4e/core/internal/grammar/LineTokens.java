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
package org.eclipse.tm4e.core.internal.grammar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.logger.ILogger;

class LineTokens {

	private final ILogger logger;
	
	private final String _lineText;
	
	/**
	 * used only if `_emitBinaryTokens` is false.
	 */
	private final List<IToken> _tokens;
	
	
	private boolean _emitBinaryTokens;
	
	/**
	 * used only if `_emitBinaryTokens` is true.
	 */
	private final List<Integer> _binaryTokens;
	
	private int _lastTokenEndIndex;
	
	LineTokens(boolean emitBinaryTokens, String lineText, ILogger logger) {
		this._emitBinaryTokens = emitBinaryTokens;		
		this._lineText = logger.isEnabled() ? lineText : null;		
		if (this._emitBinaryTokens) {
			this._tokens = null;
			this._binaryTokens = new ArrayList<>();
		} else {
			this._tokens = new ArrayList<>();
			this._binaryTokens = null;
		}
		this._lastTokenEndIndex = 0;
		this.logger = logger;
	}

	public void produce(StackElement stack, int endIndex) {
		this.produceFromScopes(stack.contentNameScopesList, endIndex);
	}

	public void produceFromScopes(ScopeListElement scopesList, int endIndex) {
		if (this._lastTokenEndIndex >= endIndex) {
			return;
		}

		if (this._emitBinaryTokens) {
			int metadata = scopesList.metadata;
			if (this._binaryTokens.size() > 0 && this._binaryTokens.get(this._binaryTokens.size() - 1) == metadata) {
				// no need to push a token with the same metadata
				this._lastTokenEndIndex = endIndex;
				return;
			}

			this._binaryTokens.add(this._lastTokenEndIndex);
			this._binaryTokens.add(metadata);

			this._lastTokenEndIndex = endIndex;
			return;
		}

		List<String> scopes = scopesList.generateScopes();

		if (logger.isEnabled()) {
			logger.log("  token: |" + this._lineText.substring(this._lastTokenEndIndex, endIndex).replaceAll("\n", "\\n") + '|');
			for (int k = 0; k < scopes.size(); k++) {
				logger.log("      * " + scopes.get(k));
			}
		}
		this._tokens.add(new Token(this._lastTokenEndIndex, endIndex, scopes));

		this._lastTokenEndIndex = endIndex;
	}
	
	public IToken[] getResult(StackElement stack, int lineLength) {
		if (this._tokens.size() > 0 && this._tokens.get(this._tokens.size() - 1).getStartIndex() == lineLength - 1) {
			// pop produced token for newline
			this._tokens.remove(this._tokens.size() - 1);
		}

		if (this._tokens.size() == 0) {
			this._lastTokenEndIndex = -1;
			this.produce(stack, lineLength);
			this._tokens.get(this._tokens.size() - 1).setStartIndex(0);
		}

		return this._tokens.toArray(new IToken[0]);
	}
	
	public ILogger getLogger() {
		return logger;
	}

	public int[] getBinaryResult(StackElement stack, int lineLength) {
		if (this._binaryTokens.size() > 0 && this._binaryTokens.get(this._binaryTokens.size() - 2) == lineLength - 1) {
			// pop produced token for newline
			this._binaryTokens.remove(this._binaryTokens.size() - 1);
			this._binaryTokens.remove(this._binaryTokens.size() - 1);
		}

		if (this._binaryTokens.size() == 0) {
			this._lastTokenEndIndex = -1;
			this.produce(stack, lineLength);
			this._binaryTokens.set(this._binaryTokens.size() - 2, 0);
		}

		int[] result = new int[this._binaryTokens.size()];
		for (int i = 0, len = this._binaryTokens.size(); i < len; i++) {
			result[i] = this._binaryTokens.get(i);
		}

		return result;
	}
}
