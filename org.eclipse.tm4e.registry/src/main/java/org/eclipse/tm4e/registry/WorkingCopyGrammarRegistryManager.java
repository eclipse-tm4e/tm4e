/**
 *  Copyright (c) 2015, 2021 Angelo ZERR and others.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tm4e.registry.internal.AbstractGrammarRegistryManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Working copy of grammar registry manager.
 *
 */
public class WorkingCopyGrammarRegistryManager extends AbstractGrammarRegistryManager {

	private final IGrammarRegistryManager manager;

	private List<IGrammarDefinition> added;

	private List<IGrammarDefinition> removed;

	public WorkingCopyGrammarRegistryManager(final IGrammarRegistryManager manager) {
		this.manager = manager;
		load();
	}

	private void load() {
		// Copy grammar definitions
		final IGrammarDefinition[] definitions = manager.getDefinitions();
		for (final IGrammarDefinition definition : definitions) {
			super.registerGrammarDefinition(definition);
			// Copy binding scope/content types
			final String scopeName = definition.getScopeName();
			manager.getContentTypesForScope(scopeName).forEach(contentType -> super.registerContentTypeBinding(contentType, scopeName));
			// Copy injection
			final Collection<String> injections = manager.getInjections(scopeName);
			if (injections != null) {
				for (final String injectFrom : injections) {
					super.registerInjection(injectFrom, scopeName);
				}
			}
		}
	}

	@Override
	public void registerGrammarDefinition(final IGrammarDefinition definition) {
		super.registerGrammarDefinition(definition);
		if (added == null) {
			added = new ArrayList<>();
		}
		added.add(definition);
	}

	@Override
	public void unregisterGrammarDefinition(final IGrammarDefinition definition) {
		super.unregisterGrammarDefinition(definition);
		if (added != null && added.contains(definition)) {
			added.remove(definition);
		} else {
			if (removed == null) {
				removed = new ArrayList<>();
			}
			removed.add(definition);
		}
	}

	@Override
	public void save() throws BackingStoreException {
		if (added != null) {
			for (final IGrammarDefinition definition : added) {
				manager.registerGrammarDefinition(definition);
			}
		}
		if (removed != null) {
			for (final IGrammarDefinition definition : removed) {
				manager.unregisterGrammarDefinition(definition);
			}
		}
		if (added != null || removed != null) {
			manager.save();
		}
	}

}
