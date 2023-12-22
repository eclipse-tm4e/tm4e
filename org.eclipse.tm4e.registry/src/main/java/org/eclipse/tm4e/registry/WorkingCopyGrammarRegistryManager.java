/**
 * Copyright (c) 2015, 2021 Angelo ZERR and others.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.internal.AbstractGrammarRegistryManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Working copy of grammar registry manager used by e.g. tm4e.ui/GrammarPreferencePage.
 */
public class WorkingCopyGrammarRegistryManager extends AbstractGrammarRegistryManager {

	private final IGrammarRegistryManager manager;

	private final List<IGrammarDefinition> added = new ArrayList<>();
	private final List<IGrammarDefinition> removed = new ArrayList<>();

	public WorkingCopyGrammarRegistryManager(final IGrammarRegistryManager manager) {
		this.manager = manager;

		// Copy grammar definitions
		for (final IGrammarDefinition definition : manager.getDefinitions()) {
			super.registerGrammarDefinition(definition);
		}
	}

	@Override
	public @Nullable List<IContentType> getContentTypesForScope(String scopeName) {
		return manager.getContentTypesForScope(scopeName);
	}

	@Override
	public @Nullable Collection<String> getInjections(String scopeName) {
		return manager.getInjections(scopeName);
	}

	@Override
	public void registerGrammarDefinition(final IGrammarDefinition definition) {
		super.registerGrammarDefinition(definition);
		added.add(definition);
	}

	@Override
	public void unregisterGrammarDefinition(final IGrammarDefinition definition) {
		super.unregisterGrammarDefinition(definition);
		if (added.contains(definition)) {
			added.remove(definition);
		} else {
			removed.add(definition);
		}
	}

	@Override
	public void save() throws BackingStoreException {
		if (!added.isEmpty()) {
			for (final IGrammarDefinition definition : added) {
				manager.registerGrammarDefinition(definition);
			}
		}
		if (!removed.isEmpty()) {
			for (final IGrammarDefinition definition : removed) {
				manager.unregisterGrammarDefinition(definition);
			}
		}
		if (!added.isEmpty() || removed.isEmpty()) {
			manager.save();
		}
		added.clear();
		removed.clear();
	}
}
