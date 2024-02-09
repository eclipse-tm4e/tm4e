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
package org.eclipse.tm4e.ui.themes;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.osgi.service.prefs.BackingStoreException;

/**
 * TextMate theme manager API.
 */
public interface IThemeManager {

	interface EditSession extends IThemeManager {
		/**
		 * resets this session to the current state of the singleton theme manager
		 */
		void reset();

		/**
		 * Register the given theme.
		 *
		 * @param theme to register.
		 */
		void registerTheme(ITheme theme);

		/**
		 * Unregister the given theme.
		 *
		 * @param theme to unregister.
		 */
		void unregisterTheme(ITheme theme);

		/**
		 * Sets the TextMate theme with the given themeId as default dark or light theme.
		 */
		void setDefaultTheme(String themeId, boolean dark);

		/**
		 * Register the given theme association.
		 *
		 * @param association to register.
		 */
		void registerThemeAssociation(IThemeAssociation association);

		/**
		 * Unregister the given theme association.
		 *
		 * @param association to unregister.
		 */
		void unregisterThemeAssociation(IThemeAssociation association);

		/**
		 * Applies changes to the singleton theme manager and persists them to disk
		 */
		void save() throws BackingStoreException;

		/**
		 * @throws UnsupportedOperationException
		 */
		@Override
		default EditSession newEditSession() {
			throw new UnsupportedOperationException();
		}
	}

	IThemeManager.EditSession newEditSession();

	/**
	 * @return the default light or dark TextMate theme depending on the selected Eclipse UI theme.
	 */
	ITheme getDefaultTheme();

	ITheme getDefaultTheme(boolean dark);

	/**
	 * Returns the {@link ITheme} by the theme id.
	 *
	 * @param themeId the theme id.
	 *
	 * @return the {@link ITheme} by the theme id.
	 */
	@Nullable
	ITheme getThemeById(String themeId);

	/**
	 * @return the list of all registered TextMate themes.
	 */
	ITheme[] getThemes();

	/**
	 * Returns the list of TextMate themes for the given eclipse theme id.
	 *
	 * @return the list of TextMate themes for the given eclipse theme id.
	 */
	ITheme[] getThemes(boolean dark);

	/**
	 * Returns the TextMate theme {@link ITheme} for the given TextMate grammar
	 * <code>scopeName</code> and E4 Theme <code>eclipseThemeId</code>.
	 *
	 * @param scopeName the TextMate grammar
	 *
	 * @return the TextMate theme {@link ITheme} for the given TextMate grammar
	 *         <code>scopeName</code> and E4 Theme <code>eclipseThemeId</code>.
	 */
	ITheme getThemeForScope(String scopeName, boolean dark);

	/**
	 * @return the theme that will fit best for the defined background color
	 */
	ITheme getThemeForScope(String scopeName, RGB background);

	/**
	 * Returns the TextMate theme {@link ITheme} for the given TextMate grammar
	 * <code>scopeName</code> and default E4 Theme.
	 *
	 * @param scopeName
	 *
	 * @return the TextMate theme {@link ITheme} for the given TextMate grammar
	 *         <code>scopeName</code> and default E4 Theme.
	 */
	ITheme getThemeForScope(String scopeName);

	/**
	 * Returns list of all theme associations.
	 *
	 * @return list of all theme associations.
	 */
	IThemeAssociation[] getAllThemeAssociations();

	/**
	 * Returns the theme associations for the given TextMate grammar
	 * <code>scopeName</code>.
	 *
	 * @param scopeName
	 *
	 * @return the theme associations for the given TextMate grammar
	 *         <code>scopeName</code>.
	 */
	IThemeAssociation[] getThemeAssociationsForScope(String scopeName);
}
