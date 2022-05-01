/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
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
 * - Sebastian Thomschke - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.grammar;

import java.util.List;

import org.eclipse.tm4e.core.internal.matcher.Matcher;

public interface TokenTypeMatcher {

	Matcher<List<String>> getMatcher();

	int /*StandardTokenType*/ getType();
}
