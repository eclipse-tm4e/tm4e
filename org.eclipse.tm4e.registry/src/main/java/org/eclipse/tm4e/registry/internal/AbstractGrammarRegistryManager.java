/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.registry.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.logger.ILogger;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.registry.EclipseSystemLogger;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;

/**
 * Eclipse grammar registry.
 *
 */
public abstract class AbstractGrammarRegistryManager extends Registry implements IGrammarRegistryManager {

	private static final ILogger GRAMMAR_LOGGER = new EclipseSystemLogger(
			"org.eclipse.tm4e.registry/debug/log/Grammar");

	protected final GrammarCache pluginCache;
	protected final GrammarCache userCache;

	private static class EclipseRegistryOptions implements IRegistryOptions {

		private AbstractGrammarRegistryManager registry;

		public void setRegistry(AbstractGrammarRegistryManager registry) {
			this.registry = registry;
		}

		@Override
		public Collection<String> getInjections(String scopeName) {
			return registry.getInjections(scopeName);
		}

		@Override
		public String getFilePath(String scopeName) {
			IGrammarDefinition info = registry.getDefinition(scopeName);
			return info != null ? info.getPath() : null;
		}

		@Override
		public InputStream getInputStream(String scopeName) throws IOException {
			IGrammarDefinition info = registry.getDefinition(scopeName);
			return info != null ? info.getInputStream() : null;
		}
	};

	public AbstractGrammarRegistryManager() {
		this(new EclipseRegistryOptions(), GRAMMAR_LOGGER);
		((EclipseRegistryOptions) getLocator()).setRegistry(this);
	}

	public AbstractGrammarRegistryManager(IRegistryOptions locator, ILogger logger) {
		super(locator, logger);
		this.pluginCache = new GrammarCache();
		this.userCache = new GrammarCache();
	}

	@Override
	public IGrammar getGrammarFor(IContentType[] contentTypes) {
		if (contentTypes == null) {
			return null;
		}
		// Find grammar by content type
		for (IContentType contentType : contentTypes) {
			String scopeName = getScopeNameForContentType(contentType.getId());
			if (scopeName != null) {
				IGrammar grammar = getGrammarForScope(scopeName);
				if (grammar != null) {
					return grammar;
				}
			}
		}
		return null;
	}

	@Override
	public IGrammar getGrammarForScope(String scopeName) {
		return getGrammar(scopeName);
	}

	@Override
	public IGrammar getGrammarForFileType(String fileType) {
		// TODO: cache grammar by file types
		IGrammarDefinition[] definitions = getDefinitions();
		// #202
		if(fileType.startsWith("."))
			fileType=fileType.substring(1);
		for (IGrammarDefinition definition : definitions) {
			// Not very optimized because it forces the load of the whole
			// grammar.
			// Extension Point grammar should perhaps stores file type bindings
			// like content type/scope binding?
			IGrammar grammar = getGrammarForScope(definition.getScopeName());
			if (grammar != null) {
				Collection<String> fileTypes = grammar.getFileTypes();
				if (fileTypes != null && fileTypes.contains(fileType)) {
					return grammar;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the whole registered grammar definition.
	 * 
	 * @return
	 */
	@Override
	public IGrammarDefinition[] getDefinitions() {
		Collection<IGrammarDefinition> pluginDefinitions = pluginCache.getDefinitions();
		Collection<IGrammarDefinition> userDefinitions = userCache.getDefinitions();
		Collection<IGrammarDefinition> definitions = new ArrayList<>(pluginDefinitions);
		definitions.addAll(userDefinitions);
		return definitions.toArray(new IGrammarDefinition[definitions.size()]);
	}

	/**
	 * Returns the loaded grammar from the given <code>scopeName</code> and null
	 * otherwise.
	 * 
	 * @param scopeName
	 * @return the loaded grammar from the given <code>scopeName</code> and null
	 *         otherwise.
	 */
	public IGrammar getGrammar(String scopeName) {
		if (scopeName == null) {
			return null;
		}
		IGrammar grammar = super.grammarForScopeName(scopeName);
		if (grammar != null) {
			return grammar;
		}
		return super.loadGrammar(scopeName);
	}

	/**
	 * Returns the grammar definition from the given <code>scopeName</code> and
	 * null otherwise.
	 * 
	 * @param scopeName
	 * @return the grammar definition from the given <code>scopeName</code> and
	 *         null otherwise.
	 */
	public IGrammarDefinition getDefinition(String scopeName) {
		IGrammarDefinition definition = userCache.getDefinition(scopeName);
		if (definition != null) {
			return definition;
		}
		return pluginCache.getDefinition(scopeName);
	}

	/**
	 * Returns list of scope names to inject for the given
	 * <code>scopeName</code> and null otheriwse.
	 * 
	 * @param scopeName
	 * @return list of scope names to inject for the given
	 *         <code>scopeName</code> and null otheriwse.
	 */
	@Override
	public Collection<String> getInjections(String scopeName) {
		return pluginCache.getInjections(scopeName);
	}

	/**
	 * Register the given <code>scopeName</code> to inject to the given scope
	 * name <code>injectTo</code>.
	 * 
	 * @param scopeName
	 * @param injectTo
	 */
	public void registerInjection(String scopeName, String injectTo) {
		pluginCache.registerInjection(scopeName, injectTo);
	}

	/**
	 * Returns scope name bound with the given content type and null otherwise.
	 * 
	 * @param contentTypeId
	 * @return scope name bound with the given content type and null otherwise.
	 */
	public String getScopeNameForContentType(String contentTypeId) {
		return pluginCache.getScopeNameForContentType(contentTypeId);
	}

	@Override
	public String[] getContentTypesForScope(String scopeName) {
		return pluginCache.getContentTypesForScope(scopeName);
	}

	public void registerContentTypeBinding(String contentTypeId, String scopeName) {
		pluginCache.registerContentTypeBinding(contentTypeId, scopeName);
	}

	@Override
	public void registerGrammarDefinition(IGrammarDefinition definition) {
		if (definition.getPluginId() == null) {
			userCache.registerGrammarDefinition(definition);
		} else {
			pluginCache.registerGrammarDefinition(definition);
		}
	}
	
	@Override
	public void unregisterGrammarDefinition(IGrammarDefinition definition) {
		if (definition.getPluginId() == null) {
			userCache.unregisterGrammarDefinition(definition);
		} else {
			pluginCache.unregisterGrammarDefinition(definition);
		}	
	}
}
