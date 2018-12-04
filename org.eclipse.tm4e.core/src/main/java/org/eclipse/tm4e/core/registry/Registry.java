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
package org.eclipse.tm4e.core.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.grammar.reader.GrammarReader;
import org.eclipse.tm4e.core.internal.grammars.SyncRegistry;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.eclipse.tm4e.core.logger.ILogger;
import org.eclipse.tm4e.core.theme.IRawTheme;
import org.eclipse.tm4e.core.theme.Theme;

/**
 * The registry that will hold all grammars.
 * 
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/main.ts
 * 
 */
public class Registry {

	private final IRegistryOptions _locator;
	private final SyncRegistry _syncRegistry;

	public Registry() {
		this(IRegistryOptions.DEFAULT_LOCATOR);
	}

	public Registry(IRegistryOptions locator) {
		this(locator, ILogger.DEFAULT_LOGGER);
	}

	public Registry(IRegistryOptions locator, ILogger logger) {
		this._locator = locator;
		this._syncRegistry = new SyncRegistry(Theme.createFromRawTheme(locator.getTheme()), logger);
	}

	/**
	 * Change the theme. Once called, no previous `ruleStack` should be used
	 * anymore.
	 */
	public void setTheme(IRawTheme theme) {
		this._syncRegistry.setTheme(Theme.createFromRawTheme(theme));
	}

	/**
	 * Returns a lookup array for color ids.
	 */
	public Set<String> getColorMap() {
		return this._syncRegistry.getColorMap();
	}

	/**
	 * Load the grammar for `scopeName` and all referenced included grammars
	 * asynchronously. Please do not use language id 0.
	 */
	public IGrammar loadGrammarWithEmbeddedLanguages(String initialScopeName, int initialLanguage,
			Map<String, Integer> embeddedLanguages) {
		return _loadGrammar(initialScopeName, initialLanguage, embeddedLanguages);
	}

	public IGrammar loadGrammar(String initialScopeName) {
		return _loadGrammar(initialScopeName, 0, null);
	}

	private IGrammar _loadGrammar(String initialScopeName, int initialLanguage,
			Map<String, Integer> embeddedLanguages) {

		List<String> remainingScopeNames = new ArrayList<>();
		remainingScopeNames.add(initialScopeName);

		List<String> seenScopeNames = new ArrayList<>();
		seenScopeNames.add(initialScopeName);

		while (!remainingScopeNames.isEmpty()) {
			String scopeName = remainingScopeNames.remove(0); // shift();

			if (this._syncRegistry.lookup(scopeName) != null) {
				continue;
			}

			String filePath = this._locator.getFilePath(scopeName);
			if (filePath == null) {
				if (scopeName.equals(initialScopeName)) {
					throw new TMException("Unknown location for grammar <" + initialScopeName + ">");
					// callback(new Error('Unknown location for grammar <' +
					// initialScopeName + '>'), null);
					// return;
				}
				continue;
			}

			try {
				InputStream in = this._locator.getInputStream(scopeName);
				IRawGrammar grammar = GrammarReader.readGrammarSync(filePath, in);
				Collection<String> injections = this._locator.getInjections(scopeName);

				Collection<String> deps = this._syncRegistry.addGrammar(grammar, injections);
				for (String dep : deps) {
					if (!seenScopeNames.contains(dep)) {
						seenScopeNames.add(dep);
						remainingScopeNames.add(dep);
					}
				}
			} catch (Throwable e) {
				if (scopeName.equals(initialScopeName)) {
					// callback(new Error('Unknown location for grammar <' +
					// initialScopeName + '>'), null);
					// return;
					throw new TMException("Unknown location for grammar <" + initialScopeName + ">");
				}
			}
		}
		return this.grammarForScopeName(initialScopeName);
	}

	public IGrammar loadGrammarFromPathSync(File file) throws Exception {
		return loadGrammarFromPathSync(file.getPath(), new FileInputStream(file));
	}

	public IGrammar loadGrammarFromPathSync(String path, InputStream in) throws Exception {
		return loadGrammarFromPathSync(path, in, 0, null);
	}

	/**
	 * Load the grammar at `path` synchronously.
	 * 
	 * @throws Exception
	 */
	public IGrammar loadGrammarFromPathSync(String path, InputStream in, int initialLanguage,
			Map<String, Integer> embeddedLanguages) throws Exception {
		IRawGrammar rawGrammar = GrammarReader.readGrammarSync(path, in);
		Collection<String> injections = this._locator.getInjections(rawGrammar.getScopeName());
		this._syncRegistry.addGrammar(rawGrammar, injections);
		return this.grammarForScopeName(rawGrammar.getScopeName(), initialLanguage, embeddedLanguages);
	}

	public IGrammar grammarForScopeName(String scopeName) {
		return grammarForScopeName(scopeName, 0, null);
	}

	/**
	 * Get the grammar for `scopeName`. The grammar must first be created via
	 * `loadGrammar` or `loadGrammarFromPathSync`.
	 */
	public IGrammar grammarForScopeName(String scopeName, int initialLanguage, Map<String, Integer> embeddedLanguages) {
		return this._syncRegistry.grammarForScopeName(scopeName, initialLanguage, embeddedLanguages);
	}

	public IRegistryOptions getLocator() {
		return _locator;
	}
}
