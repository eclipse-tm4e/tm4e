/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.model;

import java.time.Duration;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IStateStack;

/**
 * @see <a href="https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages.ts#L87">
 *      github.com/microsoft/vscode/main/src/vs/editor/common/languages.ts</a>
 */
public interface ITokenizationSupport {

	IStateStack getInitialState();

	TokenizationResult tokenize(String line, @Nullable IStateStack state);

	/**
	 * @param offsetDelta adds offsetDelta to each of the returned indices
	 * @param timeLimit duration after which tokenization is stopped
	 */
	TokenizationResult tokenize(String line, @Nullable IStateStack state, int offsetDelta, @Nullable Duration timeLimit);
}
