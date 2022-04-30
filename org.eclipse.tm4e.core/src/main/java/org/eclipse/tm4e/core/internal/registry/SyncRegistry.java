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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.grammar.Grammar;
import org.eclipse.tm4e.core.internal.grammar.Raw;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;
import org.eclipse.tm4e.core.theme.IThemeProvider;
import org.eclipse.tm4e.core.theme.Theme;
import org.eclipse.tm4e.core.theme.ThemeTrieElementRule;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/registry.ts#L11">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/registry.ts</a>
 */
public final class SyncRegistry implements IGrammarRepository, IThemeProvider {

	private final Map<String, Grammar> grammars = new HashMap<>();
	private final Map<@Nullable String, IRawGrammar> rawGrammars = new HashMap<>();
	private final Map<@Nullable String, Collection<String>> injectionGrammars = new HashMap<>();
	private Theme theme;

	public SyncRegistry(final Theme theme) {
		this.theme = theme;
	}

	public void setTheme(final Theme theme) {
		this.theme = theme;
		this.grammars.values().forEach(Grammar::onDidChangeTheme);
	}

	public Set<String> getColorMap() {
		return this.theme.getColorMap();
	}

	/**
	 * Add `grammar` to registry and return a list of referenced scope names
	 *
	 * TODO implementation differs from upstream
	 */
	public Collection<String> addGrammar(final IRawGrammar grammar,
			@Nullable final Collection<String> injectionScopeNames) {
		this.rawGrammars.put(grammar.getScopeName(), grammar);
		final Collection<String> includedScopes = new ArrayList<>();
		collectIncludedScopes(includedScopes, grammar);

		if (injectionScopeNames != null) {
			this.injectionGrammars.put(grammar.getScopeName(), injectionScopeNames);
			injectionScopeNames.forEach(scopeName -> addIncludedScope(scopeName, includedScopes));
		}
		return includedScopes;
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
	public IGrammar grammarForScopeName(final String scopeName, final int initialLanguage,
			@Nullable final Map<String, Integer> embeddedLanguages) {
		if (!this.grammars.containsKey(scopeName)) {
			final var rawGrammar = lookup(scopeName);
			if (rawGrammar == null) {
				return null;
			}
			this.grammars.put(scopeName,
					new Grammar(scopeName, rawGrammar, initialLanguage, embeddedLanguages, this, this));
		}
		return this.grammars.get(scopeName);
	}

	private static void collectIncludedScopes(final Collection<String> result, final IRawGrammar grammar) {
		final var grammarPattners = grammar.getPatterns();
		if (grammarPattners != null) {
			extractIncludedScopesInPatterns(result, grammarPattners);
		}

		final IRawRepository repository = grammar.getRepository();
		if (repository != null) {
			extractIncludedScopesInRepository(result, repository);
		}

		// remove references to own scope (avoid recursion)
		result.remove(grammar.getScopeName());
	}

	/**
	 * Fill in `result` all external included scopes in `patterns`
	 */
	private static void extractIncludedScopesInPatterns(final Collection<String> result,
			final Collection<IRawRule> patterns) {
		for (final IRawRule pattern : patterns) {
			final Collection<IRawRule> p = pattern.getPatterns();
			if (p != null) {
				extractIncludedScopesInPatterns(result, p);
			}

			final String include = pattern.getInclude();
			if (include == null) {
				continue;
			}

			if (include.equals(Raw.DOLLAR_BASE) || include.equals(Raw.DOLLAR_SELF)) {
				// Special includes that can be resolved locally in this grammar
				continue;
			}

			if (include.charAt(0) == '#') {
				// Local include from this grammar
				continue;
			}

			final int sharpIndex = include.indexOf('#');
			if (sharpIndex >= 0) {
				addIncludedScope(include.substring(0, sharpIndex), result);
			} else {
				addIncludedScope(include, result);
			}
		}
	}

	private static void addIncludedScope(final String scopeName, final Collection<String> includedScopes) {
		if (!includedScopes.contains(scopeName)) {
			includedScopes.add(scopeName);
		}
	}

	/**
	 * Fill in `result` all external included scopes in `repository`
	 */
	private static void extractIncludedScopesInRepository(final Collection<String> result,
			final IRawRepository repository) {
		if (!(repository instanceof Raw)) {
			return;
		}
		final Raw rawRepository = (Raw) repository;
		for (final var entry : rawRepository.values()) {
			final IRawRule rule = (IRawRule) castNonNull(entry);
			final var patterns = rule.getPatterns();
			if (patterns != null) {
				extractIncludedScopesInPatterns(result, patterns);
			}
			final var repo = rule.getRepository();
			if (repo != null) {
				extractIncludedScopesInRepository(result, repo);
			}
		}
	}
}
