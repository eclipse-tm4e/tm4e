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
package org.eclipse.tm4e.core.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.grammar.reader.GrammarReader;
import org.eclipse.tm4e.core.internal.registry.SyncRegistry;
import org.eclipse.tm4e.core.internal.theme.IRawTheme;
import org.eclipse.tm4e.core.internal.theme.Theme;

/**
 * The registry that will hold all grammars.
 *
 * TODO outdated compared to upstream as of:
 * https://github.com/microsoft/vscode-textmate/commit/b166b75fa72d2dd3efce0d68c98c2bd10adc1ef1
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/main.ts#L77">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/main.ts</a>
 *
 */
public class Registry {

	private final IRegistryOptions options;
	private final SyncRegistry syncRegistry;

	public Registry() {
		this(IRegistryOptions.DEFAULT_LOCATOR);
	}

	public Registry(final IRegistryOptions options) {
		this.options = options;
		this.syncRegistry = new SyncRegistry(Theme.createFromRawTheme(options.getTheme(), options.getColorMap()));
	}

	/**
	 * Change the theme. Once called, no previous `ruleStack` should be used anymore.
	 */
	public void setTheme(final IRawTheme theme) {
		this.syncRegistry.setTheme(Theme.createFromRawTheme(theme, options.getColorMap()));
	}

	/**
	 * Returns a lookup array for color ids.
	 */
	public List<String> getColorMap() {
		return this.syncRegistry.getColorMap();
	}

	@Nullable
	public IGrammar loadGrammar(final String initialScopeName) {
		final var remainingScopeNames = new ArrayList<String>();
		remainingScopeNames.add(initialScopeName);

		final var seenScopeNames = new ArrayList<String>();
		seenScopeNames.add(initialScopeName);

		while (!remainingScopeNames.isEmpty()) {
			final String scopeName = remainingScopeNames.remove(0);

			if (this.syncRegistry.lookup(scopeName) != null) {
				continue;
			}

			final String filePath = this.options.getFilePath(scopeName);
			if (filePath == null) {
				if (scopeName.equals(initialScopeName)) {
					throw new TMException("Unknown location for grammar <" + initialScopeName + ">");
				}
				continue;
			}

			try (InputStream in = this.options.getInputStream(scopeName)) {
				if (in == null) {
					throw new TMException("Unknown location for grammar <" + initialScopeName + ">");
				}
				final var grammar = GrammarReader.readGrammarSync(filePath, in);
				final var injections = this.options.getInjections(scopeName);

				final var deps = this.syncRegistry.addGrammar(grammar, injections);
				for (final String dep : deps) {
					if (!seenScopeNames.contains(dep)) {
						seenScopeNames.add(dep);
						remainingScopeNames.add(dep);
					}
				}
			} catch (final Exception e) {
				if (scopeName.equals(initialScopeName)) {
					throw new TMException("Unknown location for grammar <" + initialScopeName + ">", e);
				}
			}
		}
		return this.grammarForScopeName(initialScopeName);
	}

	@Nullable
	public IGrammar loadGrammarFromPathSync(final File file) throws Exception {
		try (InputStream is = new FileInputStream(file)) {
			return loadGrammarFromPathSync(file.getPath(), is);
		}
	}

	@Nullable
	public IGrammar loadGrammarFromPathSync(final String path, final InputStream in) throws Exception {
		return loadGrammarFromPathSync(path, in, 0, null);
	}

	/**
	 * Load the grammar at `path` synchronously.
	 */
	@Nullable
	public IGrammar loadGrammarFromPathSync(final String path, final InputStream in, final int initialLanguage,
			@Nullable final Map<String, Integer> embeddedLanguages) throws Exception {
		final var rawGrammar = GrammarReader.readGrammarSync(path, in);
		final var injections = this.options.getInjections(rawGrammar.getScopeName());
		this.syncRegistry.addGrammar(rawGrammar, injections);
		return this.grammarForScopeName(rawGrammar.getScopeName(), initialLanguage, embeddedLanguages);
	}

	@Nullable
	public IGrammar grammarForScopeName(final String scopeName) {
		return grammarForScopeName(scopeName, 0, null);
	}

	/**
	 * Get the grammar for `scopeName`.
	 * The grammar must first be created via `loadGrammar` or `loadGrammarFromPathSync`.
	 */
	@Nullable
	public IGrammar grammarForScopeName(final String scopeName, final int initialLanguage,
			@Nullable final Map<String, Integer> embeddedLanguages) {
		return this.syncRegistry.grammarForScopeName(scopeName, initialLanguage, embeddedLanguages, /*TODO*/null,
				/*TODO*/null);
	}

	public IRegistryOptions getLocator() {
		return options;
	}
}
