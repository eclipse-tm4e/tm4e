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
package org.eclipse.tm4e.languageconfiguration.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationRegistryManager;

public abstract class AbstractLanguageConfigurationRegistryManager implements ILanguageConfigurationRegistryManager {

	protected final Map<IContentType, ILanguageConfigurationDefinition> pluginDefinitions = new HashMap<>();
	protected final Map<IContentType, ILanguageConfigurationDefinition> userDefinitions = new HashMap<>();

	@Override
	public ILanguageConfigurationDefinition[] getDefinitions() {
		Set<ILanguageConfigurationDefinition> definitions = new HashSet<>();
		userDefinitions.values().forEach(definitions::add);
		pluginDefinitions.values().forEach(definitions::add);
		return definitions.toArray(ILanguageConfigurationDefinition[]::new);
	}

	@Override
	public void registerLanguageConfigurationDefinition(ILanguageConfigurationDefinition definition) {
		if (definition.getPluginId() == null) {
			userDefinitions.put(definition.getContentType(), definition);
		} else {
			pluginDefinitions.put(definition.getContentType(), definition);
		}
	}

	@Override
	public void unregisterLanguageConfigurationDefinition(ILanguageConfigurationDefinition definition) {
		if (definition.getPluginId() == null) {
			userDefinitions.remove(definition.getContentType());
		} else {
			pluginDefinitions.remove(definition.getContentType());
		}
	}

	@Override
	public ILanguageConfiguration getLanguageConfigurationFor(IContentType[] contentTypes) {
		for (IContentType contentType : contentTypes) {
			if (userDefinitions.containsKey(contentType)) {
				return userDefinitions.get(contentType).getLanguageConfiguration();
			}
			if (pluginDefinitions.containsKey(contentType)) {
				return pluginDefinitions.get(contentType).getLanguageConfiguration();
			}
		}
		return null;
	}
}
