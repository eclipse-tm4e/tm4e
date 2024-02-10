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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceHelper;
import org.eclipse.tm4e.ui.internal.utils.PreferenceUtils;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
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

	/** "themes" extension point */
	private static final String EXTENSION_THEMES = "themes";

	/** "theme" declaration */
	private static final String THEME_ELT = "theme";

	/** "themeAssociation" declaration */
	private static final String THEME_ASSOCIATION_ELT = "themeAssociation";

	/** see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java */
	private static final class InstanceHolder {
		static final ThemeManager INSTANCE = new ThemeManager();
		static {
			INSTANCE.load();
		}
	}

	public static ThemeManager getInstance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Add preference change listener to observe changed of Eclipse E4 Theme and
	 * TextMate theme association with grammar.
	 *
	 * @param themeChangeListener
	 */
	public static void addPreferenceChangeListener(final IPreferenceChangeListener themeChangeListener) {
		// Observe change of Eclipse E4 Theme
		var prefs = PreferenceUtils.getE4ThemesPreferenceStore();
		if (prefs != null) {
			prefs.addPreferenceChangeListener(themeChangeListener);
		}
		// Observe change of TextMate Theme association
		prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		if (prefs != null) {
			prefs.addPreferenceChangeListener(themeChangeListener);
		}
	}

	/**
	 * Remove preference change listener to observe changed of Eclipse E4 Theme and
	 * TextMate theme association with grammar.
	 *
	 * @param themeChangeListener
	 */
	public static void removePreferenceChangeListener(final IPreferenceChangeListener themeChangeListener) {
		// Observe change of Eclipse E4 Theme
		var prefs = PreferenceUtils.getE4ThemesPreferenceStore();
		if (prefs != null) {
			prefs.removePreferenceChangeListener(themeChangeListener);
		}
		// Observe change of TextMate Theme association
		prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		if (prefs != null) {
			prefs.removePreferenceChangeListener(themeChangeListener);
		}
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
		final var config = Platform.getExtensionRegistry().getConfigurationElementsFor(TMUIPlugin.PLUGIN_ID, EXTENSION_THEMES);
		for (final IConfigurationElement elem : config) {
			switch (elem.getName()) {
				case THEME_ELT -> super.registerTheme(new Theme(elem));
				case THEME_ASSOCIATION_ELT -> super.registerThemeAssociation(new ThemeAssociation(elem));
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
			for (final var jsonElem : new Gson().fromJson(json, JsonObject[].class)) {
				final String name = jsonElem.get("id").getAsString();
				super.registerTheme(new Theme(name,
						jsonElem.get("path").getAsString(),
						name,
						jsonElem.get("dark").getAsBoolean()));
			}
		}

		json = prefs.get(PreferenceConstants.THEME_ASSOCIATIONS, null);
		if (json != null) {
			final var themeAssociations = PreferenceHelper.loadThemeAssociations(json);
			for (final IThemeAssociation association : themeAssociations) {
				super.registerThemeAssociation(association);
			}
		}

		defaultDarkThemeId = prefs.get(PreferenceConstants.DEFAULT_DARK_THEME, null);
		defaultLightThemeId = prefs.get(PreferenceConstants.DEFAULT_LIGHT_THEME, null);
	}

	void save() throws BackingStoreException {
		// save config to "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.ui.prefs"
		final var prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);

		// manually registered themes
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

		// manually modified theme associations
		final String json = PreferenceHelper.toJsonThemeAssociations(Arrays.stream(getAllThemeAssociations())
				.filter(t -> t.getPluginId() == null)
				.toList());
		prefs.put(PreferenceConstants.THEME_ASSOCIATIONS, json);

		// manually set default themes
		if (defaultDarkThemeId != null)
			prefs.put(PreferenceConstants.DEFAULT_DARK_THEME, defaultDarkThemeId);
		else
			prefs.remove(PreferenceConstants.DEFAULT_DARK_THEME);
		if (defaultLightThemeId != null)
			prefs.put(PreferenceConstants.DEFAULT_LIGHT_THEME, defaultLightThemeId);
		else
			prefs.remove(PreferenceConstants.DEFAULT_LIGHT_THEME);

		// save preferences
		prefs.flush();
	}

	@Override
	public IThemeManager.EditSession newEditSession() {
		return new WorkingCopyThemeManager(this);
	}
}
