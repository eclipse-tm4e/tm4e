/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.utils.CompareUtils;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/theme.ts#L304">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/theme.ts</a>
 */
public class ThemeTrieElement {

	private final ThemeTrieElementRule mainRule;
	private final List<ThemeTrieElementRule> rulesWithParentScopes;
	private final Map<String /* segment */, ThemeTrieElement> children;

	public ThemeTrieElement(final ThemeTrieElementRule mainRule) {
		this(mainRule, new ArrayList<>(), new HashMap<>());
	}

	public ThemeTrieElement(final ThemeTrieElementRule mainRule,
			final List<ThemeTrieElementRule> rulesWithParentScopes) {
		this(mainRule, rulesWithParentScopes, new HashMap<>());
	}

	public ThemeTrieElement(final ThemeTrieElementRule mainRule, final List<ThemeTrieElementRule> rulesWithParentScopes,
			final Map<String /* segment */, ThemeTrieElement> children) {
		this.mainRule = mainRule;
		this.rulesWithParentScopes = rulesWithParentScopes;
		this.children = children;
	}

	private static List<ThemeTrieElementRule> sortBySpecificity(final List<ThemeTrieElementRule> arr) {
		if (arr.size() == 1) {
			return arr;
		}
		arr.sort(ThemeTrieElement::cmpBySpecificity);
		return arr;
	}

	private static int cmpBySpecificity(final ThemeTrieElementRule a, final ThemeTrieElementRule b) {
		if (a.scopeDepth == b.scopeDepth) {
			final var aParentScopes = a.parentScopes;
			final var bParentScopes = b.parentScopes;
			final int aParentScopesLen = aParentScopes == null ? 0 : aParentScopes.size();
			final int bParentScopesLen = bParentScopes == null ? 0 : bParentScopes.size();
			if (aParentScopesLen == bParentScopesLen) {
				for (int i = 0; i < aParentScopesLen; i++) {
					@SuppressWarnings("null")
					final String aScope = aParentScopes.get(i);
					@SuppressWarnings("null")
					final String bScope = bParentScopes.get(i);
					final int aLen = aScope.length();
					final int bLen = bScope.length();
					if (aLen != bLen) {
						return bLen - aLen;
					}
				}
			}
			return bParentScopesLen - aParentScopesLen;
		}
		return b.scopeDepth - a.scopeDepth;
	}

	public List<ThemeTrieElementRule> match(final String scope) {
		if ("".equals(scope)) {
			final var arr = new ArrayList<ThemeTrieElementRule>();
			arr.add(this.mainRule);
			arr.addAll(this.rulesWithParentScopes);
			return ThemeTrieElement.sortBySpecificity(arr);
		}

		final int dotIndex = scope.indexOf('.');
		String head;
		String tail;
		if (dotIndex == -1) {
			head = scope;
			tail = "";
		} else {
			head = scope.substring(0, dotIndex);
			tail = scope.substring(dotIndex + 1);
		}

		if (this.children.containsKey(head)) {
			return this.children.get(head).match(tail);
		}

		final var arr = new ArrayList<ThemeTrieElementRule>();
		arr.add(this.mainRule);
		arr.addAll(this.rulesWithParentScopes);
		return ThemeTrieElement.sortBySpecificity(arr);
	}

	public void insert(final int scopeDepth, final String scope, @Nullable final List<String> parentScopes,
			final int fontStyle, final int foreground, final int background) {
		if (scope.isEmpty()) {
			this.doInsertHere(scopeDepth, parentScopes, fontStyle, foreground, background);
			return;
		}

		final int dotIndex = scope.indexOf('.');
		String head;
		String tail;
		if (dotIndex == -1) {
			head = scope;
			tail = "";
		} else {
			head = scope.substring(0, dotIndex);
			tail = scope.substring(dotIndex + 1);
		}

		ThemeTrieElement child;
		if (this.children.containsKey(head)) {
			child = this.children.get(head);
		} else {
			child = new ThemeTrieElement(this.mainRule.clone(),
					ThemeTrieElementRule.cloneArr(this.rulesWithParentScopes));
			this.children.put(head, child);
		}

		child.insert(scopeDepth + 1, tail, parentScopes, fontStyle, foreground, background);
	}

	private void doInsertHere(final int scopeDepth, @Nullable final List<String> parentScopes, int fontStyle,
			int foreground, int background) {

		if (parentScopes == null) {
			// Merge into the main rule
			this.mainRule.acceptOverwrite(scopeDepth, fontStyle, foreground, background);
			return;
		}

		// Try to merge into existing rule
		for (final ThemeTrieElementRule rule : this.rulesWithParentScopes) {
			if (CompareUtils.strArrCmp(rule.parentScopes, parentScopes) == 0) {
				// bingo! => we get to merge this into an existing one
				rule.acceptOverwrite(scopeDepth, fontStyle, foreground, background);
				return;
			}
		}

		// Must add a new rule

		// Inherit from main rule
		if (fontStyle == FontStyle.NotSet) {
			fontStyle = this.mainRule.fontStyle;
		}
		if (foreground == 0) {
			foreground = this.mainRule.foreground;
		}
		if (background == 0) {
			background = this.mainRule.background;
		}

		this.rulesWithParentScopes.add(
				new ThemeTrieElementRule(scopeDepth, parentScopes, fontStyle, foreground, background));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + children.hashCode();
		result = prime * result + mainRule.hashCode();
		result = prime * result + rulesWithParentScopes.hashCode();
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		ThemeTrieElement other = (ThemeTrieElement) obj;
		return children.equals(other.children)
				&& mainRule.equals(other.mainRule)
				&& rulesWithParentScopes.equals(other.rulesWithParentScopes);
	}
}
