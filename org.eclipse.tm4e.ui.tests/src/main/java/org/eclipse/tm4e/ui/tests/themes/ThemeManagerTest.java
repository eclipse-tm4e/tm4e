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

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ThemeIdConstants;
import org.junit.jupiter.api.Test;

/**
 * Test for theme manager.
 */
class ThemeManagerTest implements ThemeIdConstants {

	private final IThemeManager manager = TMUIPlugin.getThemeManager();

	@Test
	void testDefaultThemeAssociation() {
		final ITheme theme = manager.getDefaultTheme();
		assertThat(theme.getId()).isEqualTo(SolarizedLight);
	}

	@Test
	void testAllThemesAreRegistered() {
		// All Dark themes
		final ITheme[] darkThemes = manager.getThemes(true);
		assertThat(darkThemes).anyMatch(t -> t.getId().equals(Dark));
		assertThat(darkThemes).anyMatch(t -> t.getId().equals(Monokai));

		// All themes for Other E4 CSS Theme
		final ITheme[] lightThemes = manager.getThemes(false);
		assertThat(lightThemes).anyMatch(t -> t.getId().equals(Light));
		assertThat(lightThemes).anyMatch(t -> t.getId().equals(EclipseLight));
		assertThat(lightThemes).anyMatch(t -> t.getId().equals(SolarizedLight));
		assertThat(lightThemes).anyMatch(t -> t.getId().equals(WtpXmlClassic));
	}
}
