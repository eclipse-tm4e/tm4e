/**
 * Copyright (c) 2023 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke - initial API and implementation
 */
package org.eclipse.tm4e.registry;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.internal.TMScope;

/**
 * TextMate grammar scope API
 */
public interface ITMScope {

	/**
	 * @param scopeName fully qualified ("source.batchfile@com.example.myplugin") or unqualified scopeName ("source.batchfile")
	 */
	static ITMScope parse(final String scopeName) {
		return TMScope.parse(scopeName);
	}

	String getName();

	String getQualifiedName();

	@Nullable
	String getPluginId();

	boolean isQualified();
}
