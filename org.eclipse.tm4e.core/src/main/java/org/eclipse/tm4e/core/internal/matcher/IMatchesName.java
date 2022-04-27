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
package org.eclipse.tm4e.core.internal.matcher;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

interface IMatchesName<T> {

	IMatchesName<List<String>> NAME_MATCHER = new IMatchesName<>() {

		@Override
		public boolean match(final Collection<String> identifers, final List<String> scopes) {
			if (scopes.size() < identifers.size()) {
				return false;
			}
			final int[] lastIndex = { 0 };
			return identifers.stream().allMatch(identifier -> {
				for (int i = lastIndex[0]; i < scopes.size(); i++) {
					if (scopesAreMatching(scopes.get(i), identifier)) {
						lastIndex[0]++;
						return true;
					}
				}
				return false;
			});
		}

		private boolean scopesAreMatching(@Nullable final String thisScopeName, final String scopeName) {
			if (thisScopeName == null) {
				return false;
			}
			if (thisScopeName.equals(scopeName)) {
				return true;
			}
			int len = scopeName.length();
			return thisScopeName.length() > len
					&& thisScopeName.substring(0, len).equals(scopeName)
					&& thisScopeName.charAt(len) == '.';
		}
	};

	boolean match(Collection<String> names, T scopes);
}