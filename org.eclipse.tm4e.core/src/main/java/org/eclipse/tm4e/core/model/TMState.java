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
package org.eclipse.tm4e.core.model;

import org.eclipse.tm4e.core.grammar.StackElement;

public class TMState {

	private TMState _parentEmbedderState;
	private StackElement ruleStack;

	public TMState(TMState parentEmbedderState, StackElement ruleStatck) {
		this._parentEmbedderState = parentEmbedderState;
		this.ruleStack = ruleStatck;
	}

	public void setRuleStack(StackElement ruleStack) {
		this.ruleStack = ruleStack;
	}

	public StackElement getRuleStack() {
		return ruleStack;
	}

	public TMState clone() {
		TMState parentEmbedderStateClone = this._parentEmbedderState != null ? this._parentEmbedderState.clone() : null;
		return new TMState(parentEmbedderStateClone, this.ruleStack);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof TMState)) {
			return false;
		}
		TMState otherState = (TMState) other;

		// Equals on `_parentEmbedderState`
		if (!safeEquals(this._parentEmbedderState, otherState._parentEmbedderState)) {
			return false;
		}

		// Equals on `_ruleStack`
		if (this.ruleStack == null && otherState.ruleStack == null) {
			return true;
		}
		if (this.ruleStack == null || otherState.ruleStack == null) {
			return false;
		}
		return this.ruleStack.equals(otherState.ruleStack);
	}
	
	@Override
	public int hashCode() {
		int res = 0;
		if (this._parentEmbedderState != null) {
			res ^= this._parentEmbedderState.hashCode();
		}
		if (this.ruleStack != null) {
			res ^= this.ruleStack.hashCode();
		}
		return super.hashCode();
	}

	public static boolean safeEquals(TMState a, TMState b) {
		if (a == null && b == null) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

}
