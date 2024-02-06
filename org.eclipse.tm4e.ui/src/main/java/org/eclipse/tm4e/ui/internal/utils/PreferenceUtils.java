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
package org.eclipse.tm4e.ui.internal.utils;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;

public final class PreferenceUtils {

	private static final String E4_CSS_PREFERENCE_NAME = "org.eclipse.e4.ui.css.swt.theme";
	private static final String EDITORS_PREFERENCE_NAME = "org.eclipse.ui.editors";

	private static @Nullable Boolean isDebugGenerateTest;
	private static @Nullable Boolean isDebugThrowError;

	/**
	 * @return if Eclipse is currently using a dark UI theme
	 */
	public static boolean isDarkEclipseTheme() {
		final IEclipsePreferences prefs = getE4PreferenceStore();
		final String eclipseThemeId = prefs == null ? null : prefs.get(PreferenceConstants.E4_THEME_ID, null);
		return eclipseThemeId != null && eclipseThemeId.toLowerCase().contains("dark");
	}

	public static boolean isDebugGenerateTest() {
		var isDebugGenerateTest = PreferenceUtils.isDebugGenerateTest;
		if (isDebugGenerateTest == null)
			isDebugGenerateTest = PreferenceUtils.isDebugGenerateTest = Boolean.parseBoolean(
					Platform.getDebugOption(TMUIPlugin.PLUGIN_ID + "/debug/log/GenerateTest"));
		return isDebugGenerateTest;
	}

	public static boolean isDebugThrowError() {
		var isDebugThrowError = PreferenceUtils.isDebugThrowError;
		if (isDebugThrowError == null)
			isDebugThrowError = PreferenceUtils.isDebugThrowError = Boolean.parseBoolean(
					Platform.getDebugOption(TMUIPlugin.PLUGIN_ID + "/debug/log/ThrowError"));
		return isDebugThrowError;
	}

	/**
	 * Get e4 preferences store
	 *
	 * @return preferences store
	 */
	@Nullable
	public static IEclipsePreferences getE4PreferenceStore() {
		return InstanceScope.INSTANCE.getNode(E4_CSS_PREFERENCE_NAME);
	}

	/**
	 * Get editors preferences store
	 *
	 * @return preferences store
	 */
	@Nullable
	public static IEclipsePreferences getEditorsPreferenceStore() {
		return InstanceScope.INSTANCE.getNode(EDITORS_PREFERENCE_NAME);
	}

	/**
	 * Get TM4E preferences store
	 *
	 * @return preferences store
	 */
	@Nullable
	public static IPreferenceStore getTM4EPreferencesStore() {
		final var plugin = TMUIPlugin.getDefault();
		return plugin == null ? null : plugin.getPreferenceStore();
	}

	private PreferenceUtils() {
	}
}
