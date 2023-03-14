/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.preferences;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationDefinition;

final class LanguageConfigurationLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Nullable
	@Override
	public Image getColumnImage(@Nullable final Object element, final int columnIndex) {
		return null;
	}

	@Nullable
	@Override
	public String getText(@Nullable final Object element) {
		return getColumnText(element, 0);
	}

	@Nullable
	@Override
	public String getColumnText(@Nullable final Object element, final int columnIndex) {
		if (element == null)
			return null;

		final var definition = (ILanguageConfigurationDefinition) element;

		return switch (columnIndex) {
			case 0 -> definition.getContentType().getName();
			case 1 -> definition.getContentType().getId();
			case 2 -> definition.getPluginId();
			case 3 -> definition.getPath();
			default -> ""; //$NON-NLS-1$
		};
	}
}
