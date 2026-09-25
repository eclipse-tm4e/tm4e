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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.annotation.Owning;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.registry.GrammarContentType;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;

/**
 * Looks up grammars and workspace bindings so highlighting and editing features use the same language.
 * Shared base for the live registry and its editable copies.
 */
abstract class AbstractGrammarRegistryManager implements IGrammarRegistryManager {

	private static record UserGrammarSelection(IContentType contentType, IGrammar grammar) {
	}

	private static record ContentTypeToScopeBinding(IContentType contentType, TMScope scope) {
		ContentTypeToScopeBinding(final String pluginId, final IContentType contentType, final String scopeName) {
			this(contentType, new TMScope(scopeName, pluginId));
		}
	}

	static final class GrammarDefinitions {
		final Map<String, @Nullable IGrammarDefinition> byQualifiedScopeName = new HashMap<>();
		final Map<String /*scopeName*/, List<IGrammarDefinition>> byUnqualifiedScopeName = new HashMap<>();

		void copyFrom(final GrammarDefinitions source) {
			byQualifiedScopeName.clear();
			byQualifiedScopeName.putAll(source.byQualifiedScopeName);
			byUnqualifiedScopeName.clear();
			// Each edit session needs separate lists. Otherwise an unsaved addition or removal would change the live registry too.
			source.byUnqualifiedScopeName.forEach((scope, definitions) -> byUnqualifiedScopeName.put(scope, new ArrayList<>(definitions)));
		}

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
	protected final Map<String /*contentTypeId*/, String /*scopeName*/> userContentTypeToScopeBindings = new HashMap<>();
	protected final Map<String /*scopeName*/, Collection<String>> injections = new HashMap<>();
	protected final Map<String /*source URI*/, GrammarContentType> grammarContentTypes = new HashMap<>();
	protected final Map<String /*source URI*/, List<String>> grammarFileAssociations = new HashMap<>();

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
					public @Owning Reader getReader() throws IOException {
						return new InputStreamReader(definition_.getInputStream(), StandardCharsets.UTF_8);
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
	public @Nullable GrammarContentType getGrammarContentType(final IGrammarDefinition definition) {
		return grammarContentTypes.get(sourceKey(definition));
	}

	@Override
	public @Nullable List<String> getSavedGrammarFileAssociations(final IGrammarDefinition definition) {
		return grammarFileAssociations.get(sourceKey(definition));
	}

	static String sourceKey(final IGrammarDefinition definition) {
		return definition.getURI().normalize().toString();
	}

	@Override
	public @Nullable IGrammarDefinition getUserGrammarBinding(final IContentType contentType) {
		final String scopeName = userContentTypeToScopeBindings.get(contentType.getId());
		// A removed import must not resolve to a contributed grammar with the same unqualified scope.
		return scopeName == null ? null : userDefinitions.getBestForScope(scopeName);
	}

	private @Nullable UserGrammarSelection findUserGrammar(final IContentType[] contentTypes) {
		if (userContentTypeToScopeBindings.isEmpty())
			return null;
		// A user binding takes priority over plugin bindings for every matching content type.
		// Keep Eclipse's type order when several user bindings match. Check each type before its parents.
		for (final IContentType candidate : contentTypes) {
			for (IContentType type = candidate; type != null; type = type.getBaseType()) {
				final var definition = getUserGrammarBinding(type);
				if (definition != null) {
					// Unavailable grammar files must not switch editing rules while highlighting falls back to another language.
					try {
						final IGrammar grammar = getGrammarForScope(definition.getScope());
						if (grammar != null)
							return new UserGrammarSelection(type, grammar);
					} catch (final TMException ex) {
						TMEclipseRegistryPlugin.logError("Cannot apply user grammar binding for " + type.getId(), ex);
					}
				}
			}
		}
		return null;
	}

	@Override
	public IContentType[] getEffectiveContentTypes(final IContentType... contentTypes) {
		final UserGrammarSelection selection = findUserGrammar(contentTypes);
		// Return only the selected type because several editing features combine rules from every type they receive.
		// For inherited bindings, this also selects the configuration associated with the user's chosen parent type.
		return selection == null ? contentTypes : new IContentType[] { selection.contentType };
	}

	@Override
	public @Nullable IGrammar getGrammarFor(final IContentType... contentTypes) {
		final UserGrammarSelection selection = findUserGrammar(contentTypes);
		if (selection != null)
			return selection.grammar;
		// -> used by TMPresentationReconciler
		for (@Nullable
		IContentType contentType : contentTypes) {
			while (contentType != null) {
				final ContentTypeToScopeBinding binding = contentTypeToScopeBindings.get(contentType);
				if (binding == null) {
					// check parent content type
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

				// check parent content type
				contentType = contentType.getBaseType();
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

		if (!userContentTypeToScopeBindings.isEmpty()) {
			// Eclipse's filename matcher includes inherited extensions; getFileSpecs() only returns a type's own associations.
			// Without file contents, several types can match. Sort by ID to keep the choice predictable.
			final IContentType[] candidates = Arrays.stream(Platform.getContentTypeManager().findContentTypesFor("file." + desiredFileExt))
					.sorted(Comparator.comparing(IContentType::getId))
					.toArray(IContentType[]::new);
			final UserGrammarSelection selection = findUserGrammar(candidates);
			if (selection != null)
				return selection.grammar;
		}

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
					try {
						final IGrammar grammarForScope = getGrammarForScope(definition.getScope());
						return grammarForScope != null && grammarForScope.getFileTypes().contains(desiredFileExt)
								? grammarForScope
								: null;
					} catch (final TMException ex) {
						// Loading precedes the fileTypes check, so one missing import must not disable unrelated languages.
						TMEclipseRegistryPlugin.logError("Cannot load grammar candidate " + definition.getPath(), ex);
						return null;
					}
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
		final var contributed = contentTypeToScopeBindings.values().stream()
				.filter(binding -> scope.equals(binding.scope))
				.map(binding -> binding.contentType);
		final var user = userContentTypeToScopeBindings.entrySet().stream()
				.filter(entry -> entry.getValue().equals(scope.getQualifiedName()))
				.map(Map.Entry::getKey).sorted()
				.map(id -> Platform.getContentTypeManager().getContentType(id))
				.filter(Objects::nonNull);
		// Show contributed bindings even when a workspace choice overrides them for a document.
		return Stream.concat(contributed, user).distinct().toList();
	}

	/**
	 * Returns the contributed content types bound to an unqualified grammar scope.
	 * Used to find the editing rules for TM partitions, whose scopes carry no contributor.
	 * <p>
	 * A binding matches by scope name alone, because the bundle contributing a binding need not own the grammar.
	 * Workspace bindings are excluded: they choose a grammar for documents of one content type
	 * and must not apply editing rules to regions of other documents.
	 * <p>
	 * Editing features use the first returned type that has a configuration, so the order is part of the contract:
	 * bindings contributed by the bundle of the scope's grammar come first, then those of other bundles.
	 * Each group is ordered by content-type ID.
	 */
	public List<IContentType> getContributedContentTypesForScope(final String scopeName) {
		// Use the plugin grammar that getGrammarSource() loads for an unqualified scope, so the rules come from the same
		// bundle as the tokens. An imported grammar may shadow it, but imported grammars have no contributed bindings.
		// Contributed bindings always carry a plugin ID, so without a plugin grammar all of them fall into the second group.
		final @Nullable IGrammarDefinition owner = pluginDefinitions.getBestForScope(scopeName);
		final @Nullable String ownerPluginId = owner == null ? null : owner.getPluginId();
		return contentTypeToScopeBindings.values().stream()
				.filter(binding -> scopeName.equals(binding.scope.getName()))
				// Bindings from other bundles only add fallbacks. Otherwise any bundle binding its own content type to
				// this scope would replace the owner's editing rules everywhere, merely because its ID sorts first.
				// The bindings map has no stable order, so the ID keeps the result stable within each group.
				.sorted(Comparator
						.comparingInt((final ContentTypeToScopeBinding binding) -> Objects.equals(binding.scope.getPluginId(), ownerPluginId) ? 0 : 1)
						.thenComparing(binding -> binding.contentType.getId()))
				.map(ContentTypeToScopeBinding::contentType)
				.toList();
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
			// Bindings identify scopes, so keep them while another imported definition still supplies that scope.
			if (userDefinitions.getBestForScope(definition.getScope().getName()) == null) {
				userContentTypeToScopeBindings.values().removeIf(definition.getScope().getName()::equals);
			}
		} else {
			pluginDefinitions.remove(definition);
		}
	}
}
