/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.themes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;

/**
 * TextMate theme manager implementation.
 *
 */
public abstract class AbstractThemeManager implements IThemeManager {

	// Theme for E4 CSS Engine
	public static final String E4_THEME_ID = "themeid"; //$NON-NLS-1$

	private static final String E4_CSS_THEME_PREFERENCE_ID = "org.eclipse.e4.ui.css.swt.theme"; //$NON-NLS-1$
	private static final String E4_DARK = "org.eclipse.e4.ui.css.theme.e4_dark";
	private static final String DEVSTYLE_DARK = "com.genuitec.eclipse.themes.dark";

	private final Map<String /* theme id */ , ITheme> themes;
	private final ThemeAssociationRegistry themeAssociationRegistry;

	public AbstractThemeManager() {
		this.themes = new LinkedHashMap<>();
		this.themeAssociationRegistry = new ThemeAssociationRegistry();
	}

	@Override
	public void registerTheme(ITheme theme) {
		themes.put(theme.getId(), theme);
	}

	@Override
	public void unregisterTheme(ITheme theme) {
		themes.remove(theme.getId());
	}

	@Override
	public ITheme getThemeById(String themeId) {
		return themes.get(themeId);
	}

	@Override
	public ITheme[] getThemes() {
		Collection<ITheme> themes = this.themes.values();
		return themes.toArray(new ITheme[themes.size()]);
	}

	@Override
	public ITheme getDefaultTheme() {
		boolean dark = isDarkEclipseTheme();
		return getDefaultTheme(dark);
	}

	public ITheme getDefaultTheme(boolean dark) {
		for (ITheme theme : this.themes.values()) {
			if (theme.isDark() == dark && theme.isDefault()) {
				return theme;
			}
		}
		return null;
	}

	@Override
	public ITheme[] getThemes(boolean dark) {
		Collection<ITheme> themes = this.themes.values();
		return themes.stream().filter(theme -> {
			return theme.isDark() == dark;
		}).collect(Collectors.toList()).toArray(new ITheme[0]);
	}

	@Override
	public String getPreferenceE4CSSThemeId() {
		IEclipsePreferences preferences = getPreferenceE4CSSTheme();
		return preferences != null ? preferences.get(E4_THEME_ID, null) : null;
	}

	protected boolean isDarkDevStyleTheme(String themeId) {
		return themeId.contains(DEVSTYLE_DARK);
	}

	@Override
	public boolean isDarkEclipseTheme() {
		return isDarkEclipseTheme(getPreferenceE4CSSThemeId());
	}

	@Override
	public boolean isDarkEclipseTheme(String eclipseThemeId) {
		return eclipseThemeId.contains(E4_DARK) || isDarkDevStyleTheme(eclipseThemeId);
	}

	protected IEclipsePreferences getPreferenceE4CSSTheme() {
		return InstanceScope.INSTANCE.getNode(E4_CSS_THEME_PREFERENCE_ID);
	}

	@Override
	public ITheme getThemeForScope(String scopeName, boolean dark) {
		IThemeAssociation association = themeAssociationRegistry.getThemeAssociationFor(scopeName, dark);
		if (association != null) {
			String themeId = association.getThemeId();
			return getThemeById(themeId);
		}
		return getDefaultTheme(dark);
	}

	@Override
	public ITheme getThemeForScope(String scopeName) {
		return getThemeForScope(scopeName, isDarkEclipseTheme());
	}

	@Override
	public IThemeAssociation[] getThemeAssociationsForScope(String scopeName) {
		List<IThemeAssociation> associations = new ArrayList<>();
		IThemeAssociation light = themeAssociationRegistry.getThemeAssociationFor(scopeName, false);
		if (light == null) {
			light = new ThemeAssociation(getDefaultTheme(false).getId(), scopeName, false);
		}
		associations.add(light);
		IThemeAssociation dark = themeAssociationRegistry.getThemeAssociationFor(scopeName, true);
		if (dark == null) {
			dark = new ThemeAssociation(getDefaultTheme(true).getId(), scopeName, true);
		}
		associations.add(dark);
		return associations.toArray(new IThemeAssociation[associations.size()]);
	}

	@Override
	public void registerThemeAssociation(IThemeAssociation association) {
		themeAssociationRegistry.register(association);
	}

	@Override
	public void unregisterThemeAssociation(IThemeAssociation association) {
		themeAssociationRegistry.unregister(association);
	}

	@Override
	public IThemeAssociation[] getAllThemeAssociations() {
		List<IThemeAssociation> associations = themeAssociationRegistry.getThemeAssociations();
		return associations.toArray(new IThemeAssociation[associations.size()]);
	}

}
