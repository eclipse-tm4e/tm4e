package org.eclipse.tm4e.ui.utils;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;

/**
 * Preference store utilities
 *
 */
public class PreferenceUtils {
	private static final String E4_CSS_PREFERENCE_NAME = "org.eclipse.e4.ui.css.swt.theme";

	private PreferenceUtils() {
	}

	/**
	 * Get e4 preference store
	 * 
	 * @return preference store
	 */
	public static IEclipsePreferences getE4PreferenceStore() {
		return InstanceScope.INSTANCE.getNode(E4_CSS_PREFERENCE_NAME);
	}

	/**
	 * Get Id of the current eclipse theme
	 * 
	 * @return themeIf of the current eclipse theme
	 */
	public static String getPreferenceE4CSSThemeId() {
		IEclipsePreferences preferences = getE4PreferenceStore();
		return preferences != null ? preferences.get(PreferenceConstants.E4_THEME_ID, null) : null;
	}

}
