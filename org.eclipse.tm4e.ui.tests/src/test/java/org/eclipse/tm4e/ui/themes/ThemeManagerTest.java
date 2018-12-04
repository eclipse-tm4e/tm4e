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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for theme manager.	
 *
 */
public class ThemeManagerTest implements ThemeIdConstants {

	private IThemeManager manager;
	
	@Before
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
		Assert.assertNotNull(themes);
		Assert.assertEquals(4, themes.length);
	}

	@Test
	public void defaultThemeAssociation() {
		// Default theme
		ITheme theme = manager.getDefaultTheme();
		Assert.assertNotNull(theme);
		Assert.assertEquals(SolarizedLight, theme.getId());
	}

	@Test
	public void darkThemes() {
		// All themes for Dark E4 CSS Theme
		ITheme[] darkThemes = manager.getThemes(true);
		Assert.assertNotNull(darkThemes);
		Assert.assertEquals(2, darkThemes.length);
		Assert.assertEquals(Dark, darkThemes[0].getId());
		Assert.assertEquals(Monokai, darkThemes[1].getId());
		
		// All themes for Other E4 CSS Theme
		ITheme[] otherThemes = manager.getThemes(false);
		Assert.assertNotNull(otherThemes);
		Assert.assertEquals(2, otherThemes.length);
		Assert.assertEquals(SolarizedLight, otherThemes[0].getId());
		Assert.assertEquals(Light, otherThemes[1].getId());
	}
}
