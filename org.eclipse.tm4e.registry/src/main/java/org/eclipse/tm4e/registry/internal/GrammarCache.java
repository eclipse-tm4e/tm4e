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
package org.eclipse.tm4e.registry.internal;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.IGrammarDefinition;

/**
 * Grammar cache.
 */
final class GrammarCache {

	private final Map<String /* Scope name */, IGrammarDefinition> definitions = new HashMap<>();
	private final Map<String /* Scope name */, Collection<String>> injections = new HashMap<>();
	private final Map<IContentType, String /* Scope name */> scopeNameBindings = new HashMap<>();

	/**
	 * Register a grammar definition.
	 *
	 * @param definition the grammar definition to register.
	 */
	void registerGrammarDefinition(final IGrammarDefinition definition) {
		definitions.put(definition.getScopeName(), definition);
	}

	void unregisterGrammarDefinition(final IGrammarDefinition definition) {
		definitions.remove(definition.getScopeName());
	}

	/**
	 * Returns the whole registered grammar definition.
	 *
	 * @return the whole registered grammar definition.
	 */
	Collection<IGrammarDefinition> getDefinitions() {
		return this.definitions.values();
	}

	/**
	 * Returns the grammar definition from the given <code>scopeName</code> and null otherwise.
	 *
	 * @return the grammar definition from the given <code>scopeName</code> and null otherwise.
	 */
	@Nullable
	IGrammarDefinition getDefinition(final String scopeName) {
		return castNullable(definitions.get(scopeName));
	}

	/**
	 * Returns list of scope names to inject for the given <code>scopeName</code> and null otherwise.
	 *
	 * @return list of scope names to inject for the given <code>scopeName</code> and null otherwise.
	 */
	@Nullable
	Collection<String> getInjections(final String scopeName) {
		return injections.get(scopeName);
	}

	/**
	 * Register the given <code>scopeName</code> to inject to the given scope name <code>injectTo</code>.
	 */
	void registerInjection(final String scopeName, final String injectTo) {
		var injections = getInjections(injectTo);
		if (injections == null) {
			injections = new ArrayList<>();
			this.injections.put(injectTo, injections);
		}
		injections.add(scopeName);
	}

	/**
	 * Returns scope name bound with the given content type and null otherwise.
	 *
	 * @return scope name bound with the given content type and null otherwise.
	 */
	@Nullable
	String getScopeNameForContentType(final IContentType contentType) {
		return scopeNameBindings.get(contentType);
	}

	List<IContentType> getContentTypesForScope(@Nullable final String scopeName) {
		if (scopeName == null) {
			return Collections.emptyList();
		}

		return scopeNameBindings.entrySet().stream().filter(map -> scopeName.equals(map.getValue()))
				.map(Entry::getKey).toList();
	}

	void registerContentTypeBinding(final IContentType contentType, final String scopeName) {
		scopeNameBindings.put(contentType, scopeName);
	}
}
