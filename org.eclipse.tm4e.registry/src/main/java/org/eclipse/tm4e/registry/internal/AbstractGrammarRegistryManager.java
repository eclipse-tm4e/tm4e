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
 * Sebastian Thomschke - major refactoring, performance improvements, handle conflicting grammar registrations
 */
package org.eclipse.tm4e.registry.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.ITMScope;

abstract class AbstractGrammarRegistryManager implements IGrammarRegistryManager {

	private static record ContentTypeToScopeBinding(IContentType contentType, TMScope scope) {
		ContentTypeToScopeBinding(final String pluginId, final IContentType contentType, final String scopeName) {
			this(contentType, new TMScope(scopeName, pluginId));
		}
	}

	static final class GrammarDefinitions {
		final Map<String, @Nullable IGrammarDefinition> byQualifiedScopeName = new HashMap<>();
		final Map<String /*scopeName*/, List<IGrammarDefinition>> byUnqualifiedScopeName = new HashMap<>();

		void add(final IGrammarDefinition definition) {
			final ITMScope scope = definition.getScope();

			if (scope.isQualified()) {
				byQualifiedScopeName.put(scope.getQualifiedName(), definition);
			}

			byUnqualifiedScopeName
					.computeIfAbsent(scope.getName(), unused -> new ArrayList<>())
					.add(definition);
		}

		/**
		 * @param scopeName an unqualified (sources.batchfile) or qualified (sources.batchfile@plugin) scope name
		 *
		 * @return returns the best matching {@link IGrammarDefinition} for the given scopeName or null
		 */
		@Nullable
		IGrammarDefinition getBestForScope(final String scopeName) {
			// check if the scopeName is qualified and a grammar definition exists
			final IGrammarDefinition definition = byQualifiedScopeName.get(scopeName);
			if (definition != null)
				return definition;

			// check if the scopeName is unqualified return the first grammar bound to it
			final List<IGrammarDefinition> definitionsOfScope = byUnqualifiedScopeName.get(scopeName);
			return definitionsOfScope == null ? null : definitionsOfScope.get(0);
		}

		void remove(final IGrammarDefinition definition) {
			final ITMScope scope = definition.getScope();

			if (scope.isQualified()) {
				byQualifiedScopeName.remove(scope.getQualifiedName());
			}

			final List<IGrammarDefinition> definitionsOfScope = byUnqualifiedScopeName.get(scope.getName());
			if (definitionsOfScope != null) {
				definitionsOfScope.remove(definition);
				if (definitionsOfScope.isEmpty()) {
					byUnqualifiedScopeName.remove(scope.getName());
				}
			}
		}

		Stream<IGrammarDefinition> stream() {
			return byUnqualifiedScopeName.values().stream().flatMap(List::stream);
		}
	}

	static String getQualifiedScopeName(final String scopeName, final String pluginId) {
		return scopeName + '@' + pluginId;
	}

	protected final GrammarDefinitions pluginDefinitions = new GrammarDefinitions();
	protected final GrammarDefinitions userDefinitions = new GrammarDefinitions();

	protected final Map<IContentType, ContentTypeToScopeBinding> contentTypeToScopeBindings = new HashMap<>();
	protected final Map<String /*scopeName*/, Collection<String>> injections = new HashMap<>();

	private final ReloadingRegistry registry;

	protected AbstractGrammarRegistryManager() {
		registry = new ReloadingRegistry(new IRegistryOptions() {
			@Override
			public @Nullable Collection<String> getInjections(final String scopeName) {
				return AbstractGrammarRegistryManager.this.getInjections(scopeName);
			}

			/**
			 * @param scopeName an unqualified (sources.batchfile) or qualified (sources.batchfile@plugin) scope name
			 */
			@Override
			public @Nullable IGrammarSource getGrammarSource(final String scopeName) {
				IGrammarDefinition definition = userDefinitions.getBestForScope(scopeName);
				if (definition == null) {
					definition = pluginDefinitions.getBestForScope(scopeName);
				}
				if (definition == null)
					return null;

				final var definition_ = definition;
				return new IGrammarSource() {
					@Override
					public URI getURI() {
						return definition_.getURI();
					}

					@Override
					public Reader getReader() throws IOException {
						return new InputStreamReader(definition_.getInputStream());
					}

					@Override
					public long getLastModified() {
						return definition_.getLastModified();
					}
				};
			}
		});
	}

	AbstractGrammarRegistryManager(final IRegistryOptions options) {
		registry = new ReloadingRegistry(options);
	}

	@Override
	public @Nullable IGrammar getGrammarFor(final IContentType... contentTypes) {
		// -> used by TMPresentationReconciler
		for (@Nullable
		IContentType contentType : contentTypes) {
			while (contentType != null) {
				final ContentTypeToScopeBinding binding = contentTypeToScopeBindings.get(contentType);
				if (binding == null) {
					contentType = contentType.getBaseType();
					continue;
				}

				// look for a grammar provided by the same plugin as the content-type
				IGrammar grammar = getGrammarForScope(binding.scope.getQualifiedName());
				if (grammar != null)
					return grammar;

				// look for a grammar provided by any plugin
				grammar = getGrammarForScope(binding.scope.getName());
				if (grammar != null)
					return grammar;
			}
		}
		return null;
	}

	@Override
	public @Nullable IGrammar getGrammarForScope(final ITMScope scope) {
		// -> used by GrammarPreferencePage.createGrammarListContent().fillGeneralTab()
		// -> used by GrammarPreferencePage.createGrammarListContent().fillPreview()
		return getGrammarForScope(scope.getQualifiedName());
	}

	/**
	 * @param scopeName an unqualified (sources.batchfile) or qualified (sources.batchfile@plugin) scope name
	 */
	private @Nullable IGrammar getGrammarForScope(final String scopeName) {
		final IGrammar grammar = registry.grammarForScopeName(scopeName);
		return grammar == null ? registry.loadGrammar(scopeName) : grammar;
	}

	@Override
	public @Nullable IGrammar getGrammarForFileExtension(final String fileExt) {
		// -> used by TMPresentationReconciler as fallback if #getGrammarFor(contentTypes) returns nothing
		final String desiredFileExt = fileExt.startsWith(".") ? fileExt.substring(1) : fileExt;
		if (desiredFileExt.isBlank())
			return null;

		/*
		 * first try to lookup grammar via contentTypes that match the file extension
		 */
		for (final ContentTypeToScopeBinding binding : contentTypeToScopeBindings.values()) {
			for (final String contentTypeFileExt : binding.contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)) {
				if (contentTypeFileExt.equals(desiredFileExt)) {
					// look for a grammar provided by the same plugin as the content-type
					IGrammar grammar = getGrammarForScope(binding.scope.getQualifiedName());
					if (grammar != null)
						return grammar;

					// look for a grammar provided by any plugin
					grammar = getGrammarForScope(binding.scope.getName());
					if (grammar != null)
						return grammar;
					break;
				}
			}
		}

		/*
		 * as a fallback try to lookup a matching grammar via the fileType property inside the TextMate grammar file.
		 * this can be expensive as it results in potentially loading/parsing all registered grammar files
		 */
		final Optional<IGrammar> result = Stream //
				.concat(userDefinitions.stream(), pluginDefinitions.stream())
				.map(definition -> {
					final IGrammar grammarForScope = getGrammarForScope(definition.getScope());
					return grammarForScope != null && grammarForScope.getFileTypes().contains(desiredFileExt)
							? grammarForScope
							: null;
				})
				.filter(Objects::nonNull)
				.findFirst();

		// workaround for "Null type mismatch: required '@NonNull IGrammar' but the provided value is null" when using:
		// return result.orElse(null);
		return result.isPresent() ? result.get() : null;
	}

	@Override
	public IGrammarDefinition[] getDefinitions() {
		// -> used by grammars table in GrammarPreferencePage
		return Stream.concat(
				pluginDefinitions.stream(),
				userDefinitions.stream())
				.toArray(IGrammarDefinition[]::new);
	}

	@Override
	public @Nullable Collection<String> getInjections(final String scopeName) {
		// -> indirectly used by org.eclipse.tm4e.core.registry.Registry._doLoadSingleGrammar(String)
		return getInjections(TMScope.parse(scopeName));
	}

	@Override
	public @Nullable Collection<String> getInjections(final ITMScope scope) {
		return injections.get(scope.getName());
	}

	/**
	 * Register the given <code>scopeName</code> to inject to the given scope name <code>injectTo</code>.
	 */
	void registerInjection(final TMScope scopeName, final String injectTo) {
		// -> used by GrammarRegistryManager.loadGrammarsFromExtensionPoints()
		Collection<String> injectionsOfScope = getInjections(injectTo);
		if (injectionsOfScope == null) {
			injectionsOfScope = new ArrayList<>();
			injections.put(injectTo, injectionsOfScope);
		}
		injectionsOfScope.add(scopeName.toString());
	}

	@Override
	public @Nullable Collection<IContentType> getContentTypesForScope(final ITMScope scope) {
		// -> used by GrammarPreferencePage.createGrammarListContent().fillContentTypeTab()
		return contentTypeToScopeBindings.values().stream()
				.filter(binding -> scope.equals(binding.scope))
				.map(binding -> binding.contentType).toList();
	}

	protected void registerContentTypeToScopeBinding(final String pluginId, final IContentType contentType, final String scopeName) {
		// -> used by GrammarRegistryManager.loadGrammarsFromExtensionPoints()
		contentTypeToScopeBindings.put(contentType, new ContentTypeToScopeBinding(pluginId, contentType, scopeName));
	}

	protected void registerGrammarDefinition(final IGrammarDefinition definition) {
		// -> used by GrammarRegistryManager.loadGrammarsFromExtensionPoints()
		// -> used by TextMateGrammarImportWizard.performFinish()
		if (definition.getPluginId() == null) {
			userDefinitions.add(definition);
		} else {
			pluginDefinitions.add(definition);
		}
	}

	protected void unregisterGrammarDefinition(final IGrammarDefinition definition) {
		// -> used by GrammarPreferencePage.grammarRemoveButton
		if (definition.getPluginId() == null) {
			userDefinitions.remove(definition);
		} else {
			pluginDefinitions.remove(definition);
		}
	}
}
