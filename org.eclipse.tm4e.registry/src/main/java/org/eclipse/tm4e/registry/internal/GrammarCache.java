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
package org.eclipse.tm4e.registry.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.tm4e.registry.IGrammarDefinition;

/**
 * Grammar cache.
 *
 */
public class GrammarCache {

	private final Map<String /* Scope name */, IGrammarDefinition> definitions;
	private final Map<String /* Scope name */, Collection<String>> injections;
	private final Map<String /* IContentType Id */ , String /* Scope name */> scopeNameBindings;

	public GrammarCache() {
		this.definitions = new HashMap<>();
		this.injections = new HashMap<>();
		this.scopeNameBindings = new HashMap<>();
	}

	/**
	 * Register a grammar definition.
	 * 
	 * @param definition
	 *            the grammar definition to register.
	 */
	public void registerGrammarDefinition(IGrammarDefinition definition) {
		definitions.put(definition.getScopeName(), definition);
	}

	public void unregisterGrammarDefinition(IGrammarDefinition definition) {
		definitions.remove(definition.getScopeName());
	}

	/**
	 * Returns the whole registered grammar definition.
	 * 
	 * @return
	 */
	public Collection<IGrammarDefinition> getDefinitions() {
		return this.definitions.values();
	}

	/**
	 * Returns the grammar definition from the given <code>scopeName</code> and
	 * null otherwise.
	 * 
	 * @param scopeName
	 * @return the grammar definition from the given <code>scopeName</code> and
	 *         null otherwise.
	 */
	public IGrammarDefinition getDefinition(String scopeName) {
		return definitions.get(scopeName);
	}

	/**
	 * Returns list of scope names to inject for the given
	 * <code>scopeName</code> and null otheriwse.
	 * 
	 * @param scopeName
	 * @return list of scope names to inject for the given
	 *         <code>scopeName</code> and null otheriwse.
	 */
	public Collection<String> getInjections(String scopeName) {
		return injections.get(scopeName);
	}

	/**
	 * Register the given <code>scopeName</code> to inject to the given scope
	 * name <code>injectTo</code>.
	 * 
	 * @param scopeName
	 * @param injectTo
	 */
	public void registerInjection(String scopeName, String injectTo) {
		Collection<String> injections = getInjections(injectTo);
		if (injections == null) {
			injections = new ArrayList<>();
			this.injections.put(injectTo, injections);
		}
		injections.add(scopeName);
	}

	/**
	 * Returns scope name bound with the given content type and null otherwise.
	 * 
	 * @param contentTypeId
	 * @return scope name bound with the given content type and null otherwise.
	 */
	public String getScopeNameForContentType(String contentTypeId) {
		return scopeNameBindings.get(contentTypeId);
	}

	public String[] getContentTypesForScope(String scopeName) {
		if (scopeName == null) {
			return null;
		}
		return scopeNameBindings.entrySet().stream().filter(map -> scopeName.equals(map.getValue()))
				.map(map -> map.getKey()).collect(Collectors.toList()).toArray(new String[0]);
	}

	public void registerContentTypeBinding(String contentTypeId, String scopeName) {
		scopeNameBindings.put(contentTypeId, scopeName);
	}

}
