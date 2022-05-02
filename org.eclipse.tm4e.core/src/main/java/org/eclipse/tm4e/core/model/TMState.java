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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IStackElement;

public class TMState {

	@Nullable
	private final TMState parentEmbedderState;

	@Nullable
	private IStackElement ruleStack;

	public TMState(@Nullable final TMState parentEmbedderState, @Nullable final IStackElement ruleStatck) {
		this.parentEmbedderState = parentEmbedderState;
		this.ruleStack = ruleStatck;
	}

	public void setRuleStack(final IStackElement ruleStack) {
		this.ruleStack = ruleStack;
	}

	@Nullable
	public IStackElement getRuleStack() {
		return ruleStack;
	}

	@Override
	public TMState clone() {
		final TMState parentEmbedderStateClone = this.parentEmbedderState != null
				? this.parentEmbedderState.clone()
				: null;
		return new TMState(parentEmbedderStateClone, this.ruleStack);
	}

	@Override
	public boolean equals(@Nullable final Object other) {
		if (!(other instanceof TMState)) {
			return false;
		}
		final TMState otherState = (TMState) other;
		return Objects.equals(this.parentEmbedderState, otherState.parentEmbedderState)
				&& Objects.equals(this.ruleStack, otherState.ruleStack);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.parentEmbedderState, this.ruleStack);
	}
}
