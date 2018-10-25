package org.eclipse.tm4e.ui.themes;

/**
 * API to identify dark or light color scheme applied.
 * 
 * Motivation: get the opportunity to influence the applying TM themes behavior depending on the eclipse theme.
 * 
 * @author Tim Kovalyov
 *
 */
public interface IThemeBehaviour {

	/**
	 * Return is current eclipse theme dark or not
	 */
	boolean isDarkEclipseTheme();

	/**
	 * Return is given eclipse theme dark or not
	 * 
	 * @param eclipseThemeId
	 *            given eclipse theme
	 * @return boolean
	 */
	boolean isDarkEclipseTheme(String eclipseThemeId);

}
