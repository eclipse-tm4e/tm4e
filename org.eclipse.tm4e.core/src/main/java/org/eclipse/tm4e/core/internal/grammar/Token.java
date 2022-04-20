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
package org.eclipse.tm4e.core.internal.grammar;

import java.util.List;

import org.eclipse.tm4e.core.grammar.IToken;

final class Token implements IToken {

	private int startIndex;

	private final int endIndex;

	private final List<String> scopes;

	Token(int startIndex, int endIndex, List<String> scopes) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.scopes = scopes;
	}

	@Override
	public int getStartIndex() {
		return startIndex;
	}

	@Override
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	@Override
	public int getEndIndex() {
		return endIndex;
	}

	@Override
	public List<String> getScopes() {
		return scopes;
	}

	@Override
	public String toString() {
		return "{" +
				"startIndex: " + startIndex +
				", endIndex: " + endIndex +
				", scopes: " + scopes +
				"}";
	}
}
