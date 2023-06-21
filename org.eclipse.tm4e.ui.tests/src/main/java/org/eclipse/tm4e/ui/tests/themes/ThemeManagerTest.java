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
package org.eclipse.tm4e.ui.tests.themes;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.Theme;
import org.eclipse.tm4e.ui.themes.ThemeIdConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for theme manager.
 *
 */
class ThemeManagerTest implements ThemeIdConstants {

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
	void themes() {
		// All themes
		final ITheme[] themes = manager.getThemes();
		assertNotNull(themes);
		assertEquals(4, themes.length);
	}

	@Test
	void defaultThemeAssociation() {
		// Default theme
		final ITheme theme = manager.getDefaultTheme();
		assertNotNull(theme);
		assertEquals(SolarizedLight, theme.getId());
	}

	@Test
	void darkThemes() {
		// All themes for Dark E4 CSS Theme
		final ITheme[] darkThemes = manager.getThemes(true);
		assertNotNull(darkThemes);
		assertEquals(2, darkThemes.length);
		assertEquals(Dark, darkThemes[0].getId());
		assertEquals(Monokai, darkThemes[1].getId());

		// All themes for Other E4 CSS Theme
		final ITheme[] otherThemes = manager.getThemes(false);
		assertNotNull(otherThemes);
		assertEquals(2, otherThemes.length);
		assertEquals(SolarizedLight, otherThemes[0].getId());
		assertEquals(Light, otherThemes[1].getId());
	}
}
