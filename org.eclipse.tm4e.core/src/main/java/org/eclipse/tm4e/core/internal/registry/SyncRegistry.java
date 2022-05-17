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
package org.eclipse.tm4e.core.internal.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.grammar.BalancedBracketSelectors;
import org.eclipse.tm4e.core.internal.grammar.Grammar;
import org.eclipse.tm4e.core.internal.theme.IThemeProvider;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.internal.theme.ThemeTrieElementRule;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/registry.ts#L11">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/registry.ts</a>
 */
public final class SyncRegistry implements IGrammarRepository, IThemeProvider {

	private final Map<String, Grammar> grammars = new HashMap<>();
	private final Map<String, IRawGrammar> rawGrammars = new HashMap<>();
	private final Map<String, Collection<String>> injectionGrammars = new HashMap<>();
	private Theme theme;

	public SyncRegistry(final Theme theme) {
		this.theme = theme;
	}

	public void setTheme(final Theme theme) {
		this.theme = theme;
		this.grammars.values().forEach(Grammar::onDidChangeTheme);
	}

	public List<String> getColorMap() {
		return this.theme.getColorMap();
	}

	/**
	 * Add `grammar` to registry and return a list of referenced scope names
	 */
	public void addGrammar(final IRawGrammar grammar,
		@Nullable final Collection<String> injectionScopeNames) {
		this.rawGrammars.put(grammar.getScopeName(), grammar);

		if (injectionScopeNames != null) {
			this.injectionGrammars.put(grammar.getScopeName(), injectionScopeNames);
		}
	}

	@Override
	@Nullable
	public IRawGrammar lookup(final String scopeName) {
		return this.rawGrammars.get(scopeName);
	}

	@Override
	@Nullable
	public Collection<String> injections(final String targetScope) {
		return this.injectionGrammars.get(targetScope);
	}

	/**
	 * Get the default theme settings
	 */
	@Override
	public ThemeTrieElementRule getDefaults() {
		return this.theme.getDefaults();
	}

	/**
	 * Match a scope in the theme.
	 */
	@Override
	public List<ThemeTrieElementRule> themeMatch(final String scopeName) {
		return this.theme.match(scopeName);
	}

	/**
	 * Lookup a grammar.
	 */
	@Nullable
	public IGrammar grammarForScopeName(final String scopeName,
		final int initialLanguage,
		@Nullable final Map<String, Integer> embeddedLanguages,
		@Nullable final Map<String, Integer> tokenTypes,
		@Nullable final BalancedBracketSelectors balancedBracketSelectors) {
		if (!this.grammars.containsKey(scopeName)) {
			final var rawGrammar = lookup(scopeName);
			if (rawGrammar == null) {
				return null;
			}
			this.grammars.put(scopeName,
				new Grammar(scopeName, rawGrammar, initialLanguage, embeddedLanguages, tokenTypes,
					balancedBracketSelectors, this, this));
		}
		return this.grammars.get(scopeName);
	}
}
