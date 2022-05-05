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
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm4e.registry.IGrammarDefinition;

/**
 * Label provider for grammar definition.
 */
public final class GrammarDefinitionLabelProvider extends LabelProvider implements ITableLabelProvider {

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
		final IGrammarDefinition definition = (IGrammarDefinition) element;

		switch (columnIndex) {
		case 0:
			return definition.getScopeName();
		case 1:
			return definition.getPath();
		case 2:
			return definition.getPluginId();
		default:
			return ""; //$NON-NLS-1$
		}
	}
}