/**
 * Copyright (c) 2022 Sebastian Thomschke.
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
package org.eclipse.tm4e.core.internal.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/theme.ts#L101">
 *      github.com/microsoft/vscode-textmate/blob/main/src/theme.ts</a>
 */
public final class ScopeStack {

	static @Nullable ScopeStack push(@Nullable ScopeStack path, final List<String> scopeNames) {
		for (final var name : scopeNames) {
			path = new ScopeStack(path, name);
		}
		return path;
	}

	public static ScopeStack from(final String first) {
		return new ScopeStack(null, first);
	}

	public static @Nullable ScopeStack from(final String... segments) {
		ScopeStack result = null;
		for (final String segment : segments) {
			result = new ScopeStack(result, segment);
		}
		return result;
	}

	public final @Nullable ScopeStack parent;
	public final String scopeName;

	ScopeStack(final @Nullable ScopeStack parent, final String scopeName) {
		this.parent = parent;
		this.scopeName = scopeName;
	}

	ScopeStack push(final String scopeName) {
		return new ScopeStack(this, scopeName);
	}

	public List<String> getSegments() {
		@Nullable
		ScopeStack item = this;
		final var result = new ArrayList<String>();
		while (item != null) {
			result.add(item.scopeName);
			item = item.parent;
		}
		Collections.reverse(result);
		return result;
	}

	@Override
	public String toString() {
		return String.join(" ", getSegments());
	}

	public boolean isExtending(final ScopeStack other) {
		if (this == other) {
			return true;
		}

		final var parent = this.parent;
		if (parent == null) {
			return false;
		}
		return parent.isExtending(other);
	}

	List<String> getExtensionIfDefined(final @Nullable ScopeStack base) {
		final var result = new ArrayList<String>();
		@Nullable
		ScopeStack item = this;
		while (item != null && item != base) {
			result.add(item.scopeName);
			item = item.parent;
		}
		if (item == base) {
			Collections.reverse(result);
			return result;
		}
		return Collections.emptyList();
	}
}
