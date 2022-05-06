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
package org.eclipse.tm4e.ui.internal.snippets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.snippets.ISnippet;
import org.eclipse.tm4e.ui.snippets.ISnippetManager;

public final class SnippetManager implements ISnippetManager {

	private static final ISnippet[] EMPTY_SNIPPETS = {};

	private static final String SNIPPET_ELT = "snippet";

	// "snippets" extension point
	private static final String EXTENSION_SNIPPETS = "snippets"; //$NON-NLS-1$

	@Nullable
	private static ISnippetManager INSTANCE;

	public static ISnippetManager getInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		INSTANCE = createInstance();
		return INSTANCE;
	}

	private static synchronized ISnippetManager createInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		final SnippetManager manager = new SnippetManager();
		manager.load();
		return manager;
	}

	private final Map<String, @Nullable Collection<ISnippet>> snippets = new HashMap<>();

	private SnippetManager() {
	}

	private void load() {
		loadGrammarsFromExtensionPoints();
	}

	/**
	 * Load snippets from extension point.
	 */
	private void loadGrammarsFromExtensionPoints() {
		final IConfigurationElement[] cf = Platform.getExtensionRegistry().getConfigurationElementsFor(TMUIPlugin.PLUGIN_ID,
				EXTENSION_SNIPPETS);
		for (final IConfigurationElement ce : cf) {
			final String extensionName = ce.getName();
			if (SNIPPET_ELT.equals(extensionName)) {
				this.registerSnippet(new Snippet(ce));
			}
		}
	}

	private void registerSnippet(final Snippet snippet) {
		final String scopeName = snippet.getScopeName();
		Collection<ISnippet> snippets = this.snippets.get(scopeName);
		if (snippets == null) {
			snippets = new ArrayList<>();
			this.snippets.put(scopeName, snippets);
		}
		snippets.add(snippet);
	}

	@Override
	public ISnippet[] getSnippets(final String scopeName) {
		final Collection<ISnippet> snippets = this.snippets.get(scopeName);
		return snippets != null ? snippets.toArray(ISnippet[]::new) : EMPTY_SNIPPETS;
	}
}
