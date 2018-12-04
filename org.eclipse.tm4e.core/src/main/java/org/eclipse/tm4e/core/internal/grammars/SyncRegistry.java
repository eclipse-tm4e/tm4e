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
package org.eclipse.tm4e.core.internal.grammars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.tm4e.core.grammar.GrammarHelper;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IGrammarRepository;
import org.eclipse.tm4e.core.internal.grammar.Grammar;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;
import org.eclipse.tm4e.core.logger.ILogger;
import org.eclipse.tm4e.core.theme.IThemeProvider;
import org.eclipse.tm4e.core.theme.Theme;
import org.eclipse.tm4e.core.theme.ThemeTrieElementRule;

public class SyncRegistry implements IGrammarRepository, IThemeProvider {

	private final ILogger logger;
	private final Map<String, IGrammar> _grammars;
	private final Map<String, IRawGrammar> _rawGrammars;
	private final Map<String, Collection<String>> _injectionGrammars;
	private Theme _theme;

	public SyncRegistry(Theme theme, ILogger logger) {
		this._theme = theme;
		this.logger = logger;
		this._grammars = new HashMap<>();
		this._rawGrammars = new HashMap<>();
		this._injectionGrammars = new HashMap<>();
	}

	public void setTheme(Theme theme) {
		this._theme = theme;
		this._grammars.keySet().forEach((scopeName) -> {
			IGrammar grammar = this._grammars.get(scopeName);
			((Grammar) grammar).onDidChangeTheme();
		});
	}

	public Set<String> getColorMap() {
		return this._theme.getColorMap();
	}

	/**
	 * Add `grammar` to registry and return a list of referenced scope names
	 */
	public Collection<String> addGrammar(IRawGrammar grammar, Collection<String> injectionScopeNames) {
		this._rawGrammars.put(grammar.getScopeName(), grammar);
		Collection<String> includedScopes = new ArrayList<>();
		collectIncludedScopes(includedScopes, grammar);

		if (injectionScopeNames != null) {
			this._injectionGrammars.put(grammar.getScopeName(), injectionScopeNames);
			injectionScopeNames.forEach(scopeName -> {
				addIncludedScope(scopeName, includedScopes);
			});
		}
		return includedScopes;
	}

	@Override
	public IRawGrammar lookup(String scopeName) {
		return this._rawGrammars.get(scopeName);
	}

	@Override
	public Collection<String> injections(String targetScope) {
		return this._injectionGrammars.get(targetScope);
	}

	/**
	 * Get the default theme settings
	 */
	@Override
	public ThemeTrieElementRule getDefaults() {
		return this._theme.getDefaults();
	}

	/**
	 * Match a scope in the theme.
	 */
	@Override
	public List<ThemeTrieElementRule> themeMatch(String scopeName) {
		return this._theme.match(scopeName);
	}

	/**
	 * Lookup a grammar.
	 */
	public IGrammar grammarForScopeName(String scopeName, int initialLanguage,
			Map<String, Integer> embeddedLanguages) {
		if (!this._grammars.containsKey(scopeName)) {
			IRawGrammar rawGrammar = lookup(scopeName);
			if (rawGrammar == null) {
				return null;
			}
			this._grammars.put(scopeName,
					GrammarHelper.createGrammar(rawGrammar, initialLanguage, embeddedLanguages, this, this));
		}
		return this._grammars.get(scopeName);
	}

	@Override
	public ILogger getLogger() {
		return logger;
	}

	private static void collectIncludedScopes(Collection<String> result, IRawGrammar grammar) {
		if (grammar
				.getPatterns() != null /* && Array.isArray(grammar.patterns) */) {
			_extractIncludedScopesInPatterns(result, grammar.getPatterns());
		}

		IRawRepository repository = grammar.getRepository();
		if (repository != null) {
			_extractIncludedScopesInRepository(result, repository);
		}

		// remove references to own scope (avoid recursion)
		result.remove(grammar.getScopeName());
	}

	/**
	 * Fill in `result` all external included scopes in `patterns`
	 */
	private static void _extractIncludedScopesInPatterns(Collection<String> result, Collection<IRawRule> patterns) {
		for (IRawRule pattern : patterns) {
			Collection<IRawRule> p = pattern.getPatterns();
			if (p != null) {
				_extractIncludedScopesInPatterns(result, p);
			}

			String include = pattern.getInclude();
			if (include == null) {
				continue;
			}

			if (include.equals("$base") || include.equals("$self")) {
				// Special includes that can be resolved locally in this grammar
				continue;
			}

			if (include.charAt(0) == '#') {
				// Local include from this grammar
				continue;
			}

			int sharpIndex = include.indexOf('#');
			if (sharpIndex >= 0) {
				addIncludedScope(include.substring(0, sharpIndex), result);
			} else {
				addIncludedScope(include, result);
			}
		}
	}

	private static void addIncludedScope(String scopeName, Collection<String> includedScopes) {
		if (!includedScopes.contains(scopeName)) {
			includedScopes.add(scopeName);
		}
	}

	/**
	 * Fill in `result` all external included scopes in `repository`
	 */
	private static void _extractIncludedScopesInRepository(Collection<String> result, IRawRepository repository) {
		Map<String, Object> r = (Map<String, Object>) repository;
		for (Entry<String, Object> entry : r.entrySet()) {
			IRawRule rule = (IRawRule) entry.getValue();
			if (rule.getPatterns() != null) {
				_extractIncludedScopesInPatterns(result, rule.getPatterns());
			}
			if (rule.getRepository() != null) {
				_extractIncludedScopesInRepository(result, rule.getRepository());
			}
		}
	}

}
