/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Sebastian Thomschke (Vegard IT) - add concept of EditSession
 */
package org.eclipse.tm4e.registry;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Looks up TextMate grammars and provides edit sessions for workspace imports and bindings.
 */
public interface IGrammarRegistryManager {

	interface EditSession extends IGrammarRegistryManager {
		/**
		 * resets this session to the current state of the singleton grammar registry manager
		 */
		void reset();

		/**
		 * Add grammar definition to the registry.
		 * Importing the same file again keeps its existing definition and priority in this session.
		 * <p/>
		 * <b>NOTE:</b> you must call {@link #save()} method to make the changes persistent.
		 *
		 * @throws IllegalArgumentException if the file is already imported with a different scope
		 */
		void registerGrammarDefinition(IGrammarDefinition definition);

		/**
		 * Imports a file, reusing its existing entry, and saves its matching-file suggestions.
		 * A non-null name also sets up a workspace default for the given file associations.
		 * A null name preserves any existing setup and bindings.
		 * Nothing is created in Eclipse until {@link #save()}; conflicts leave this session unchanged.
		 *
		 * @throws IllegalArgumentException if associations are unsupported or conflict with another user binding
		 */
		IGrammarDefinition importGrammar(IGrammarDefinition definition, @Nullable String name, List<String> fileAssociations);

		/**
		 * Remove grammar definition from the registry.
		 * <p/>
		 * <b>NOTE:</b> you must call {@link #save()} method to make the changes persistent.
		 */
		void unregisterGrammarDefinition(IGrammarDefinition definition);

		/**
		 * Associates a content type with an imported grammar, replacing any previous user choice.
		 * Pass {@code null} to restore automatic selection. Changes take effect when {@link #save()} is called.
		 * Reopen existing editors to apply the new grammar and editing rules.
		 *
		 * @throws IllegalArgumentException if the grammar is not imported, or an earlier import with the same scope
		 *             points to a different source file. Remove that earlier import before binding this grammar.
		 */
		void setUserGrammarBinding(IContentType contentType, @Nullable IGrammarDefinition definition);

		/**
		 * Merges this session's edits with the latest saved imports and bindings, then persists them before updating the live registry.
		 * Reimporting a file already saved by another session keeps that entry and its priority.
		 * Failed saves leave the live registry unchanged and keep this session's edits available for retry or reset.
		 *
		 * @throws BackingStoreException if another session imported the same file with a different scope or changed
		 *             the source selected by an edited binding, or the workspace setup or preferences cannot be saved
		 */
		void save() throws BackingStoreException;

		/**
		 * @throws UnsupportedOperationException
		 */
		@Override
		default IGrammarRegistryManager.EditSession newEditSession() {
			throw new UnsupportedOperationException();
		}
	}

	IGrammarRegistryManager.EditSession newEditSession();

	/**
	 * @return the list of registered TextMate grammar definitions.
	 */
	IGrammarDefinition[] getDefinitions();

	/** Returns the setup created for this source, or null for imports without managed setup, including old workspaces. */
	@Nullable
	GrammarContentType getGrammarContentType(IGrammarDefinition definition);

	/**
	 * Returns the matching-file suggestions explicitly saved for an imported grammar.
	 * This does not include Eclipse content-type associations. Use this value for editing and stale-save checks.
	 * A null result means no suggestions have been saved and callers may use grammar metadata as a fallback.
	 * An empty list is an explicit choice and must not be replaced with metadata defaults.
	 */
	@Nullable
	List<String> getSavedGrammarFileAssociations(IGrammarDefinition definition);

	/**
	 * Returns the explicit user binding for this exact content type, or {@code null} for automatic selection.
	 */
	@Nullable
	IGrammarDefinition getUserGrammarBinding(IContentType contentType);

	/**
	 * Selects the content type of an explicit user binding, including inherited bindings.
	 * If several user bindings match, the first match in the supplied content-type order wins.
	 * Returns the original content types when no user binding has a grammar that can be loaded.
	 * Editor features must use this selection for both editing rules and syntax highlighting.
	 */
	IContentType[] getEffectiveContentTypes(IContentType... contentTypes);

	/**
	 * Checks user bindings for all supplied content types before checking plugin bindings.
	 * For user bindings, it follows the supplied order and checks each type before its parents.
	 * It skips user bindings whose grammar cannot be loaded.
	 *
	 * @param contentTypes the content types to lookup for grammar association.
	 *
	 * @return the selected {@link IGrammar} that applies to given content-types, or <code>null</code> if no content-type
	 *         has a grammar associated. Grammars associated with parent content-types will be returned if applicable.
	 */
	@Nullable
	IGrammar getGrammarFor(IContentType... contentTypes);

	/**
	 * @return the {@link IGrammar} for the given scope name and null otherwise.
	 */
	@Nullable
	IGrammar getGrammarForScope(ITMScope scope);

	/**
	 * <b>NOTE:</b> This method can be very expensive as it potentially results in eagerly loading of all registered grammar files,
	 * therefore using {@link #getGrammarFor(IContentType...)} should be preferred.
	 *
	 * @param fileExtension a file extension
	 *
	 * @return the {@link IGrammar} for the file type name and null otherwise.
	 */
	@Nullable
	IGrammar getGrammarForFileExtension(String fileExtension);

	/**
	 * Matches the exact scope, including its contributor when qualified.
	 * Includes plugin bindings even when a user choice overrides them for a document.
	 *
	 * @return the list of content types bound with the given scope name and null otherwise.
	 */
	@Nullable
	Collection<IContentType> getContentTypesForScope(ITMScope scope);

	/**
	 * @param scopeName an unqualified (sources.batchfile) or qualified (sources.batchfile@plugin) scope name
	 *
	 * @return list of registered scope names to inject for the given <code>scopeName</code> and null otherwise.
	 */
	@Nullable
	Collection<String> getInjections(String scopeName);

	/**
	 * @return list of registered scope names to inject for the given scope name and null otherwise.
	 */
	@Nullable
	Collection<String> getInjections(ITMScope scope);
}
