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
package org.eclipse.tm4e.core.internal.types;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.grammar.RawRepository;
import org.eclipse.tm4e.core.internal.parser.PropertySettable;

public interface IRawRepository {

	static IRawRepository merge(@Nullable IRawRepository... sources) {
		final RawRepository merged = new RawRepository();
		for (final var source : sources) {
			if (source == null)
				continue;
			source.putEntries(merged);
		}
		return merged;
	}

	void putEntries(PropertySettable<IRawRule> target);

	@Nullable
	IRawRule getRule(String name);

	IRawRule getBase();

	IRawRule getSelf();

	void setSelf(IRawRule raw);

	void setBase(IRawRule base);
}
