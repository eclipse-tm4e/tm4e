/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
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
 * - Sebastian Thomschke - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.registry;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/167bbbd509356cc4617f250c0d754aef670ab14a/src/main.ts#L44">
 *      github.com/microsoft/vscode-textmate/blob/main/src/main.ts</a>
 */
public interface IGrammarConfiguration {

	default @Nullable Map<String, Integer> getEmbeddedLanguages() {
		return null;
	}

	default @Nullable /*SequencedMap*/ Map<String, Integer> getTokenTypes() {
		return null;
	}

	default @Nullable List<String> getBalancedBracketSelectors() {
		return null;
	}

	default @Nullable List<String> getUnbalancedBracketSelectors() {
		return null;
	}
}
