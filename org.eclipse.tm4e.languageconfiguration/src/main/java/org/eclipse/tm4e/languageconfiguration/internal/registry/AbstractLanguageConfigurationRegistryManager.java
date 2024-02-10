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
 */
package org.eclipse.tm4e.languageconfiguration.internal.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;

abstract class AbstractLanguageConfigurationRegistryManager implements ILanguageConfigurationRegistryManager {

	final Map<IContentType, ILanguageConfigurationDefinition> pluginDefinitions = new HashMap<>();
	final Map<IContentType, ILanguageConfigurationDefinition> userDefinitions = new HashMap<>();

	@Override
	public ILanguageConfigurationDefinition[] getDefinitions() {
		final var definitions = new HashSet<ILanguageConfigurationDefinition>();
		userDefinitions.values().forEach(definitions::add);
		pluginDefinitions.values().forEach(definitions::add);
		return definitions.toArray(ILanguageConfigurationDefinition[]::new);
	}

	void registerLanguageConfigurationDefinition(final ILanguageConfigurationDefinition definition) {
		(definition.getPluginId() == null ? userDefinitions : pluginDefinitions).put(definition.getContentType(), definition);
	}

	void unregisterLanguageConfigurationDefinition(final ILanguageConfigurationDefinition definition) {
		(definition.getPluginId() == null ? userDefinitions : pluginDefinitions).remove(definition.getContentType());
	}

	@Nullable
	@Override
	public LanguageConfiguration getLanguageConfigurationFor(final IContentType... contentTypes) {
		for (final IContentType contentType : contentTypes) {
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
