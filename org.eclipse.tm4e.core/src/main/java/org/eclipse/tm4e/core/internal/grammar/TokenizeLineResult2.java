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

import org.eclipse.tm4e.core.grammar.ITokenizeLineResult2;
import org.eclipse.tm4e.core.grammar.StackElement;

/**
 * 
 * Result of the line tokenization2 implementation.
 *
 */
public class TokenizeLineResult2 implements ITokenizeLineResult2 {

	private final int[] tokens;
	private final StackElement ruleStack;

	public TokenizeLineResult2(int[] tokens, StackElement ruleStack) {
		this.tokens = tokens;
		this.ruleStack = ruleStack;
	}

	@Override
	public int[] getTokens() {
		return tokens;
	}

	@Override
	public StackElement getRuleStack() {
		return ruleStack;
	}

}
