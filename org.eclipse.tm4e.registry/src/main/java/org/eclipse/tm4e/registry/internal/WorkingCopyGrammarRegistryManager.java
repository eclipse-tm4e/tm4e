/**
 * Copyright (c) 2015, 2021 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Sebastian Thomschke (Vegard IT) - code cleanup, refactoring, simplification
 */
package org.eclipse.tm4e.registry.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.ITMScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Working copy of grammar registry manager used by e.g. tm4e.ui/GrammarPreferencePage.
 */
class WorkingCopyGrammarRegistryManager extends AbstractGrammarRegistryManager implements IGrammarRegistryManager.EditSession {

	private final GrammarRegistryManager manager;
	private final Set<IGrammarDefinition> added = new HashSet<>();
	private final Set<IGrammarDefinition> removed = new HashSet<>();
	private boolean isDirty = false;

	WorkingCopyGrammarRegistryManager(final GrammarRegistryManager manager) {
		this.manager = manager;
		reset();
	}

	@Override
	public void reset() {
		pluginDefinitions.byQualifiedScopeName.clear();
		pluginDefinitions.byQualifiedScopeName.putAll(manager.pluginDefinitions.byQualifiedScopeName);
		pluginDefinitions.byUnqualifiedScopeName.clear();
		pluginDefinitions.byUnqualifiedScopeName.putAll(manager.pluginDefinitions.byUnqualifiedScopeName);
		userDefinitions.byQualifiedScopeName.clear();
		userDefinitions.byQualifiedScopeName.putAll(manager.userDefinitions.byQualifiedScopeName);
		userDefinitions.byUnqualifiedScopeName.clear();
		userDefinitions.byUnqualifiedScopeName.putAll(manager.userDefinitions.byUnqualifiedScopeName);

		added.clear();
		removed.clear();
		isDirty = false;
	}

	@Override
	public @Nullable Collection<IContentType> getContentTypesForScope(ITMScope scope) {
		return manager.getContentTypesForScope(scope);
	}

	@Override
	public @Nullable Collection<String> getInjections(String scopeName) {
		return manager.getInjections(scopeName);
	}

	@Override
	public void registerGrammarDefinition(final IGrammarDefinition definition) {
		super.registerGrammarDefinition(definition);
		removed.remove(definition);
		added.add(definition);
		isDirty = true;
	}

	@Override
	public void unregisterGrammarDefinition(final IGrammarDefinition definition) {
		super.unregisterGrammarDefinition(definition);
		added.remove(definition);
		removed.add(definition);
		isDirty = true;
	}

	@Override
	public void save() throws BackingStoreException {
		if (!isDirty)
			return;

		removed.forEach(manager::unregisterGrammarDefinition);
		added.forEach(manager::registerGrammarDefinition);

		manager.save();
		reset();
	}
}
