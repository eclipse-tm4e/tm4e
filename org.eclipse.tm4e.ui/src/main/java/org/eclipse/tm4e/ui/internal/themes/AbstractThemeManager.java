/**
 * Copyright (c) 2015, 2021 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.themes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.ui.internal.utils.PreferenceUtils;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;

/**
 * TextMate theme manager implementation.
 */
public abstract class AbstractThemeManager implements IThemeManager {

	protected final Map<String /* theme id */, ITheme> themes = new LinkedHashMap<>();
	protected final Map<@Nullable String, @Nullable IThemeAssociation> darkThemeAssociations = new HashMap<>();
	protected final Map<@Nullable String, @Nullable IThemeAssociation> lightThemeAssociations = new HashMap<>();
	protected @Nullable String defaultDarkThemeId;
	protected @Nullable String defaultLightThemeId;

	protected void registerTheme(final ITheme theme) {
		themes.put(theme.getId(), theme);
	}

	protected void unregisterTheme(final ITheme theme) {
		themes.remove(theme.getId());
	}

	@Override
	public @Nullable ITheme getThemeById(final String themeId) {
		return themes.get(themeId);
	}

	@Override
	public ITheme[] getThemes() {
		return themes.values().toArray(ITheme[]::new);
	}

	@Override
	public ITheme getDefaultTheme() {
		return getDefaultTheme(PreferenceUtils.isDarkEclipseTheme());
	}

	@Override
	public ITheme getDefaultTheme(final boolean dark) {
		final var defaultThemeId = dark ? defaultDarkThemeId : defaultLightThemeId;
		final var defaultTheme = defaultThemeId == null ? null : themes.get(defaultThemeId);
		if (defaultTheme != null) {
			return defaultTheme;
		}

		for (final ITheme theme : themes.values()) {
			if (theme.isDark() == dark && theme.isDefault()) {
				return theme;
			}
		}
		throw new IllegalStateException("Should never be reached");
	}

	protected void setDefaultTheme(final String themeId, final boolean dark) {
		if (dark)
			defaultDarkThemeId = themeId;
		else
			defaultLightThemeId = themeId;
	}

	@Override
	public ITheme[] getThemes(final boolean dark) {
		return themes.values().stream().filter(theme -> theme.isDark() == dark).toArray(ITheme[]::new);
	}

	@Override
	public ITheme getThemeForScope(final String scopeName) {
		return getThemeForScope(scopeName, PreferenceUtils.isDarkEclipseTheme());
	}

	@Override
	public ITheme getThemeForScope(String scopeName, final boolean dark) {
		scopeName = ITMScope.parse(scopeName).getName();

		final IThemeAssociation association = dark
				? darkThemeAssociations.get(scopeName)
				: lightThemeAssociations.get(scopeName);
		if (association != null) {
			final String themeId = association.getThemeId();
			final var theme = getThemeById(themeId);
			if (theme != null) {
				return theme;
			}
		}
		return getDefaultTheme(dark);
	}

	@Override
	public ITheme getThemeForScope(final String scopeName, final RGB background) {
		return getThemeForScope(scopeName, 0.299 * background.red
				+ 0.587 * background.green
				+ 0.114 * background.blue < 128);
	}

	@Override
	public IThemeAssociation[] getThemeAssociationsForScope(String scopeName) {
		scopeName = ITMScope.parse(scopeName).getName();

		final var associations = new ArrayList<IThemeAssociation>();
		IThemeAssociation light = lightThemeAssociations.get(scopeName);
		if (light == null) {
			light = new ThemeAssociation(getDefaultTheme(false).getId(), scopeName, false);
		}
		associations.add(light);
		IThemeAssociation dark = darkThemeAssociations.get(scopeName);
		if (dark == null) {
			dark = new ThemeAssociation(getDefaultTheme(true).getId(), scopeName, true);
		}
		associations.add(dark);
		return associations.toArray(IThemeAssociation[]::new);
	}

	protected void registerThemeAssociation(final IThemeAssociation association) {
		if (association.isWhenDark()) {
			darkThemeAssociations.put(association.getScopeName(), association);
		} else {
			lightThemeAssociations.put(association.getScopeName(), association);
		}
	}

	protected void unregisterThemeAssociation(final IThemeAssociation association) {
		if (association.isWhenDark()) {
			darkThemeAssociations.remove(association.getScopeName(), association);
		} else {
			lightThemeAssociations.remove(association.getScopeName(), association);
		}
	}

	@Override
	public IThemeAssociation[] getAllThemeAssociations() {
		return Stream.concat(darkThemeAssociations.values().stream(), lightThemeAssociations.values().stream())
				.toArray(IThemeAssociation[]::new);
	}
}
