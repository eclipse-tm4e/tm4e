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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public class LineTokens {

	final List<TMToken> tokens;
	int actualStopOffset;

	@Nullable
	TMState endState;

	public LineTokens(final List<TMToken> tokens, final int actualStopOffset, @Nullable final TMState endState) {
		this.tokens = tokens;
		this.actualStopOffset = actualStopOffset;
		this.endState = endState;
	}

	@Nullable
	public TMState getEndState() {
		return endState;
	}

	public void setEndState(@Nullable  final TMState endState) {
		this.endState = endState;
	}

	public List<TMToken> getTokens() {
		return tokens;
	}
}
