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
package org.eclipse.tm4e.core.internal.grammar;

import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;

/**
 * Result of the line tokenization implementation.
 *
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/09effd8b7429b71010e0fa34ea2e16e622692946/src/main.ts#L219">
 *      github.com/microsoft/vscode-textmate/blob/main/src/main.ts</a>
 */
final class TokenizeLineResult<T> implements ITokenizeLineResult<T> {

	private final T tokens;
	private final StateStack ruleStack;
	private final boolean stoppedEarly;

	TokenizeLineResult(final T tokens, final StateStack ruleStack, final boolean stoppedEarly) {
		this.tokens = tokens;
		this.ruleStack = ruleStack;
		this.stoppedEarly = stoppedEarly;
	}

	@Override
	public T getTokens() {
		return tokens;
	}

	@Override
	public StateStack getRuleStack() {
		return ruleStack;
	}

	@Override
	public boolean isStoppedEarly() {
		return stoppedEarly;
	}
}
