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
package org.eclipse.tm4e.registry.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.ITMScope;

public final class TMScope implements ITMScope {
	private final @Nullable String pluginId; // e.g. "com.example.myplugin"
	private final String name; // e.g. "source.batchfile"
	private final String qualifiedName; //e.g. "source.batchfile@com.example.myplugin"

	public TMScope(String scopeName, @Nullable String pluginId) {
		this.name = scopeName;
		this.pluginId = pluginId;
		qualifiedName = pluginId == null ? scopeName : scopeName + '@' + pluginId;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		final TMScope other = (TMScope) obj;
		return qualifiedName.equals(other.qualifiedName);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public @Nullable String getPluginId() {
		return pluginId;
	}

	@Override
	public String getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public int hashCode() {
		return qualifiedName.hashCode();
	}

	@Override
	public boolean isQualified() {
		return pluginId != null;
	}

	@Override
	public String toString() {
		return qualifiedName;
	}
}
