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
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.ui.editors.text.EditorsUI;

public final class PreferenceUtils {

	/** see {@link org.eclipse.e4.ui.css.swt.internal.theme.ThemeEngine#THEMEID_KEY} */
	@SuppressWarnings({ "restriction", "javadoc" })
	public static final String E4_THEME_ID = "themeid";

	/** see {@link org.eclipse.e4.ui.css.swt.internal.theme.ThemeEngine#THEME_PLUGIN_ID} */
	@SuppressWarnings({ "restriction" })
	private static final String E4_CSS_PREFERENCE_NAME = "org.eclipse.e4.ui.css.swt.theme";

	private static @Nullable Boolean isDebugGenerateTest;
	private static @Nullable Boolean isDebugThrowError;

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
	 * Get e4 themes preferences store
	 *
	 * @return preferences store
	 */
	public static @Nullable IEclipsePreferences getE4ThemesPreferenceStore() {
		return InstanceScope.INSTANCE.getNode(E4_CSS_PREFERENCE_NAME);
	}

	/**
	 * Get editors preferences store
	 *
	 * @return preferences store
	 */
	public static @Nullable IEclipsePreferences getEditorsPreferenceStore() {
		return InstanceScope.INSTANCE.getNode(EditorsUI.PLUGIN_ID);
	}

	private PreferenceUtils() {
	}
}
