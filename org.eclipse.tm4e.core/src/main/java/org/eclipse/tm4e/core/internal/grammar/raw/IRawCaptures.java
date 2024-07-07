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
package org.eclipse.tm4e.core.internal.grammar.raw;

import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/167bbbd509356cc4617f250c0d754aef670ab14a/src/rawGrammar.ts#L62">
 *      github.com/microsoft/vscode-textmate/blob/main/src/rawGrammar.ts</a>
 */
public interface IRawCaptures {

	@Nullable
	IRawRule getCapture(String captureId);

	Iterable<String> getCaptureIds();

	void forEachCapture(BiConsumer<String, IRawRule> action);
}
