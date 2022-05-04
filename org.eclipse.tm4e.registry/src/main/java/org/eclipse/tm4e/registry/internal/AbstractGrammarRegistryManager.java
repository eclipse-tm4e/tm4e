/**
 * Copyright (c) 2015-2019 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Pierre-Yves B. - Issue #221 NullPointerException when retrieving fileTypes
 */
package org.eclipse.tm4e.registry.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;

/**
 * Eclipse grammar registry.
 *
 */
public abstract class AbstractGrammarRegistryManager extends Registry implements IGrammarRegistryManager {

	private final GrammarCache pluginCache;
	final GrammarCache userCache;

	private static final class EclipseRegistryOptions implements IRegistryOptions {

		@Nullable
		private AbstractGrammarRegistryManager registry;

		private void setRegistry(final AbstractGrammarRegistryManager registry) {
			this.registry = registry;
		}

		@Nullable
		@Override
		public Collection<String> getInjections(final String scopeName) {
			final var registry = this.registry;
			if (registry == null) {
				return null;
			}
			return registry.getInjections(scopeName);
		}

		@Nullable
		@Override
		public String getFilePath(final String scopeName) {
			final IGrammarDefinition info = getDefinition(scopeName);
			return info != null ? info.getPath() : null;
		}

		@Nullable
		@Override
		public InputStream getInputStream(final String scopeName) throws IOException {
			final IGrammarDefinition info = getDefinition(scopeName);
			return info != null ? info.getInputStream() : null;
		}

		@Nullable
		private IGrammarDefinition getDefinition(final String scopeName) {
			final var registry = this.registry;
			if (registry == null) {
				return null;
			}
			final IGrammarDefinition definition = registry.userCache.getDefinition(scopeName);
			if (definition != null) {
				return definition;
			}
			return registry.pluginCache.getDefinition(scopeName);
		}
	}

	protected AbstractGrammarRegistryManager() {
		this(new EclipseRegistryOptions());
		((EclipseRegistryOptions) getLocator()).setRegistry(this);
	}

	protected AbstractGrammarRegistryManager(final IRegistryOptions locator) {
		super(locator);
		this.pluginCache = new GrammarCache();
		this.userCache = new GrammarCache();
	}

	@Nullable
	@Override
	public IGrammar getGrammarFor(final IContentType @Nullable [] contentTypes) {
		if (contentTypes == null) {
			return null;
		}
		// Find grammar by content type
		for (final IContentType contentType : contentTypes) {
			final String scopeName = getScopeNameForContentType(contentType);
			if (scopeName != null) {
				final IGrammar grammar = getGrammarForScope(scopeName);
				if (grammar != null) {
					return grammar;
				}
			}
		}
		return null;
	}

	@Nullable
	@Override
	public IGrammar getGrammarForScope(final String scopeName) {
		return getGrammar(scopeName);
	}

	@Nullable
	@Override
	public IGrammar getGrammarForFileType(String fileType) {
		// TODO: cache grammar by file types
		final IGrammarDefinition[] definitions = getDefinitions();
		// #202
		if (fileType.startsWith(".")) {
			fileType = fileType.substring(1);
		}
		for (final IGrammarDefinition definition : definitions) {
			// Not very optimized because it forces the load of the whole
			// grammar.
			// Extension Point grammar should perhaps stores file type bindings
			// like content type/scope binding?
			final IGrammar grammar = getGrammarForScope(definition.getScopeName());
			if (grammar != null) {
				final Collection<String> fileTypes = grammar.getFileTypes();
				if (fileTypes.contains(fileType)) {
					return grammar;
				}
			}
		}
		return null;
	}

	@Nullable
	@Override
	public IGrammarDefinition[] getDefinitions() {
		final Collection<IGrammarDefinition> pluginDefinitions = pluginCache.getDefinitions();
		final Collection<IGrammarDefinition> userDefinitions = userCache.getDefinitions();
		final Collection<IGrammarDefinition> definitions = new ArrayList<>(pluginDefinitions);
		definitions.addAll(userDefinitions);
		return definitions.toArray(IGrammarDefinition[]::new);
	}

	/**
	 * Returns the loaded grammar from the given <code>scopeName</code> and null otherwise.
	 *
	 * @return the loaded grammar from the given <code>scopeName</code> and null otherwise.
	 */
	@Nullable
	private IGrammar getGrammar(@Nullable final String scopeName) {
		if (scopeName == null) {
			return null;
		}
		final IGrammar grammar = super.grammarForScopeName(scopeName);
		if (grammar != null) {
			return grammar;
		}
		return super.loadGrammar(scopeName);
	}

	@Nullable
	@Override
	public Collection<String> getInjections(final String scopeName) {
		return pluginCache.getInjections(scopeName);
	}

	/**
	 * Register the given <code>scopeName</code> to inject to the given scope name <code>injectTo</code>.
	 */
	protected void registerInjection(final String scopeName, final String injectTo) {
		pluginCache.registerInjection(scopeName, injectTo);
	}

	/**
	 * @return scope name bound with the given content type (or its base type) and <code>null</code> otherwise.
	 */
	@Nullable
	private String getScopeNameForContentType(@Nullable IContentType contentType) {
		while (contentType != null) {
			final String scopeName = pluginCache.getScopeNameForContentType(contentType);
			if (scopeName != null) {
				return scopeName;
			}
			contentType = contentType.getBaseType();
		}
		return null;
	}

	@Nullable
	@Override
	public List<IContentType> getContentTypesForScope(final String scopeName) {
		return pluginCache.getContentTypesForScope(scopeName);
	}

	protected void registerContentTypeBinding(final IContentType contentType, final String scopeName) {
		pluginCache.registerContentTypeBinding(contentType, scopeName);
	}

	@Override
	public void registerGrammarDefinition(final IGrammarDefinition definition) {
		if (definition.getPluginId() == null) {
			userCache.registerGrammarDefinition(definition);
		} else {
			pluginCache.registerGrammarDefinition(definition);
		}
	}

	@Override
	public void unregisterGrammarDefinition(final IGrammarDefinition definition) {
		if (definition.getPluginId() == null) {
			userCache.unregisterGrammarDefinition(definition);
		} else {
			pluginCache.unregisterGrammarDefinition(definition);
		}
	}
}
