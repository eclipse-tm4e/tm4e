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
package org.eclipse.tm4e.ui.internal.themes;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceHelper;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.Theme;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;
import org.eclipse.tm4e.ui.utils.PreferenceUtils;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Theme manager singleton.
 */
public class ThemeManager extends AbstractThemeManager {

	// "themes" extension point
	private static final String EXTENSION_THEMES = "themes"; //$NON-NLS-1$

	// "theme" declaration
	private static final String THEME_ELT = "theme"; //$NON-NLS-1$

	// "themeAssociation" declaration
	private static final String THEME_ASSOCIATION_ELT = "themeAssociation"; //$NON-NLS-1$

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
		ThemeManager manager = new ThemeManager();
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
		IConfigurationElement[] cf = Platform.getExtensionRegistry().getConfigurationElementsFor(TMUIPlugin.PLUGIN_ID,
				EXTENSION_THEMES);
		for (IConfigurationElement ce : cf) {
			String name = ce.getName();
			if (THEME_ELT.equals(name)) {
				// theme
				Theme theme = new Theme(ce);
				super.registerTheme(theme);
			} else if (THEME_ASSOCIATION_ELT.equals(name)) {
				// themeAssociation
				super.registerThemeAssociation(new ThemeAssociation(ce));
			}
		}
	}

	/**
	 * Load TextMate Themes from preferences.
	 */
	private void loadThemesFromPreferences() {
		// Load Theme definitions from the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.ui.prefs"
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		String json = prefs.get(PreferenceConstants.THEMES, null);
		if (json != null) {
			ITheme[] themes = PreferenceHelper.loadThemes(json);
			for (ITheme theme : themes) {
				super.registerTheme(theme);
			}
		}

		json = prefs.get(PreferenceConstants.THEME_ASSOCIATIONS, null);
		if (json != null) {
			IThemeAssociation[] themeAssociations = PreferenceHelper.loadThemeAssociations(json);
			for (IThemeAssociation association : themeAssociations) {
				super.registerThemeAssociation(association);
			}
		}
	}

	@Override
	public void save() throws BackingStoreException {
		// Save Themes in the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.ui.prefs"
		String json = PreferenceHelper.toJsonThemes(
				Arrays.stream(getThemes()).filter(t -> t.getPluginId() == null).collect(Collectors.toList()));
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(TMUIPlugin.PLUGIN_ID);
		prefs.put(PreferenceConstants.THEMES, json);

		// Save Theme associations in the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.ui.prefs"
		json = PreferenceHelper.toJsonThemeAssociations(Arrays.stream(getAllThemeAssociations())
				.filter(t -> t.getPluginId() == null).collect(Collectors.toList()));
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
	public void addPreferenceChangeListener(IPreferenceChangeListener themeChangeListener) {
		// Observe change of Eclipse E4 Theme
		IEclipsePreferences preferences = PreferenceUtils.getE4PreferenceStore();
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
	public void removePreferenceChangeListener(IPreferenceChangeListener themeChangeListener) {
		// Observe change of Eclipse E4 Theme
		IEclipsePreferences preferences = PreferenceUtils.getE4PreferenceStore();
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
