/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.registry;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Language Configuration registry manager API.
 */
public interface ILanguageConfigurationRegistryManager {

	// --------------- Language configuration definitions methods

	/**
	 * Returns the list of registered language configuration definitions.
	 *
	 * @return the list of registered language configuration definitions.
	 */
	ILanguageConfigurationDefinition[] getDefinitions();

	/**
	 * Add language configuration definition to the registry.
	 *
	 * NOTE: you must call save() method if you wish to save in the preferences.
	 */
	void registerLanguageConfigurationDefinition(ILanguageConfigurationDefinition definition);

	/**
	 * Remove language configuration definition from the registry.
	 *
	 * NOTE: you must call save() method if you wish to save in the preferences.
	 */
	void unregisterLanguageConfigurationDefinition(ILanguageConfigurationDefinition definition);

	/**
	 * Save the language configuration definitions.
	 */
	void save() throws BackingStoreException;

	// --------------- Language configuration queries methods.

	/**
	 * Returns the {@link LanguageConfiguration} for the given content types and null otherwise.
	 *
	 * @param contentTypes the content type.
	 *
	 * @return the {@link LanguageConfiguration} for the given content type and null otherwise.
	 */
	@Nullable
	LanguageConfiguration getLanguageConfigurationFor(IContentType... contentTypes);
}
