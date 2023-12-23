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
package org.eclipse.tm4e.registry;

import java.util.Collection;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.osgi.service.prefs.BackingStoreException;

/**
 * TextMate Grammar registry manager API.
 */
public interface IGrammarRegistryManager {

	// --------------- TextMate grammar definitions methods

	/**
	 * @return the list of registered TextMate grammar definitions.
	 */
	IGrammarDefinition[] getDefinitions();

	/**
	 * Add grammar definition to the registry.
	 * <p/>
	 * <b>NOTE:</b> you must call {@link #save()} method to make the changes persistent.
	 */
	void registerGrammarDefinition(IGrammarDefinition definition);

	/**
	 * Remove grammar definition from the registry.
	 * <p/>
	 * <b>NOTE:</b> you must call {@link #save()} method to make the changes persistent.
	 */
	void unregisterGrammarDefinition(IGrammarDefinition definition);

	/**
	 * Save the grammar definitions
	 */
	void save() throws BackingStoreException;

	// --------------- TextMate grammar queries methods.

	/**
	 * @param contentTypes the content types to lookup for grammar association.
	 *
	 * @return the first {@link IGrammar} that applies to given content-types, or <code>null</code> if no content-type
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
	 * @return the list of content types bound with the given scope name and null otherwise.
	 */
	@Nullable
	Collection<IContentType> getContentTypesForScope(ITMScope scope);

	/**
	 * @return list of scope names to inject for the given <code>scopeName</code> and null otherwise.
	 */
	@Nullable
	Collection<String> getInjections(String scopeName);
}
