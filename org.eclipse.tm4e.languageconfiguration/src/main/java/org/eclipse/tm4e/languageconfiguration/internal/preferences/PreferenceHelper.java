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
package org.eclipse.tm4e.languageconfiguration.internal.preferences;

import java.util.Collection;

import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationDefinition;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

/**
 * Helper class load, save language configuration preferences with Json format.
 *
 */
public class PreferenceHelper {

	private static final Gson DEFAULT_GSON;

	static {
		DEFAULT_GSON = new GsonBuilder().registerTypeAdapter(LanguageConfigurationDefinition.class,
				(JsonDeserializer<LanguageConfigurationDefinition>) (json, typeOfT, context) -> {
					JsonObject object = json.getAsJsonObject();
					JsonElement pluginId = object.get("pluginId");
					return new LanguageConfigurationDefinition(
							ContentTypeHelper.getContentTypeById(object.get("contentTypeId").getAsString()), //$NON-NLS-1$
							object.get("path").getAsString(), //$NON-NLS-1$
							pluginId == null ? null : pluginId.getAsString(),
							object.get("onEnterEnabled").getAsBoolean(), //$NON-NLS-1$
							object.get("bracketAutoClosingEnabled").getAsBoolean(), //$NON-NLS-1$
							object.get("matchingPairsEnabled").getAsBoolean()); //$NON-NLS-1$
				}).registerTypeAdapter(LanguageConfigurationDefinition.class,
						(JsonSerializer<LanguageConfigurationDefinition>) (definition, typeOfT, context) -> {
							JsonObject object = new JsonObject();
							object.addProperty("path", definition.getPath()); //$NON-NLS-1$
							object.addProperty("pluginId", definition.getPluginId()); //$NON-NLS-1$
							object.addProperty("contentTypeId", definition.getContentType().getId()); //$NON-NLS-1$
							object.addProperty("onEnterEnabled", definition.isOnEnterEnabled()); //$NON-NLS-1$
							object.addProperty("bracketAutoClosingEnabled", definition.isBracketAutoClosingEnabled()); //$NON-NLS-1$
							object.addProperty("matchingPairsEnabled", definition.isMatchingPairsEnabled()); //$NON-NLS-1$
							return object;
						})
				.create();
	}

	public static ILanguageConfigurationDefinition[] loadLanguageConfigurationDefinitions(String json) {
		return DEFAULT_GSON.fromJson(json, LanguageConfigurationDefinition[].class);
	}

	public static String toJson(Collection<ILanguageConfigurationDefinition> definitions) {
		return DEFAULT_GSON.toJson(definitions);
	}

}
