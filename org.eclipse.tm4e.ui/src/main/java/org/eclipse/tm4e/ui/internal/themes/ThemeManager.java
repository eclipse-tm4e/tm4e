/**
 * Copyright (c) 2015, 2021 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.themes;

import java.util.Arrays;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceHelper;
import org.eclipse.tm4e.ui.internal.utils.PreferenceUtils;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.Theme;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Theme manager singleton.
 */
public final class ThemeManager extends AbstractThemeManager {

	// "themes" extension point
	private static final String EXTENSION_THEMES = "themes"; //$NON-NLS-1$

	// "theme" declaration
	private static final String THEME_ELT = "theme"; //$NON-NLS-1$

	// "themeAssociation" declaration
	private static final String THEME_ASSOCIATION_ELT = "themeAssociation"; //$NON-NLS-1$

	@Nullable
	private static ThemeManager INSTANCE;

	public static ThemeManager getInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		INSTANCE = createInstance();
		return INSTANCE;
	}

	private static synchronized ThemeManager createInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		final var manager = new ThemeManager();
		manager.load();
		return manager;
	}

	private ThemeManager() {
	}

	private void load() {
		loadThemesFromExtensionPoints();
		loadThemesFromPreferences();
	}

	/**
	 * Load TextMate Themes from extension point.
	 */
	private void loadThemesFromExtensionPoints() {
		final var config = Platform.getExtensionRegistry().getConfigurationElementsFor(TMUIPlugin.PLUGIN_ID,
				EXTENSION_THEMES);
		for (final var configElement : config) {
			final String name = configElement.getName();
			switch (name) {
				case THEME_ELT:
					super.registerTheme(new Theme(configElement));
					break;
				case THEME_ASSOCIATION_ELT:
					super.registerThemeAssociation(new ThemeAssociation(configElement));
					break;
			}
		}
	}

	/**
	 * Load TextMate Themes from preferences.
	 */
	private void loadThemesFromPreferences() {
		// Load Theme definitions from the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.ui.prefs"
		final var prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		String json = prefs.get(PreferenceConstants.THEMES, null);
		if (json != null) {
			for (final var element : new Gson().fromJson(json, JsonObject[].class)) {
				final String name = element.get("id").getAsString();
				super.registerTheme(new Theme(name, element.get("path").getAsString(), name,
						element.get("dark").getAsBoolean(), false));
			}
		}

		json = prefs.get(PreferenceConstants.THEME_ASSOCIATIONS, null);
		if (json != null) {
			final var themeAssociations = PreferenceHelper.loadThemeAssociations(json);
			for (final IThemeAssociation association : themeAssociations) {
				super.registerThemeAssociation(association);
			}
		}
	}

	@Override
	public void save() throws BackingStoreException {
		final var prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		// Save Themes in the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.ui.prefs"
		prefs.put(PreferenceConstants.THEMES, Arrays.stream(getThemes()) //
				.filter(t -> t.getPluginId() == null) //
				.map(theme -> {
					final var json = new JsonObject();
					json.addProperty("id", theme.getId());
					json.addProperty("path", theme.getPath());
					json.addProperty("dark", theme.isDark());
					return json;
				}).collect(JsonArray::new, JsonArray::add, (r, r1) -> {
				})
				.toString());

		// Save Theme associations in the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.ui.prefs"
		final String json = PreferenceHelper.toJsonThemeAssociations(Arrays.stream(getAllThemeAssociations())
				.filter(t -> t.getPluginId() == null).toList());
		prefs.put(PreferenceConstants.THEME_ASSOCIATIONS, json);

		// Save preferences
		prefs.flush();
	}

	/**
	 * Add preference change listener to observe changed of Eclipse E4 Theme and
	 * TextMate theme association with grammar.
	 *
	 * @param themeChangeListener
	 */
	public void addPreferenceChangeListener(final IPreferenceChangeListener themeChangeListener) {
		// Observe change of Eclipse E4 Theme
		var preferences = PreferenceUtils.getE4PreferenceStore();
		if (preferences != null) {
			preferences.addPreferenceChangeListener(themeChangeListener);
		}
		// Observe change of TextMate Theme association
		preferences = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		if (preferences != null) {
			preferences.addPreferenceChangeListener(themeChangeListener);
		}
	}

	/**
	 * Remove preference change listener to observe changed of Eclipse E4 Theme and
	 * TextMate theme association with grammar.
	 *
	 * @param themeChangeListener
	 */
	public void removePreferenceChangeListener(final IPreferenceChangeListener themeChangeListener) {
		// Observe change of Eclipse E4 Theme
		var preferences = PreferenceUtils.getE4PreferenceStore();
		if (preferences != null) {
			preferences.removePreferenceChangeListener(themeChangeListener);
		}
		// Observe change of TextMate Theme association
		preferences = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		if (preferences != null) {
			preferences.removePreferenceChangeListener(themeChangeListener);
		}
	}
}
