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
package org.eclipse.tm4e.core.internal.theme;

import static org.eclipse.tm4e.core.internal.utils.StringUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Based on <a href="https://github.com/microsoft/vscode-textmate/blob/167bbbd509356cc4617f250c0d754aef670ab14a/src/theme.ts#L481">
 * github.com/microsoft/vscode-textmate/blob/main/src/theme.ts#ThemeTrieElement</a>.
 * <p>
 * See also <a href=
 * "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/supports/tokenization.ts#L338">
 * github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/tokenization.ts#ThemeTrieElement</a>
 */
public final class ThemeTrieElement {

	private final ThemeTrieElementRule _mainRule;
	private final List<ThemeTrieElementRule> _rulesWithParentScopes;
	private final Map<String /*segment*/, ThemeTrieElement> _children;

	public ThemeTrieElement(final ThemeTrieElementRule mainRule) {
		this(mainRule, new ArrayList<>(), new HashMap<>());
	}

	public ThemeTrieElement(
			final ThemeTrieElementRule mainRule,
			final List<ThemeTrieElementRule> rulesWithParentScopes) {

		this(mainRule, rulesWithParentScopes, new HashMap<>());
	}

	public ThemeTrieElement(
			final ThemeTrieElementRule mainRule,
			final List<ThemeTrieElementRule> rulesWithParentScopes,
			final Map<String /*segment*/, ThemeTrieElement> children) {

		this._mainRule = mainRule;
		this._rulesWithParentScopes = rulesWithParentScopes;
		this._children = children;
	}

	private static int _cmpBySpecificity(final ThemeTrieElementRule a, final ThemeTrieElementRule b) {
		// First, compare the scope depths of both rules. The “scope depth” of a rule is
		// the number of segments (delimited by dots) in the rule's deepest scope name
		// (i.e. the final scope name in the scope path delimited by spaces).
		if (a.scopeDepth != b.scopeDepth) {
			return b.scopeDepth - a.scopeDepth;
		}

		// Traverse the parent scopes depth-first, comparing the specificity of both
		// rules' parent scopes, which matches the behavior described by ”Ranking Matches”
		// in TextMate 1.5's manual: https://macromates.com/manual/en/scope_selectors
		// Start at index 0 for both rules, since the parent scopes were reversed
		// beforehand (i.e. index 0 is the deepest parent scope).
		int aParentIndex = 0;
		int bParentIndex = 0;

		final int aParentScopesSize = a.parentScopes.size();
		final int bParentScopesSize = b.parentScopes.size();

		while (true) {
			// Child combinators don't affect specificity.
			if (aParentScopesSize > aParentIndex && ">".equals(a.parentScopes.get(aParentIndex))) {
				aParentIndex++;
			}
			if (bParentScopesSize > bParentIndex && ">".equals(b.parentScopes.get(bParentIndex))) {
				bParentIndex++;
			}

			// This is a scope-by-scope comparison, so we need to stop once a rule runs
			// out of parent scopes.
			if (aParentIndex >= aParentScopesSize || bParentIndex >= bParentScopesSize) {
				break;
			}

			// When sorting by scope name specificity, it's safe to treat a longer parent
			// scope as more specific. If both rules' parent scopes match a given scope
			// path, the longer parent scope will always be more specific.
			final int parentScopeLengthDiff = b.parentScopes.get(bParentIndex).length() - a.parentScopes.get(aParentIndex).length();

			if (parentScopeLengthDiff != 0) {
				return parentScopeLengthDiff;
			}

			aParentIndex++;
			bParentIndex++;
		}

		// If a depth-first, scope-by-scope comparison resulted in a tie, the rule with
		// more parent scopes is considered more specific.
		return bParentScopesSize - aParentScopesSize;
	}

	public List<ThemeTrieElementRule> match(final String scope) {
		if (!scope.isEmpty()) {
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

			final ThemeTrieElement child = this._children.get(head);
			if (child != null) {
				return child.match(tail);
			}
		}

		final var rules = new ArrayList<>(this._rulesWithParentScopes);
		rules.add(this._mainRule);
		rules.sort(ThemeTrieElement::_cmpBySpecificity);
		return rules;
	}

	public void insert(final int scopeDepth, final String scope, final @Nullable List<String> parentScopes, final int fontStyle,
			final int foreground, final int background) {
		if (scope.isEmpty()) {
			this.doInsertHere(scopeDepth, parentScopes, fontStyle, foreground, background);
			return;
		}

		final int dotIndex = scope.indexOf('.');
		final String head;
		final String tail;
		if (dotIndex == -1) {
			head = scope;
			tail = "";
		} else {
			head = scope.substring(0, dotIndex);
			tail = scope.substring(dotIndex + 1);
		}

		final ThemeTrieElement child = this._children.computeIfAbsent(head,
				key -> new ThemeTrieElement(this._mainRule.clone(), ThemeTrieElementRule.cloneArr(this._rulesWithParentScopes)));

		child.insert(scopeDepth + 1, tail, parentScopes, fontStyle, foreground, background);
	}

	private void doInsertHere(final int scopeDepth, final @Nullable List<String> parentScopes, int fontStyle, int foreground,
			int background) {

		if (parentScopes == null) {
			// Merge into the main rule
			this._mainRule.acceptOverwrite(scopeDepth, fontStyle, foreground, background);
			return;
		}

		// Try to merge into existing rule
		for (final ThemeTrieElementRule rule : this._rulesWithParentScopes) {
			if (strArrCmp(rule.parentScopes, parentScopes) == 0) {
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

		this._rulesWithParentScopes.add(new ThemeTrieElementRule(scopeDepth, parentScopes, fontStyle, foreground, background));
	}

	@Override
	public int hashCode() {
		int result = 31 + _children.hashCode();
		result = 31 * result + _mainRule.hashCode();
		return 31 * result + _rulesWithParentScopes.hashCode();
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof final ThemeTrieElement other)
			return _children.equals(other._children)
					&& _mainRule.equals(other._mainRule)
					&& _rulesWithParentScopes.equals(other._rulesWithParentScopes);
		return false;
	}
}
