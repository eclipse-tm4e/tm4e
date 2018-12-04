/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.languageconfiguration.internal.AbstractLanguageConfigurationRegistryManager;
import org.osgi.service.prefs.BackingStoreException;

public class WorkingCopyLanguageConfigurationRegistryManager extends AbstractLanguageConfigurationRegistryManager {

	private final ILanguageConfigurationRegistryManager manager;

	private List<ILanguageConfigurationDefinition> added;

	private List<ILanguageConfigurationDefinition> removed;

	public WorkingCopyLanguageConfigurationRegistryManager(ILanguageConfigurationRegistryManager manager) {
		this.manager = manager;
		load();
	}

	private void load() {
		// Copy definitions
		ILanguageConfigurationDefinition[] definitions = manager.getDefinitions();
		for (ILanguageConfigurationDefinition definition : definitions) {
			super.registerLanguageConfigurationDefinition(definition);
		}
	}

	@Override
	public void registerLanguageConfigurationDefinition(ILanguageConfigurationDefinition definition) {
		super.registerLanguageConfigurationDefinition(definition);
		if (added == null) {
			added = new ArrayList<>();
		}
		added.add(definition);
	}

	@Override
	public void unregisterLanguageConfigurationDefinition(ILanguageConfigurationDefinition definition) {
		super.unregisterLanguageConfigurationDefinition(definition);
		if (removed == null) {
			removed = new ArrayList<>();
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
			for (ILanguageConfigurationDefinition definition : removed) {
				manager.unregisterLanguageConfigurationDefinition(definition);
			}
		}
		if (added != null) {
			for (ILanguageConfigurationDefinition definition : added) {
				manager.registerLanguageConfigurationDefinition(definition);
			}
		}
		if (added != null || removed != null) {
			manager.save();
		}
	}

}
