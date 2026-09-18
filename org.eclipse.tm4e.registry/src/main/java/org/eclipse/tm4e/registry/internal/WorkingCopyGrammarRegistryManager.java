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
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.ITMScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Edits grammar imports without changing the live registry until the session is saved.
 */
class WorkingCopyGrammarRegistryManager extends AbstractGrammarRegistryManager implements IGrammarRegistryManager.EditSession {

	private final GrammarRegistryManager manager;
	// The first import wins when several grammars share a scope, so saving must keep insertion order.
	private final Set<IGrammarDefinition> added = new LinkedHashSet<>();
	private final Set<IGrammarDefinition> removed = new HashSet<>();
	private boolean isDirty = false;

	WorkingCopyGrammarRegistryManager(final GrammarRegistryManager manager) {
		this.manager = manager;
		reset();
	}

	@Override
	public void reset() {
		pluginDefinitions.copyFrom(manager.pluginDefinitions);
		userDefinitions.copyFrom(manager.userDefinitions);

		added.clear();
		removed.clear();
		isDirty = false;
	}

	@Override
	public @Nullable Collection<IContentType> getContentTypesForScope(final ITMScope scope) {
		return manager.getContentTypesForScope(scope);
	}

	@Override
	public @Nullable Collection<String> getInjections(final String scopeName) {
		return manager.getInjections(scopeName);
	}

	@Override
	public @Nullable Collection<String> getInjections(final ITMScope scope) {
		return manager.getInjections(scope);
	}

	@Override
	public void registerGrammarDefinition(final IGrammarDefinition definition) {
		if (definition.getPluginId() == null) {
			final var existing = userDefinitions.stream().filter(item -> sourceKey(item).equals(sourceKey(definition))).findFirst();
			if (existing.isPresent()) {
				// Reimporting must keep the original entry and its priority among grammars sharing a scope.
				if (!existing.get().getScope().equals(definition.getScope()))
					throw new IllegalArgumentException("This file was imported with a different scope. Remove the old import first.");
				return;
			}
		}
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
