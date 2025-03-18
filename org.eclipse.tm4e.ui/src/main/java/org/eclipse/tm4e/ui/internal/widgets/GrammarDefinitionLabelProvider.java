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
package org.eclipse.tm4e.ui.internal.widgets;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm4e.registry.IGrammarDefinition;

/**
 * Label provider for grammar definition.
 */
public final class GrammarDefinitionLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public @Nullable Image getColumnImage(final @Nullable Object element, final int columnIndex) {
		return null;
	}

	@Override
	public @Nullable String getText(final Object element) {
		return getColumnText(element, 0);
	}

	@Override
	public @Nullable String getColumnText(final @Nullable Object element, final int columnIndex) {
		if (element == null)
			return "";

		final var definition = (IGrammarDefinition) element;
		return switch (columnIndex) {
			case 0 -> definition.getScope().getName();
			case 1 -> definition.getPath();
			case 2 -> definition.getPluginId();
			default -> "";
		};
	}
}
