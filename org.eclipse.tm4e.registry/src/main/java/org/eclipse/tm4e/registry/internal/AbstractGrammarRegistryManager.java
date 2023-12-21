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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;

/**
 * Eclipse grammar registry.
 */
public abstract class AbstractGrammarRegistryManager implements IGrammarRegistryManager {

	private final GrammarCache pluginCache = new GrammarCache();
	protected final GrammarCache userCache = new GrammarCache();

	private static final class EclipseRegistryOptions implements IRegistryOptions {

		@Nullable
		private AbstractGrammarRegistryManager registryManager;

		private void setRegistry(final AbstractGrammarRegistryManager registryManager) {
			this.registryManager = registryManager;
		}

		@Nullable
		@Override
		public Collection<String> getInjections(final String scopeName) {
			final var registryManager = this.registryManager;
			if (registryManager == null) {
				return null;
			}
			return registryManager.getInjections(scopeName);
		}

		@Override
		public @Nullable IGrammarSource getGrammarSource(final String scopeName) {
			final IGrammarDefinition info = getDefinition(scopeName);
			if (info == null)
				return null;

			return new IGrammarSource() {
				@Override
				public Reader getReader() throws IOException {
					return new InputStreamReader(info.getInputStream());
				}

				@Override
				public String getFilePath() {
					return defaultIfNull(info.getPath(), "unknown");
				}
			};
		}

		@Nullable
		private IGrammarDefinition getDefinition(final String scopeName) {
			final var registryManager = this.registryManager;
			if (registryManager == null) {
				return null;
			}
			final var definition = registryManager.userCache.getDefinition(scopeName);
			if (definition != null) {
				return definition;
			}
			return registryManager.pluginCache.getDefinition(scopeName);
		}
	}

	private final Registry registry;

	protected AbstractGrammarRegistryManager() {
		final var options = new EclipseRegistryOptions();
		options.setRegistry(this);
		registry = new Registry(options);
	}

	protected AbstractGrammarRegistryManager(final IRegistryOptions options) {
		registry = new Registry(options);
	}

	@Nullable
	@Override
	public IGrammar getGrammarFor(final IContentType @Nullable [] contentTypes) {
		if (contentTypes == null) {
			return null;
		}
		// Find grammar by content type
		for (final var contentType : contentTypes) {
			final String scopeName = getScopeNameForContentType(contentType);
			if (scopeName != null) {
				final var grammar = getGrammarForScope(scopeName);
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
	public IGrammar getGrammarForFileExtension(String fileExtension) {
		// TODO: cache grammar by file types
		final IGrammarDefinition[] definitions = getDefinitions();
		// #202
		if (fileExtension.startsWith(".")) {
		   fileExtension = fileExtension.substring(1);
		}
		for (final var definition : definitions) {
			// Not very optimized because it forces the load of the whole grammar.
			// Extension Point grammar should perhaps stores file type bindings
			// like content type/scope binding?
			final var grammar = getGrammarForScope(definition.getScopeName());
			if (grammar != null) {
				final Collection<String> fileTypes = grammar.getFileTypes();
				if (fileTypes.contains(fileExtension)) {
					return grammar;
				}
			}
		}
		return null;
	}

	@Nullable
	@Override
	public IGrammarDefinition[] getDefinitions() {
		return Stream.concat(
				pluginCache.getDefinitions().stream(),
				userCache.getDefinitions().stream())
				.toArray(IGrammarDefinition[]::new);
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
		final var grammar = registry.grammarForScopeName(scopeName);
		if (grammar != null) {
			return grammar;
		}
		return registry.loadGrammar(scopeName);
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
