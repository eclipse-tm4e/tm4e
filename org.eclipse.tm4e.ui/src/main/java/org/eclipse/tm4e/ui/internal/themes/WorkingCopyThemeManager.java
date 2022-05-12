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

import org.eclipse.jdt.annotation.Nullable;
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

	@Nullable
	private List<ITheme> themeAdded;

	@Nullable
	private List<ITheme> themeRemoved;

	@Nullable
	private List<IThemeAssociation> associationAdded;

	@Nullable
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
		var themeAdded = this.themeAdded;
		if (themeAdded == null) {
			themeAdded = this.themeAdded = new ArrayList<>();
		}
		themeAdded.add(theme);
	}

	@Override
	public void unregisterTheme(final ITheme theme) {
		super.unregisterTheme(theme);
		final var themeAdded = this.themeAdded;
		if (themeAdded != null && themeAdded.contains(theme)) {
			themeAdded.remove(theme);
		} else {
			var themeRemoved = this.themeRemoved;
			if (themeRemoved == null) {
				themeRemoved = this.themeRemoved = new ArrayList<>();
			}
			themeRemoved.add(theme);
		}
	}

	@Override
	public void registerThemeAssociation(final IThemeAssociation association) {
		super.registerThemeAssociation(association);
		var associationAdded = this.associationAdded;
		if (associationAdded == null) {
			associationAdded = this.associationAdded = new ArrayList<>();
		}
		associationAdded.add(association);
	}

	@Override
	public void unregisterThemeAssociation(final IThemeAssociation association) {
		super.unregisterThemeAssociation(association);
		final var associationAdded = this.associationAdded;
		if (associationAdded != null && associationAdded.contains(association)) {
			associationAdded.remove(association);
		} else {
			var associationRemoved = this.associationRemoved;
			if (associationRemoved == null) {
				associationRemoved = this.associationRemoved = new ArrayList<>();
			}
			associationRemoved.add(association);
		}
	}

	@Override
	public void save() throws BackingStoreException {
		if (themeAdded != null) {
			for (final var theme : themeAdded) {
				manager.registerTheme(theme);
			}
		}
		if (themeRemoved != null) {
			for (final var theme : themeRemoved) {
				manager.unregisterTheme(theme);
			}
		}
		if (associationAdded != null) {
			for (final var association : associationAdded) {
				manager.registerThemeAssociation(association);
			}
		}
		if (associationRemoved != null) {
			for (final var association : associationRemoved) {
				manager.unregisterThemeAssociation(association);
			}
		}
		if (themeAdded != null || themeRemoved != null || associationAdded != null || associationRemoved != null) {
			manager.save();
		}
	}
}
