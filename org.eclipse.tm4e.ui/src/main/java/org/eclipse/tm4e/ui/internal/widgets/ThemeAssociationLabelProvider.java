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
package org.eclipse.tm4e.ui.internal.widgets;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;

/**
 * Label provider for TextMate theme association.
 */
public final class ThemeAssociationLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(final Object element, final int columnIndex) {
		return null;
	}

	@Override
	public String getText(final Object element) {
		return getColumnText(element, 0);
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex) {
		final IThemeAssociation association = (IThemeAssociation) element;
		switch (columnIndex) {
		case 0:
			final ITheme theme = getTheme(association);
			final String themeName = theme != null ? theme.getName() : association.getThemeId();
			if (association.isWhenDark()) {
				return NLS.bind(TMUIMessages.ThemeAssociationLabelProvider_dark, themeName);
			}
			return NLS.bind(TMUIMessages.ThemeAssociationLabelProvider_light, themeName);
		default:
			return ""; //$NON-NLS-1$
		}
	}

	private ITheme getTheme(final IThemeAssociation association) {
		final String themeId = association.getThemeId();
		final IThemeManager themeManager = TMUIPlugin.getThemeManager();
		return themeManager.getThemeById(themeId);
	}

}