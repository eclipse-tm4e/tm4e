/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.preferences;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationDefinition;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

/**
 * Helper class load, save language configuration preferences with Json format.
 */
public final class PreferenceHelper {

	@NonNullByDefault({})
	private static final Gson DEFAULT_GSON = new GsonBuilder()
			.registerTypeAdapter(LanguageConfigurationDefinition.class,
					(JsonDeserializer<LanguageConfigurationDefinition>) (json, typeOfT, context) -> {
						final JsonObject object = json.getAsJsonObject();
						final JsonElement pluginId = object.get("pluginId");
						final var contentTypeId = object.get("contentTypeId").getAsString();
						final var contentType = ContentTypeHelper.getContentTypeById(contentTypeId);
						if (contentType == null) {
							LanguageConfigurationPlugin.logError(
									"Cannot load language configuration with unknown content type ID " + contentTypeId,
									null);
							return null;
						}
						return new LanguageConfigurationDefinition(contentType,
								object.get("path").getAsString(),
								pluginId == null ? null : pluginId.getAsString(),
								getAsBoolean(object, "onEnterEnabled", true),
								getAsBoolean(object, "indentRulesEnabled", true),
								getAsBoolean(object, "bracketAutoClosingEnabled", true),
								getAsBoolean(object, "matchingPairsEnabled", true));
					})
			.registerTypeAdapter(LanguageConfigurationDefinition.class,
					(JsonSerializer<LanguageConfigurationDefinition>) (definition, typeOfT, context) -> {
						final JsonObject object = new JsonObject();
						object.addProperty("path", definition.getPath());
						object.addProperty("pluginId", definition.getPluginId());
						object.addProperty("contentTypeId", definition.getContentType().getId());
						object.addProperty("onEnterEnabled", definition.isOnEnterEnabled());
						object.addProperty("indentRulesEnabled", definition.isIndentRulesEnabled());
						object.addProperty("bracketAutoClosingEnabled", definition.isBracketAutoClosingEnabled());
						object.addProperty("matchingPairsEnabled", definition.isMatchingPairsEnabled());
						return object;
					})
			.create();

	private static boolean getAsBoolean(final JsonObject obj, final String property, final boolean defaultValue) {
		final JsonElement elem = obj.get(property);
		if (elem == null)
			return defaultValue;
		return elem.getAsBoolean();
	}

	public static ILanguageConfigurationDefinition[] loadLanguageConfigurationDefinitions(final String json) {
		return Arrays.stream(DEFAULT_GSON.fromJson(json, LanguageConfigurationDefinition[].class))
				.filter(Objects::nonNull)
				.toArray(ILanguageConfigurationDefinition[]::new);
	}

	public static String toJson(final Collection<ILanguageConfigurationDefinition> definitions) {
		return DEFAULT_GSON.toJson(definitions);
	}

	private PreferenceHelper() {
	}
}
