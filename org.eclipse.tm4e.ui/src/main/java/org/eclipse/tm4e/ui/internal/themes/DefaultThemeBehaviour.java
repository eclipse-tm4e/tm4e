package org.eclipse.tm4e.ui.internal.themes;

import org.eclipse.tm4e.ui.themes.IThemeBehaviour;
import org.eclipse.tm4e.ui.utils.PreferenceUtils;

public class DefaultThemeBehaviour implements IThemeBehaviour {

	private static final String E4_DARK = "org.eclipse.e4.ui.css.theme.e4_dark";

	@Override
	public boolean isDarkEclipseTheme() {
		return isDarkEclipseTheme(PreferenceUtils.getPreferenceE4CSSThemeId());
	}

	@Override
	public boolean isDarkEclipseTheme(String eclipseThemeId) {
		return E4_DARK.equals(eclipseThemeId);
	}

}
