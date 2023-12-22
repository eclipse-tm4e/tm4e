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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	protected final Map<String /*scopeName*/, IGrammarDefinition> pluginDefinitions = new HashMap<>();
	protected final Map<String /*scopeName*/, IGrammarDefinition> userDefinitions = new HashMap<>();

	private final Map<String /*scopeName*/, Collection<String>> injections = new HashMap<>();
	private final Map<IContentType, String /*scopeName*/> scopeNamesByContentType = new HashMap<>();

	private final Registry registry;

	protected AbstractGrammarRegistryManager() {
		registry = new Registry(new IRegistryOptions() {
			@Override
			public @Nullable Collection<String> getInjections(final String scopeName) {
				return AbstractGrammarRegistryManager.this.getInjections(scopeName);
			}

			@Override
			public @Nullable IGrammarSource getGrammarSource(final String scopeName) {
				var userDefinition = castNullable(userDefinitions.get(scopeName));
				final var definition = userDefinition == null ? castNullable(pluginDefinitions.get(scopeName)) : userDefinition;
				if (definition == null) {
					return null;
				}
				return new IGrammarSource() {
					@Override
					public Reader getReader() throws IOException {
						return new InputStreamReader(definition.getInputStream());
					}

					@Override
					public String getFilePath() {
						return defaultIfNull(definition.getPath(), "unknown");
					}
				};
			}
		});
	}

	protected AbstractGrammarRegistryManager(final IRegistryOptions options) {
		registry = new Registry(options);
	}

	@Override
	public @Nullable IGrammar getGrammarFor(final IContentType... contentTypes) {
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

	@Override
	public @Nullable IGrammar getGrammarForScope(final String scopeName) {
		final var grammar = registry.grammarForScopeName(scopeName);
		if (grammar != null) {
			return grammar;
		}
		return registry.loadGrammar(scopeName);
	}

	@Override
	public @Nullable IGrammar getGrammarForFileExtension(String fileExtension) {

		if (fileExtension.startsWith(".")) {
			fileExtension = fileExtension.substring(1);
		}

		if (fileExtension.isBlank()) {
			return null;
		}

		/*
		 * first try to lookup grammar via contentTypes that match the file extension
		 */
		for (var binding : scopeNamesByContentType.entrySet()) {
			var contentType = binding.getKey();
			var scopeName = binding.getValue();
			for (var contentTypeFileExtension : contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)) {
				if (fileExtension.equals(contentTypeFileExtension)) {
					final var grammar = getGrammarForScope(scopeName);
					if (grammar != null)
						return grammar;
					break;
				}
			}
		}

		/*
		 * as a fallback try to lookup a matching grammar via the fileType property inside the TextMate grammar file.
		 * this can be expensive as it potentially has to eagerly load all registered grammar files
		 */
		for (var definition : userDefinitions.values()) {
			final var grammar = getGrammarForScope(definition.getScopeName());
			if (grammar != null) {
				if (grammar.getFileTypes().contains(fileExtension)) {
					return grammar;
				}
			}
		}
		for (var definition : pluginDefinitions.values()) {
			final var grammar = getGrammarForScope(definition.getScopeName());
			if (grammar != null) {
				if (grammar.getFileTypes().contains(fileExtension)) {
					return grammar;
				}
			}
		}
		return null;
	}

	@Override
	public @Nullable IGrammarDefinition[] getDefinitions() {
		return Stream.concat(
				pluginDefinitions.values().stream(),
				userDefinitions.values().stream())
				.toArray(IGrammarDefinition[]::new);
	}

	@Override
	public @Nullable Collection<String> getInjections(final String scopeName) {
		return injections.get(scopeName);
	}

	/**
	 * Register the given <code>scopeName</code> to inject to the given scope name <code>injectTo</code>.
	 */
	protected void registerInjection(final String scopeName, final String injectTo) {
		var injections = getInjections(injectTo);
		if (injections == null) {
			injections = new ArrayList<>();
			this.injections.put(injectTo, injections);
		}
		injections.add(scopeName);
	}

	/**
	 * @return scope name bound with the given content type (or its base type) and <code>null</code> otherwise.
	 */
	private @Nullable String getScopeNameForContentType(@Nullable IContentType contentType) {
		while (contentType != null) {
			final String scopeName = castNullable(scopeNamesByContentType.get(contentType));
			if (scopeName != null) {
				return scopeName;
			}
			contentType = contentType.getBaseType();
		}
		return null;
	}

	@Override
	public @Nullable List<IContentType> getContentTypesForScope(final String scopeName) {
		return scopeNamesByContentType.entrySet().stream()
				.filter(entry -> scopeName.equals(entry.getValue()))
				.map(Entry::getKey).toList();
	}

	protected void registerContentTypeToScopeBinding(final IContentType contentType, final String scopeName) {
		scopeNamesByContentType.put(contentType, scopeName);
	}

	@Override
	public void registerGrammarDefinition(final IGrammarDefinition definition) {
		if (definition.getPluginId() == null) {
			userDefinitions.put(definition.getScopeName(), definition);
		} else {
			pluginDefinitions.put(definition.getScopeName(), definition);
		}
	}

	@Override
	public void unregisterGrammarDefinition(final IGrammarDefinition definition) {
		if (definition.getPluginId() == null) {
			userDefinitions.remove(definition.getScopeName());
		} else {
			pluginDefinitions.remove(definition.getScopeName());
		}
	}
}
