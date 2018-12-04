/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm4e.core.internal.utils.CompareUtils;

public class ThemeTrieElement {

	// _themeTrieElementBrand: void;

	private final ThemeTrieElementRule _mainRule;
	private final List<ThemeTrieElementRule> _rulesWithParentScopes;
	private final Map<String /* segment */, ThemeTrieElement> _children;

	public ThemeTrieElement(ThemeTrieElementRule mainRule) {
		this(mainRule, new ArrayList<>(), new HashMap<>());
	}

	public ThemeTrieElement(ThemeTrieElementRule mainRule, List<ThemeTrieElementRule> rulesWithParentScopes) {
		this(mainRule, rulesWithParentScopes, new HashMap<>());
	}

	public ThemeTrieElement(ThemeTrieElementRule mainRule, List<ThemeTrieElementRule> rulesWithParentScopes,
			Map<String /* segment */, ThemeTrieElement> children) {
		this._mainRule = mainRule;
		this._rulesWithParentScopes = rulesWithParentScopes;
		this._children = children;
	}

	private static List<ThemeTrieElementRule> _sortBySpecificity(List<ThemeTrieElementRule> arr) {
		if (arr.size() == 1) {
			return arr;
		}

		arr.sort((a, b) -> _cmpBySpecificity(a, b));

		return arr;
	}

	private static int _cmpBySpecificity(ThemeTrieElementRule a, ThemeTrieElementRule b) {
		if (a.scopeDepth == b.scopeDepth) {
			List<String> aParentScopes = a.parentScopes;
			List<String> bParentScopes = b.parentScopes;
			int aParentScopesLen = aParentScopes == null ? 0 : aParentScopes.size();
			int bParentScopesLen = bParentScopes == null ? 0 : bParentScopes.size();
			if (aParentScopesLen == bParentScopesLen) {
				for (int i = 0; i < aParentScopesLen; i++) {
					int aLen = aParentScopes.get(i).length();
					int bLen = bParentScopes.get(i).length();
					if (aLen != bLen) {
						return bLen - aLen;
					}
				}
			}
			return bParentScopesLen - aParentScopesLen;
		}
		return b.scopeDepth - a.scopeDepth;
	}

	public List<ThemeTrieElementRule> match(String scope) {
		if ("".equals(scope)) {
			List<ThemeTrieElementRule> arr = new ArrayList<>();
			arr.add(this._mainRule);
			arr.addAll(this._rulesWithParentScopes);
			return ThemeTrieElement._sortBySpecificity(arr);
		}

		int dotIndex = scope.indexOf('.');
		String head;
		String tail;
		if (dotIndex == -1) {
			head = scope;
			tail = "";
		} else {
			head = scope.substring(0, dotIndex);
			tail = scope.substring(dotIndex + 1);
		}

		if (this._children.containsKey(head)) {
			return this._children.get(head).match(tail);
		}

		List<ThemeTrieElementRule> arr = new ArrayList<>();
		arr.add(this._mainRule);
		arr.addAll(this._rulesWithParentScopes);
		return ThemeTrieElement._sortBySpecificity(arr);
	}

	public void insert(int scopeDepth, String scope, List<String> parentScopes, int fontStyle, int foreground,
			int background) {
		if ("".equals(scope)) {
			this._doInsertHere(scopeDepth, parentScopes, fontStyle, foreground, background);
			return;
		}

		int dotIndex = scope.indexOf('.');
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
		if (this._children.containsKey(head)) {
			child = this._children.get(head);
		} else {
			child = new ThemeTrieElement(this._mainRule.clone(),
					ThemeTrieElementRule.cloneArr(this._rulesWithParentScopes));
			this._children.put(head, child);
		}

		child.insert(scopeDepth + 1, tail, parentScopes, fontStyle, foreground, background);
	}

	private void _doInsertHere(int scopeDepth, List<String> parentScopes, int fontStyle, int foreground,
			int background) {

		if (parentScopes == null) {
			// Merge into the main rule
			this._mainRule.acceptOverwrite(scopeDepth, fontStyle, foreground, background);
			return;
		}

		// Try to merge into existing rule
		for (int i = 0, len = this._rulesWithParentScopes.size(); i < len; i++) {
			ThemeTrieElementRule rule = this._rulesWithParentScopes.get(i);

			if (CompareUtils.strArrCmp(rule.parentScopes, parentScopes) == 0) {
				// bingo! => we get to merge this into an existing one
				rule.acceptOverwrite(scopeDepth, fontStyle, foreground, background);
				return;
			}
		}

		// Must add a new rule

		// Inherit from main rule
		if (fontStyle == FontStyle.NotSet) {
			fontStyle = this._mainRule.fontStyle;
		}
		if (foreground == 0) {
			foreground = this._mainRule.foreground;
		}
		if (background == 0) {
			background = this._mainRule.background;
		}

		this._rulesWithParentScopes
				.add(new ThemeTrieElementRule(scopeDepth, parentScopes, fontStyle, foreground, background));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_children == null) ? 0 : _children.hashCode());
		result = prime * result + ((_mainRule == null) ? 0 : _mainRule.hashCode());
		result = prime * result + ((_rulesWithParentScopes == null) ? 0 : _rulesWithParentScopes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThemeTrieElement other = (ThemeTrieElement) obj;
		if (_children == null) {
			if (other._children != null)
				return false;
		} else if (!_children.equals(other._children))
			return false;
		if (_mainRule == null) {
			if (other._mainRule != null)
				return false;
		} else if (!_mainRule.equals(other._mainRule))
			return false;
		if (_rulesWithParentScopes == null) {
			if (other._rulesWithParentScopes != null)
				return false;
		} else if (!_rulesWithParentScopes.equals(other._rulesWithParentScopes))
			return false;
		return true;
	}
	
	
}
