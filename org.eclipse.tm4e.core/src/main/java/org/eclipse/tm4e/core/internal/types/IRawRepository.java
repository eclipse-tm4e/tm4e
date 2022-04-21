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

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.tm4e.core.internal.grammar.parser.Raw;

public interface IRawRepository {

	static IRawRepository merge(IRawRepository... sources) {
	   final Raw merged = new Raw();
		for (final IRawRepository source : sources) {
		   final Set<Entry<String, Object>> entries = source.entrySet();
			for (final Entry<String, Object> entry : entries) {
				merged.put(entry.getKey(), entry.getValue());
			}
		}
		return merged;
	}

	Set<Map.Entry<String, Object>> entrySet();

	IRawRule getProp(String name);

	IRawRule getBase();

	IRawRule getSelf();

	void setSelf(IRawRule raw);

	void setBase(IRawRule base);
}
