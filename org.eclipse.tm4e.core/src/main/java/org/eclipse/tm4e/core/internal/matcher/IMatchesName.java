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
package org.eclipse.tm4e.core.internal.matcher;

import java.util.Collection;
import java.util.List;

public interface IMatchesName<T> {

	class IntegerHolder {

		public int value;

		public IntegerHolder() {
			this.value = 0;
		}
	}

	public static final IMatchesName<List<String>> NAME_MATCHER = new IMatchesName<List<String>>() {

		@Override
		public boolean match(Collection<String> identifers, List<String> scopes) {
			if (scopes.size() < identifers.size()) {
				return false;
			}
			IntegerHolder lastIndex = new IntegerHolder();
			// every
			return identifers.stream().allMatch(identifier -> {
				for (int i = lastIndex.value; i < scopes.size(); i++) {
					if (scopesAreMatching(scopes.get(i), identifier)) {
						lastIndex.value = i + 1;
						return true;
					}
				}
				return false;
			});
		}

		private boolean scopesAreMatching(String thisScopeName, String scopeName) {
			if (thisScopeName == null) {
				return false;
			}
			if (thisScopeName.equals(scopeName)) {
				return true;
			}
			int len = scopeName.length();
			return thisScopeName.length() > len && thisScopeName.substring(0, len).equals(scopeName)
					&& thisScopeName.charAt(len) == '.';
		}

	};

	boolean match(Collection<String> names, T scopes);

}