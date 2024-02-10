/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Lucas Bullen (Red Hat Inc.) - initial API and implementation
 * - Sebastian Thomschke (Vegard IT) - code cleanup, refactoring, simplification
 */
package org.eclipse.tm4e.languageconfiguration.internal.registry;

import java.util.HashSet;
import java.util.Set;

import org.osgi.service.prefs.BackingStoreException;

class WorkingCopyLanguageConfigurationRegistryManager extends AbstractLanguageConfigurationRegistryManager
		implements ILanguageConfigurationRegistryManager.EditSession {

	private final LanguageConfigurationRegistryManager manager;
	private final Set<ILanguageConfigurationDefinition> added = new HashSet<>();
	private final Set<ILanguageConfigurationDefinition> removed = new HashSet<>();
	private boolean isDirty = false;

	WorkingCopyLanguageConfigurationRegistryManager(final LanguageConfigurationRegistryManager manager) {
		this.manager = manager;
		reset();
	}

	@Override
	public void reset() {
		pluginDefinitions.clear();
		pluginDefinitions.putAll(manager.pluginDefinitions);
		userDefinitions.clear();
		userDefinitions.putAll(manager.userDefinitions);

		added.clear();
		removed.clear();
		isDirty = false;
	}

	@Override
	public void registerLanguageConfigurationDefinition(final ILanguageConfigurationDefinition definition) {
		super.registerLanguageConfigurationDefinition(definition);
		removed.remove(definition);
		added.add(definition);
		isDirty = true;
	}

	@Override
	public void unregisterLanguageConfigurationDefinition(final ILanguageConfigurationDefinition definition) {
		super.unregisterLanguageConfigurationDefinition(definition);
		added.remove(definition);
		removed.add(definition);
		isDirty = true;
	}

	@Override
	public void save() throws BackingStoreException {
		if (!isDirty)
			return;

		removed.forEach(manager::unregisterLanguageConfigurationDefinition);
		added.forEach(manager::registerLanguageConfigurationDefinition);

		manager.save();
		reset();
	}
}
