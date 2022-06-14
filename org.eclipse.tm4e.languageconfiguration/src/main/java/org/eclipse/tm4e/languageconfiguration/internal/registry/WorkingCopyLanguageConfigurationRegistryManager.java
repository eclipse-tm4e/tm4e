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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.prefs.BackingStoreException;

public class WorkingCopyLanguageConfigurationRegistryManager extends AbstractLanguageConfigurationRegistryManager {

	private final ILanguageConfigurationRegistryManager manager;

	@Nullable
	private List<ILanguageConfigurationDefinition> added;

	@Nullable
	private List<ILanguageConfigurationDefinition> removed;

	public WorkingCopyLanguageConfigurationRegistryManager(final ILanguageConfigurationRegistryManager manager) {
		this.manager = manager;

		// Copy definitions
		final ILanguageConfigurationDefinition[] definitions = manager.getDefinitions();
		for (final ILanguageConfigurationDefinition definition : definitions) {
			super.registerLanguageConfigurationDefinition(definition);
		}
	}

	@Override
	public void registerLanguageConfigurationDefinition(final ILanguageConfigurationDefinition definition) {
		super.registerLanguageConfigurationDefinition(definition);
		var added = this.added;
		if (added == null) {
			added = this.added = new ArrayList<>();
		}
		added.add(definition);
	}

	@Override
	public void unregisterLanguageConfigurationDefinition(final ILanguageConfigurationDefinition definition) {
		super.unregisterLanguageConfigurationDefinition(definition);
		var removed = this.added;
		if (removed == null) {
			removed = this.removed = new ArrayList<>();
		}
		if (added != null) {
			added.remove(definition);
		} else {
			removed.add(definition);
		}
	}

	@Override
	public void save() throws BackingStoreException {
		if (removed != null) {
			for (final ILanguageConfigurationDefinition definition : removed) {
				manager.unregisterLanguageConfigurationDefinition(definition);
			}
		}
		if (added != null) {
			for (final ILanguageConfigurationDefinition definition : added) {
				manager.registerLanguageConfigurationDefinition(definition);
			}
		}
		if (added != null || removed != null) {
			manager.save();
		}
	}
}
