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
package org.eclipse.tm4e.ui.themes;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for theme manager.	
 *
 */
public class ThemeManagerTest implements ThemeIdConstants {

	private IThemeManager manager;
	
	@BeforeEach
	public void init() {
		manager = new MockThemeManager();
		
		// Register theme
		manager.registerTheme(new Theme(SolarizedLight, "./themes/SolarizedLight.css", "SolarizedLight", false, true));
		manager.registerTheme(new Theme(Light, "./themes/Light.css", "Light", false, false));
		manager.registerTheme(new Theme(Dark, "./themes/Dark.css", "Dark", true, true));
		manager.registerTheme(new Theme(Monokai, "./themes/Monokai.css", "Monokai", true, false));
	}
		
	
	@Test
	public void themes() {
		// All themes
		ITheme[] themes = manager.getThemes();
		assertNotNull(themes);
		assertEquals(4, themes.length);
	}

	@Test
	public void defaultThemeAssociation() {
		// Default theme
		ITheme theme = manager.getDefaultTheme();
		assertNotNull(theme);
		assertEquals(SolarizedLight, theme.getId());
	}

	@Test
	public void darkThemes() {
		// All themes for Dark E4 CSS Theme
		ITheme[] darkThemes = manager.getThemes(true);
		assertNotNull(darkThemes);
		assertEquals(2, darkThemes.length);
		assertEquals(Dark, darkThemes[0].getId());
		assertEquals(Monokai, darkThemes[1].getId());
		
		// All themes for Other E4 CSS Theme
		ITheme[] otherThemes = manager.getThemes(false);
		assertNotNull(otherThemes);
		assertEquals(2, otherThemes.length);
		assertEquals(SolarizedLight, otherThemes[0].getId());
		assertEquals(Light, otherThemes[1].getId());
	}
}
