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
package org.eclipse.tm4e.ui.internal.themes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Working copy of theme manager.
 */
final class WorkingCopyThemeManager extends AbstractThemeManager implements IThemeManager.EditSession {

	private final ThemeManager manager;
	private final List<ITheme> themesAdded = new ArrayList<>(2);
	private final List<ITheme> themesRemoved = new ArrayList<>(2);
	private final List<IThemeAssociation> associationsAdded = new ArrayList<>(2);
	private final List<IThemeAssociation> associationsRemoved = new ArrayList<>(2);

	private boolean isDefaultDarkThemeModified = false;
	private boolean isDefaultLightThemeModified = false;
	private boolean isDirty = false;

	WorkingCopyThemeManager(final ThemeManager manager) {
		this.manager = manager;
		reset();
	}

	@Override
	public void reset() {
		themes.clear();
		themes.putAll(manager.themes);
		darkThemeAssociations.clear();
		darkThemeAssociations.putAll(manager.darkThemeAssociations);
		lightThemeAssociations.clear();
		lightThemeAssociations.putAll(manager.lightThemeAssociations);
		defaultDarkThemeId = manager.defaultDarkThemeId;
		defaultLightThemeId = manager.defaultLightThemeId;
		themesAdded.clear();
		themesRemoved.clear();
		associationsAdded.clear();
		associationsRemoved.clear();
		isDirty = false;
	}

	@Override
	public void registerTheme(final ITheme theme) {
		super.registerTheme(theme);
		themesAdded.add(theme);
		isDirty = true;
	}

	@Override
	public void unregisterTheme(final ITheme theme) {
		super.unregisterTheme(theme);
		if (themesAdded.contains(theme)) {
			themesAdded.remove(theme);
		} else {
			themesRemoved.add(theme);
		}
		isDirty = true;
	}

	@Override
	public void registerThemeAssociation(final IThemeAssociation association) {
		super.registerThemeAssociation(association);
		associationsAdded.add(association);
		isDirty = true;
	}

	@Override
	public void unregisterThemeAssociation(final IThemeAssociation association) {
		super.unregisterThemeAssociation(association);
		if (associationsAdded.contains(association)) {
			associationsAdded.remove(association);
		} else {
			associationsRemoved.add(association);
		}
		isDirty = true;
	}

	@Override
	public void setDefaultTheme(String themeId, boolean dark) {
		super.setDefaultTheme(themeId, dark);
		if (dark)
			isDefaultDarkThemeModified = true;
		else
			isDefaultLightThemeModified = true;
		isDirty = true;
	}

	@Override
	public void save() throws BackingStoreException {
		if (!isDirty)
			return;

		for (final var theme : themesAdded) {
			manager.registerTheme(theme);
		}
		for (final var theme : themesRemoved) {
			manager.unregisterTheme(theme);
		}
		for (final var association : associationsAdded) {
			manager.registerThemeAssociation(association);
		}
		for (final var association : associationsRemoved) {
			manager.unregisterThemeAssociation(association);
		}

		// this if checks ensures that in case two separate working copies exist, e.g. for different prefs pages
		// the changes are not overwritten with old values if both copies are saved
		if (isDefaultDarkThemeModified)
			manager.defaultDarkThemeId = defaultDarkThemeId;

		if (isDefaultLightThemeModified)
			manager.defaultLightThemeId = defaultLightThemeId;

		manager.save();

		reset();
	}
}
