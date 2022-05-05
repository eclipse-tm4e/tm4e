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
package org.eclipse.tm4e.ui.internal.themes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Working copy of theme manager.
 *
 */
public final class WorkingCopyThemeManager extends AbstractThemeManager {

	private final IThemeManager manager;

	private List<ITheme> themeAdded;
	private List<ITheme> themeRemoved;

	private List<IThemeAssociation> associationAdded;
	private List<IThemeAssociation> associationRemoved;

	public WorkingCopyThemeManager(final IThemeManager manager) {
		this.manager = manager;
		load();
	}

	private void load() {
		// Copy themes
		final ITheme[] themes = manager.getThemes();
		for (final ITheme theme : themes) {
			super.registerTheme(theme);
		}
		// Copy theme associations
		final IThemeAssociation[] associations = manager.getAllThemeAssociations();
		for (final IThemeAssociation association : associations) {
			super.registerThemeAssociation(association);
		}
	}

	@Override
	public void registerTheme(final ITheme theme) {
		super.registerTheme(theme);
		if (themeAdded == null) {
			themeAdded = new ArrayList<>();
		}
		themeAdded.add(theme);
	}

	@Override
	public void unregisterTheme(final ITheme theme) {
		super.unregisterTheme(theme);
		if (themeAdded != null && themeAdded.contains(theme)) {
			themeAdded.remove(theme);
		} else {
			if (themeRemoved == null) {
				themeRemoved = new ArrayList<>();
			}
			themeRemoved.add(theme);
		}
	}

	@Override
	public void registerThemeAssociation(final IThemeAssociation association) {
		super.registerThemeAssociation(association);
		if (associationAdded == null) {
			associationAdded = new ArrayList<>();
		}
		associationAdded.add(association);
	}

	@Override
	public void unregisterThemeAssociation(final IThemeAssociation association) {
		super.unregisterThemeAssociation(association);
		if (associationAdded != null && associationAdded.contains(association)) {
			associationAdded.remove(association);
		} else {
			if (associationRemoved == null) {
				associationRemoved = new ArrayList<>();
			}
			associationRemoved.add(association);
		}
	}

	@Override
	public void save() throws BackingStoreException {
		if (themeAdded != null) {
			for (final ITheme theme : themeAdded) {
				manager.registerTheme(theme);
			}
		}
		if (themeRemoved != null) {
			for (final ITheme theme : themeRemoved) {
				manager.unregisterTheme(theme);
			}
		}
		if (associationAdded != null) {
			for (final IThemeAssociation association : associationAdded) {
				manager.registerThemeAssociation(association);
			}
		}
		if (associationRemoved != null) {
			for (final IThemeAssociation association : associationRemoved) {
				manager.unregisterThemeAssociation(association);
			}
		}
		if (themeAdded != null || themeRemoved != null || associationAdded != null || associationRemoved != null) {
			manager.save();
		}
	}

}
