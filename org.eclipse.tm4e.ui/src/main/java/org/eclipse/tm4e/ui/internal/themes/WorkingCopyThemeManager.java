/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Sebastian Thomschke (Vegard IT) - code cleanup, refactoring, simplification
 */
package org.eclipse.tm4e.ui.internal.themes;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Working copy of theme manager.
 */
final class WorkingCopyThemeManager extends AbstractThemeManager implements IThemeManager.EditSession {

	private final ThemeManager manager;
	private final Set<ITheme> themesAdded = new HashSet<>();
	private final Set<ITheme> themesRemoved = new HashSet<>();
	private final Set<IThemeAssociation> associationsAdded = new HashSet<>();
	private final Set<IThemeAssociation> associationsRemoved = new HashSet<>();

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

		themesAdded.clear();
		themesRemoved.clear();
		associationsAdded.clear();
		associationsRemoved.clear();
		defaultDarkThemeId = manager.defaultDarkThemeId;
		defaultLightThemeId = manager.defaultLightThemeId;
		isDirty = false;
	}

	@Override
	public void registerTheme(final ITheme theme) {
		super.registerTheme(theme);
		themesRemoved.remove(theme);
		themesAdded.add(theme);
		isDirty = true;
	}

	@Override
	public void unregisterTheme(final ITheme theme) {
		super.unregisterTheme(theme);
		themesAdded.remove(theme);
		themesRemoved.add(theme);
		isDirty = true;
	}

	@Override
	public void registerThemeAssociation(final IThemeAssociation association) {
		super.registerThemeAssociation(association);
		associationsRemoved.remove(association);
		associationsAdded.add(association);
		isDirty = true;
	}

	@Override
	public void unregisterThemeAssociation(final IThemeAssociation association) {
		super.unregisterThemeAssociation(association);
		associationsAdded.remove(association);
		associationsRemoved.add(association);
		isDirty = true;
	}

	@Override
	public void setDefaultTheme(final String themeId, final boolean dark) {
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

		themesRemoved.forEach(manager::unregisterTheme);
		themesAdded.forEach(manager::registerTheme);

		associationsRemoved.forEach(manager::unregisterThemeAssociation);
		associationsAdded.forEach(manager::registerThemeAssociation);

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
