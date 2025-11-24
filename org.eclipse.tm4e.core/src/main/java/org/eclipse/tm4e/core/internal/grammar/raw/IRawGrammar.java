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

import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/rawGrammar.ts#L8">
 *      github.com/microsoft/vscode-textmate/blob/main/src/rawGrammar.ts</a>
 */
public interface IRawGrammar {

	IRawRepository getRepository();

	String getScopeName();

	@Nullable // TODO non-null in upstream project
	Collection<IRawRule> getPatterns();

	@Nullable
	Map<String, IRawRule> getInjections();

	@Nullable
	String getInjectionSelector();

	Collection<String> getFileTypes();

	@Nullable
	String getName();

	@Nullable
	String getFirstLineMatch();

	void setRepository(IRawRepository repository);

	IRawRule toRawRule();

	@Nullable
	String getFoldingStartMarker(); // custom tm4e code - not in upstream

	@Nullable
	String getFoldingEndMarker(); // custom tm4e code - not in upstream

}
