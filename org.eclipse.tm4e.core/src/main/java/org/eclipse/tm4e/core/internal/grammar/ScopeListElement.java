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
import org.eclipse.tm4e.core.theme.FontStyle;
import org.eclipse.tm4e.core.theme.ThemeTrieElementRule;

import com.google.common.base.Splitter;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/grammar.ts#L1209">
 *      https://github.com/Microsoft/vscode-textmate/blob/main/src/grammar.ts</a>
 *
 */
public final class ScopeListElement {

	private static final Splitter BY_SPACE_SPLITTER = Splitter.on(' ');

	@Nullable
	private final ScopeListElement parent;
	private final String scope;
	final int metadata;

	public ScopeListElement(@Nullable final ScopeListElement parent, final String scope, final int metadata) {
		this.parent = parent;
		this.scope = scope;
		this.metadata = metadata;
	}

	private static boolean structuralEquals(@Nullable ScopeListElement a, @Nullable ScopeListElement b) {
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

			if (a.scope != b.scope || a.metadata != b.metadata) {
				return false;
			}

			// Go to previous pair
			a = a.parent;
			b = b.parent;
		} while (true);
	}

	private static boolean equals(@Nullable final ScopeListElement a, @Nullable final ScopeListElement b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return structuralEquals(a, b);
	}

	@Override
	public boolean equals(@Nullable final Object other) {
		if (other == null || other.getClass() != ScopeListElement.class) {
			return false;
		}
		return equals(this, (ScopeListElement) other);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parent, scope, metadata);
	}

	private static boolean matchesScope(final String scope, final String selector, final String selectorWithDot) {
		return (selector.equals(scope) || scope.startsWith(selectorWithDot));
	}

	/**
	 * implementation differs from upstream in that it is prevents potential NPEs/IndexOutOfBoundExceptions
	 */
	private static boolean matches(@Nullable ScopeListElement target, @Nullable final List<String> parentScopes) {
		if (parentScopes == null || parentScopes.isEmpty()) {
			return true;
		}

		if (target == null) {
			return false;
		}

		parent_scopes_loop: for (final String selector : parentScopes) {
			final String selectorWithDot = selector + '.';

			while (target != null) {
				if (matchesScope(target.scope, selector, selectorWithDot)) {
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

	public static int mergeMetadata(final int metadata, @Nullable final ScopeListElement scopesList,
			@Nullable final ScopeMetadata source) {
		if (source == null) {
			return metadata;
		}

		int fontStyle = FontStyle.NotSet;
		int foreground = 0;
		int background = 0;

		if (source.themeData != null) {
			// Find the first themeData that matches
			for (final ThemeTrieElementRule themeData : source.themeData) {
				if (matches(scopesList, themeData.parentScopes)) {
					fontStyle = themeData.fontStyle;
					foreground = themeData.foreground;
					background = themeData.background;
					break;
				}
			}
		}

		return StackElementMetadata.set(metadata, source.languageId, source.tokenType, fontStyle, foreground,
				background);
	}

	private static ScopeListElement push(ScopeListElement target, final Grammar grammar,
			final Iterable<String> scopes) {
		for (final String scope : scopes) {
			final var rawMetadata = grammar.getMetadataForScope(scope);
			final int metadata = ScopeListElement.mergeMetadata(target.metadata, target, rawMetadata);
			target = new ScopeListElement(target, scope, metadata);
		}
		return target;
	}

	ScopeListElement push(final Grammar grammar, @Nullable final String scope) {
		if (scope == null) {
			return this;
		}

		return ScopeListElement.push(this, grammar, BY_SPACE_SPLITTER.split(scope));
	}

	private static List<String> generateScopes(@Nullable ScopeListElement scopesList) {
		final var result = new ArrayList<String>();
		while (scopesList != null) {
			result.add(scopesList.scope);
			scopesList = scopesList.parent;
		}
		Collections.reverse(result);
		return result;
	}

	List<String> generateScopes() {
		return ScopeListElement.generateScopes(this);
	}
}
