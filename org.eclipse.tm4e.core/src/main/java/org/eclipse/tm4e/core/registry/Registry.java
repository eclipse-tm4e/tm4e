/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.registry;

import static java.lang.System.Logger.Level.WARNING;
import static org.eclipse.tm4e.core.internal.utils.MoreCollections.nullToEmpty;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.lang.System.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.grammar.BalancedBracketSelectors;
import org.eclipse.tm4e.core.internal.grammar.dependencies.ScopeDependencyProcessor;
import org.eclipse.tm4e.core.internal.grammar.raw.RawGrammar;
import org.eclipse.tm4e.core.internal.grammar.raw.RawGrammarReader;
import org.eclipse.tm4e.core.internal.registry.SyncRegistry;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.internal.utils.ScopeNames;

/**
 * The registry that will hold all grammars.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/main.ts#L54">
 *      github.com/microsoft/vscode-textmate/blob/main/src/main.ts</a>
 */
public class Registry {

	private static final Logger LOGGER = System.getLogger(Registry.class.getName());

	private final IRegistryOptions _options;
	private final SyncRegistry _syncRegistry;
	private final Map<String, Boolean> _ensureGrammarCache = new HashMap<>();

	public Registry() {
		this(new IRegistryOptions() {
		});
	}

	public Registry(final IRegistryOptions options) {
		this._options = options;
		this._syncRegistry = new SyncRegistry(Theme.createFromRawTheme(options.getTheme(), options.getColorMap()));
	}

	/**
	 * Change the theme. Once called, no previous `ruleStack` should be used anymore.
	 */
	public void setTheme(final IThemeSource source) throws TMException {
		try {
			this._syncRegistry.setTheme(Theme.createFromRawTheme(RawThemeReader.readTheme(source), _options.getColorMap()));
		} catch (final Exception ex) {
			throw new TMException("Loading theme from '" + source.getURI() + "' failed: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Returns a lookup array for color ids.
	 */
	public List<String> getColorMap() {
		return this._syncRegistry.getColorMap();
	}

	/**
	 * Load the grammar for `scopeName` and all referenced included grammars asynchronously.
	 * Please do not use language id 0.
	 */
	public @Nullable IGrammar loadGrammarWithEmbeddedLanguages(
			final String initialScopeName,
			final int initialLanguage,
			final Map<String, Integer> embeddedLanguages) {
		return this.loadGrammarWithConfiguration(initialScopeName, initialLanguage,
				new IGrammarConfiguration() {
					@Override
					public @Nullable Map<String, Integer> getEmbeddedLanguages() {
						return embeddedLanguages;
					}
				});
	}

	/**
	 * Load the grammar for `scopeName` and all referenced included grammars asynchronously.
	 * Please do not use language id 0.
	 */
	public @Nullable IGrammar loadGrammarWithConfiguration(
			final String initialScopeName,
			final int initialLanguage,
			final IGrammarConfiguration configuration) {
		return this._loadGrammar(
				initialScopeName,
				initialLanguage,
				configuration.getEmbeddedLanguages(),
				configuration.getTokenTypes(),
				new BalancedBracketSelectors(
						nullToEmpty(configuration.getBalancedBracketSelectors()),
						nullToEmpty(configuration.getUnbalancedBracketSelectors())));
	}

	/**
	 * Load the grammar for `scopeName` and all referenced included grammars.
	 */
	public @Nullable IGrammar loadGrammar(final String initialScopeName) {
		return this._loadGrammar(initialScopeName, 0, null, null, null);
	}

	private @Nullable IGrammar _loadGrammar(
			final String initialScopeName,
			final int initialLanguage,
			final @Nullable Map<String, Integer> embeddedLanguages,
			final @Nullable Map<String, Integer> tokenTypes,
			final @Nullable BalancedBracketSelectors balancedBracketSelectors) {

		if (!_loadSingleGrammar(initialScopeName))
			return null;

		final var dependencyProcessor = new ScopeDependencyProcessor(this._syncRegistry, initialScopeName);
		while (!dependencyProcessor.Q.isEmpty()) {
			dependencyProcessor.Q.forEach(request -> this._loadSingleGrammar(request.scopeName));
			dependencyProcessor.processQueue();
		}

		return this._grammarForScopeName(
				initialScopeName,
				initialLanguage,
				embeddedLanguages,
				tokenTypes,
				balancedBracketSelectors);
	}

	protected boolean _loadSingleGrammar(final String scopeName) {
		return this._ensureGrammarCache.computeIfAbsent(scopeName, this::_doLoadSingleGrammar);
	}

	protected boolean _doLoadSingleGrammar(final String scopeName) {
		final var grammarSource = _grammarSourceForScopeName(scopeName);
		if (grammarSource == null) {
			LOGGER.log(WARNING, "No grammar source for scope [{0}]", scopeName);
			return false;
		}
		try {
			final var grammar = RawGrammarReader.readGrammar(grammarSource);

			// this code is specific to the tm4e project and not from upstream:
			// adjust the scopeName in case the name as defined inside the TextMate grammar file
			// diverges from the scopeName with which it is registered in the plugin.xml.
			grammar.put(RawGrammar.SCOPE_NAME, scopeName);

			this._syncRegistry.addGrammar(grammar, this._options.getInjections(scopeName));
		} catch (final Exception ex) {
			throw new TMException("Loading grammar for scope [" + scopeName + "] from [" +
					grammarSource.getURI() + "] failed: " + ex.getMessage(), ex);
		}
		return true;
	}

	public IGrammar addGrammar(final IGrammarSource source) throws TMException {
		return addGrammar(source, null, null, null);
	}

	public IGrammar addGrammar(
			final IGrammarSource source,
			final @Nullable List<String> injections,
			final @Nullable Integer initialLanguage,
			final @Nullable Map<String, Integer> embeddedLanguages) throws TMException {
		try {
			final var rawGrammar = RawGrammarReader.readGrammar(source);
			this._syncRegistry.addGrammar(rawGrammar,
					injections == null || injections.isEmpty()
							? this._options.getInjections(rawGrammar.getScopeName())
							: injections);
			return castNonNull(this._grammarForScopeName(rawGrammar.getScopeName(), initialLanguage, embeddedLanguages, null, null));
		} catch (final Exception ex) {
			throw new TMException("Loading grammar from [" + source.getURI() + "] failed: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Lookup a grammar. The grammar must first be registered via `loadGrammar` or `addGrammar`.
	 */
	public @Nullable IGrammar grammarForScopeName(final String scopeName) {
		return _grammarForScopeName(scopeName, null, null, null, null);
	}

	/**
	 * Get the grammar for `scopeName`. The grammar must first be created via `loadGrammar` or `addGrammar`.
	 */
	private @Nullable IGrammar _grammarForScopeName(
			final String scopeName,
			final @Nullable Integer initialLanguage,
			final @Nullable Map<String, Integer> embeddedLanguages,
			final @Nullable Map<String, Integer> tokenTypes,
			final @Nullable BalancedBracketSelectors balancedBracketSelectors) {
		return this._syncRegistry.grammarForScopeName(
				scopeName,
				initialLanguage == null ? 0 : initialLanguage,
				embeddedLanguages,
				tokenTypes,
				balancedBracketSelectors);
	}

	/**
	 * custom tm4e code, not from upstream
	 */
	protected @Nullable IGrammarSource _grammarSourceForScopeName(final String scopeName) {
		var grammarSource = this._options.getGrammarSource(scopeName);
		if (grammarSource == null) {
			final var scopeNameWithoutContributor = ScopeNames.withoutContributor(scopeName);
			if (!scopeNameWithoutContributor.equals(scopeName))
				grammarSource = this._options.getGrammarSource(scopeNameWithoutContributor);
		}
		return grammarSource;
	}
}
