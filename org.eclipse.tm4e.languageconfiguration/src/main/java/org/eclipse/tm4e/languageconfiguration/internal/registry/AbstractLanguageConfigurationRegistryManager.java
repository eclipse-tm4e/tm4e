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
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;


/**
 * Looks up editing configurations for content types, with or without applying workspace grammar bindings.
 */
abstract class AbstractLanguageConfigurationRegistryManager implements ILanguageConfigurationRegistryManager {

	final Map<IContentType, ILanguageConfigurationDefinition> pluginDefinitions = new HashMap<>();
	final Map<IContentType, ILanguageConfigurationDefinition> userDefinitions = new HashMap<>();

	@Override
	public ILanguageConfigurationDefinition[] getDefinitions() {
		Set<ILanguageConfigurationDefinition> definitions =
				new LinkedHashSet<>();

		definitions.addAll(pluginDefinitions.values());
		definitions.addAll(userDefinitions.values());

		return definitions.toArray(
				ILanguageConfigurationDefinition[]::new
		);
	}

	void registerLanguageConfigurationDefinition(final ILanguageConfigurationDefinition definition) {
		(definition.getPluginId() == null ? userDefinitions : pluginDefinitions).put(definition.getContentType(), definition);
	}

	void unregisterLanguageConfigurationDefinition(final ILanguageConfigurationDefinition definition) {
		(definition.getPluginId() == null ? userDefinitions : pluginDefinitions).remove(definition.getContentType());
	}

	@Override
	public @Nullable LanguageConfiguration getLanguageConfigurationFor(final IContentType... contentTypes) {
		// Some callers supply types directly rather than going through the document helper (for example folding).
		return getLanguageConfigurationForResolvedTypes(
				TMEclipseRegistryPlugin.getGrammarRegistryManager().getEffectiveContentTypes(contentTypes));
	}

	@Override
	public @Nullable LanguageConfiguration getLanguageConfigurationForResolvedTypes(final IContentType... contentTypes) {
		for (final IContentType contentType : contentTypes) {
			final var userDefinition = userDefinitions.get(contentType);
			if (userDefinition != null) {
				return userDefinition.getLanguageConfiguration();
			}
			final var pluginDefinition = pluginDefinitions.get(contentType);
			if (pluginDefinition != null) {
				return pluginDefinition.getLanguageConfiguration();
			}
		}
		return null;
	}
}
