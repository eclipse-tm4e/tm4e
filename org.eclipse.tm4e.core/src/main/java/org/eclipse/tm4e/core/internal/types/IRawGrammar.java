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
package org.eclipse.tm4e.core.internal.types;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/rawGrammar.ts#L15">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/rawGrammar.ts</a>
 */
public interface IRawGrammar {

	IRawGrammar clone();

	@Nullable
	IRawRepository getRepository();

	IRawRepository getRepositorySafe();

	String getScopeName();

	@Nullable
	Collection<IRawRule> getPatterns();

	@Nullable
	Map<String, IRawRule> getInjections();

	@Nullable
	String getInjectionSelector();

	// injections?:{ [expression:string]: IRawRule };

	Collection<String> getFileTypes();

	@Nullable
	String getName();

	@Nullable
	String getFirstLineMatch();
}
