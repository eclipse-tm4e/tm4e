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
package org.eclipse.tm4e.core.internal.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.grammar.tokenattrs.EncodedTokenAttributes;
import org.eclipse.tm4e.core.internal.theme.FontStyle;
import org.eclipse.tm4e.core.internal.theme.ThemeTrieElementRule;

import com.google.common.base.Splitter;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/grammar.ts#L1209">
 *      https://github.com/Microsoft/vscode-textmate/blob/main/src/grammar.ts</a>
 */
public final class AttributedScopeStack {

	private static final Splitter BY_SPACE_SPLITTER = Splitter.on(' ');

	@Nullable
	private final AttributedScopeStack parent;
	private final String scopePath;
	final int tokenAttributes;

	public AttributedScopeStack(
		@Nullable final AttributedScopeStack parent,
		final String scopePath,
		final int tokenAttributes) {
		this.parent = parent;
		this.scopePath = scopePath;
		this.tokenAttributes = tokenAttributes;
	}

	public boolean equals(final AttributedScopeStack other) {
		return _equals(this, other);
	}

	private static boolean _equals(
		@Nullable AttributedScopeStack a,
		@Nullable AttributedScopeStack b) {
		do {
			if (a == b) {
				return true;
			}

			if (a == null && b == null) {
				// End of list reached for both
				return true;
			}

			if (a == null || b == null) {
				// End of list reached only for one
				return false;
			}

			if (!Objects.equals(a.scopePath, b.scopePath) || a.tokenAttributes != b.tokenAttributes) {
				return false;
			}

			// Go to previous pair
			a = a.parent;
			b = b.parent;
		} while (true);
	}

	private static boolean matchesScope(final String scope, final String selector, final String selectorWithDot) {
		return selector.equals(scope) || scope.startsWith(selectorWithDot);
	}

	/**
	 * implementation differs from upstream in that it is prevents potential NPEs/IndexOutOfBoundExceptions
	 */
	private static boolean matches(@Nullable AttributedScopeStack target, @Nullable final List<String> parentScopes) {
		if (parentScopes == null || parentScopes.isEmpty()) {
			return true;
		}

		if (target == null) {
			return false;
		}

		parent_scopes_loop: for (final String selector : parentScopes) {
			final var selectorWithDot = selector + '.';

			while (target != null) {
				if (matchesScope(target.scopePath, selector, selectorWithDot)) {
					// match for current parent scope found, continue with checking next parent scope
					continue parent_scopes_loop;
				}
				target = target.parent;
			}
			// no match for current parent scope found, early exit
			return false;
		}

		// matches for all parent scopes found
		return true;
	}

	public static int mergeAttributes(
		final int existingTokenAttributes,
		@Nullable final AttributedScopeStack scopesList,
		@Nullable final BasicScopeAttributes basicScopeAttributes) {
		if (basicScopeAttributes == null) {
			return existingTokenAttributes;
		}

		int fontStyle = FontStyle.NotSet;
		int foreground = 0;
		int background = 0;

		if (basicScopeAttributes.themeData != null) {
			// Find the first themeData that matches
			for (final ThemeTrieElementRule themeData : basicScopeAttributes.themeData) {
				if (matches(scopesList, themeData.parentScopes)) {
					fontStyle = themeData.fontStyle;
					foreground = themeData.foreground;
					background = themeData.background;
					break;
				}
			}
		}

		return EncodedTokenAttributes.set(existingTokenAttributes, basicScopeAttributes.languageId,
			basicScopeAttributes.tokenType, null, fontStyle,
			foreground,
			background);
	}

	private static AttributedScopeStack push(AttributedScopeStack target, final Grammar grammar,
		final Iterable<String> scopes) {
		for (final String scope : scopes) {
			final var rawMetadata = grammar.getMetadataForScope(scope);
			final int metadata = mergeAttributes(target.tokenAttributes, target, rawMetadata);
			target = new AttributedScopeStack(target, scope, metadata);
		}
		return target;
	}

	AttributedScopeStack pushAttributed(@Nullable final String scopePath, final Grammar grammar) {
		if (scopePath == null) {
			return this;
		}

		return AttributedScopeStack.push(this, grammar, BY_SPACE_SPLITTER.split(scopePath));
	}

	private static List<String> generateScopes(@Nullable AttributedScopeStack scopesList) {
		final var result = new ArrayList<String>();
		while (scopesList != null) {
			result.add(scopesList.scopePath);
			scopesList = scopesList.parent;
		}
		Collections.reverse(result);
		return result;
	}

	List<String> getScopeNames() {
		return AttributedScopeStack.generateScopes(this);
	}
}
