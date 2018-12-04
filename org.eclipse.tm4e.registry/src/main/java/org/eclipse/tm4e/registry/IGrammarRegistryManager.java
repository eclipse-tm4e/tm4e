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
package org.eclipse.tm4e.registry;

import java.util.Collection;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 * TextMate Grammar registry manager API.
 *
 */
public interface IGrammarRegistryManager {

	// --------------- TextMate grammar definitions methods

	/**
	 * Returns the list of registered TextMate grammar definitions.
	 * 
	 * @return the list of registered TextMate grammar definitions.
	 */
	IGrammarDefinition[] getDefinitions();

	/**
	 * Add grammar definition to the registry.
	 * 
	 * NOTE: you must call save() method if you wish to save in the preferences.
	 * 
	 * @param definition
	 */
	void registerGrammarDefinition(IGrammarDefinition definition);

	/**
	 * Remove grammar definition from the registry.
	 * 
	 * NOTE: you must call save() method if you wish to save in the preferences.
	 * 
	 * @param definition
	 */
	void unregisterGrammarDefinition(IGrammarDefinition definition);

	/**
	 * Save the grammar definitions.
	 * @throws BackingStoreException 
	 */
	void save() throws BackingStoreException;

	// --------------- TextMate grammar queries methods.

	/**
	 * Returns the {@link IGrammar} for the given content types and null
	 * otherwise.
	 * 
	 * @param contentTypes
	 *            the content type.
	 * @return the {@link IGrammar} for the given content type and null
	 *         otherwise.
	 */
	IGrammar getGrammarFor(IContentType[] contentTypes);

	/**
	 * Returns the {@link IGrammar} for the given scope name and null otherwise.
	 * 
	 * @param contentTypes
	 *            the content type.
	 * @return the {@link IGrammar} for the given scope name and null otherwise.
	 */
	IGrammar getGrammarForScope(String scopeName);

	/**
	 * Returns the {@link IGrammar} for the given file type and null otherwise.
	 * 
	 * @param contentTypes
	 *            the content type.
	 * @return the {@link IGrammar} for the file type name and null otherwise.
	 */
	IGrammar getGrammarForFileType(String fileType);

	/**
	 * Returns the list of content types bound with the given scope name and
	 * null otherwise.
	 * 
	 * @param scopeName
	 * @return the list of content types bound with the given scope name and
	 *         null otherwise.
	 */
	String[] getContentTypesForScope(String scopeName);
	
	Collection<String> getInjections(String scopeName);
}
