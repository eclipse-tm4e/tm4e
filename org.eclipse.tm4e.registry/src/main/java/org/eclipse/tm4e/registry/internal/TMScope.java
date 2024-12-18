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

import static org.eclipse.tm4e.core.internal.utils.ScopeNames.CONTRIBUTOR_SEPARATOR;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.ITMScope;

public final class TMScope implements ITMScope {

	/**
	 * @param scopeName fully qualified ("source.batchfile@com.example.myplugin") or unqualified scopeName ("source.batchfile")
	 */
	public static TMScope parse(final String scopeName) {
		final int separatorAt = scopeName.indexOf(CONTRIBUTOR_SEPARATOR);
		if (separatorAt == -1) {
			return new TMScope(scopeName, null, scopeName);
		}
		return new TMScope(scopeName.substring(0, separatorAt), scopeName.substring(separatorAt + 1), scopeName);
	}

	private final @Nullable String pluginId; // e.g. "com.example.myplugin"
	private final String name; // e.g. "source.batchfile"
	private final String qualifiedName; //e.g. "source.batchfile@com.example.myplugin"

	/**
	 * @param scopeName the scope name, e.g. "source.batchfile"
	 * @param pluginId id of the grammar contributing pluginId, e.g. "com.example.myplugin"
	 */
	public TMScope(String scopeName, @Nullable String pluginId) {
		this.name = scopeName;
		this.pluginId = pluginId;
		qualifiedName = pluginId == null ? scopeName : scopeName + CONTRIBUTOR_SEPARATOR + pluginId;
	}

	private TMScope(final String scopeName, final @Nullable String pluginId, final String qualifiedName) {
		this.name = scopeName;
		this.pluginId = pluginId;
		this.qualifiedName = qualifiedName;
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		return obj instanceof TMScope other
				? qualifiedName.equals(other.qualifiedName)
				: false;
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
