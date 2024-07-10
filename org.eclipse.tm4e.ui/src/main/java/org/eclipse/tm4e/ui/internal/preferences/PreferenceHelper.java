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
package org.eclipse.tm4e.ui.internal.preferences;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Helper class to load, save preferences in JSON format.
 */
public final class PreferenceHelper {

	private static final Gson DEFAULT_GSON = new GsonBuilder() //
			.registerTypeAdapter(IThemeAssociation.class, (InstanceCreator<ThemeAssociation>) type -> new ThemeAssociation())
			.registerTypeAdapterFactory(new TypeAdapterFactory() {
				@SuppressWarnings("unchecked")
				@Override
				public @Nullable <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
					if (!MarkerConfig.class.isAssignableFrom(type.getRawType()))
						return null;

					final var jsonElementAdapter = gson.getAdapter(JsonElement.class);
					final var problemAdapter = gson.getDelegateAdapter(this, TypeToken.get(MarkerConfig.ProblemMarkerConfig.class));
					final var taskAdapter = gson.getDelegateAdapter(this, TypeToken.get(MarkerConfig.TaskMarkerConfig.class));
					return (TypeAdapter<T>) new TypeAdapter<MarkerConfig>() {
						@Override
						public void write(final JsonWriter out, final @Nullable MarkerConfig value) throws IOException {
							final var valueClass = castNonNull(value).getClass();
							if (valueClass.isAssignableFrom(MarkerConfig.ProblemMarkerConfig.class)) {
								problemAdapter.write(out, (MarkerConfig.ProblemMarkerConfig) value);
							} else if (valueClass.isAssignableFrom(MarkerConfig.TaskMarkerConfig.class)) {
								taskAdapter.write(out, (MarkerConfig.TaskMarkerConfig) value);
							}
						}

						@Override
						public @Nullable MarkerConfig read(final JsonReader in) throws IOException {
							final var objectJson = castNonNull(jsonElementAdapter.read(in)).getAsJsonObject();
							return switch (MarkerConfig.Type.valueOf(objectJson.get("type").getAsString())) {
								case PROBLEM -> problemAdapter.fromJsonTree(objectJson);
								case TASK -> taskAdapter.fromJsonTree(objectJson);
							};
						}
					};
				}
			}).create();

	public static IThemeAssociation[] loadThemeAssociations(final String json) {
		return castNonNull(DEFAULT_GSON.fromJson(json, ThemeAssociation[].class));
	}

	public static String toJsonThemeAssociations(final Collection<IThemeAssociation> themeAssociations) {
		return DEFAULT_GSON.toJson(themeAssociations);
	}

	public static Set<MarkerConfig> loadMarkerConfigs() {
		final var json = TMUIPlugin.getPreference(PreferenceConstants.TASK_TAGS, null);
		Set<MarkerConfig> result = null;
		if (json != null)
			try {
				result = loadMarkerConfigs(json);
			} catch (final JsonSyntaxException ex) {
				TMUIPlugin.logError(ex);
			}
		return result == null ? MarkerConfig.getDefaults() : result;
	}

	public static Set<MarkerConfig> loadMarkerConfigs(final String json) {
		return castNonNull(DEFAULT_GSON.fromJson(json, new TypeToken<Set<MarkerConfig>>() {
		}.getType()));
	}

	public static String toJsonMarkerConfigs(final Set<MarkerConfig> markerConfigs) {
		return DEFAULT_GSON.toJson(markerConfigs);
	}

	public static void saveMarkerConfigs(final Set<MarkerConfig> markerConfigs) throws BackingStoreException {
		final var prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		prefs.put(PreferenceConstants.TASK_TAGS, toJsonMarkerConfigs(markerConfigs));
		prefs.flush();
	}

	public static boolean isTMTokenHoverEnabled() {
		return TMUIPlugin.getPreference(PreferenceConstants.TMTOKEN_HOVER_ENABLED, false);
	}

	public static void saveTMTokenHoverEnabled(boolean isEnabled) throws BackingStoreException {
		final var prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		prefs.putBoolean(PreferenceConstants.TMTOKEN_HOVER_ENABLED, isEnabled);
		prefs.flush();
	}

	private PreferenceHelper() {
	}
}
