/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.widgets;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages;
import org.eclipse.tm4e.languageconfiguration.internal.supports.AutoClosingPairConditional;

final class AutoClosingPairConditionalTableWidget extends CharacterPairsTableWidget {

	AutoClosingPairConditionalTableWidget(final Table table) {
		super(table);
		setLabelProvider(new AutoClosingPairConditionalLabelProvider());

		final GC gc = new GC(table.getShell());
		gc.setFont(JFaceResources.getDialogFont());
		final var columnLayout = new TableColumnLayout();

		final var column2 = new TableColumn(table, SWT.NONE);
		column2.setText(LanguageConfigurationMessages.AutoClosingPairConditionalTableWidget_notIn);
		final int minWidth = computeMinimumColumnWidth(gc,
				LanguageConfigurationMessages.AutoClosingPairConditionalTableWidget_notIn);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));

		gc.dispose();
	}

	private static final class AutoClosingPairConditionalLabelProvider extends CharacterPairLabelProvider {

		@Nullable
		@Override
		public String getColumnText(@Nullable final Object element, final int columnIndex) {
			if (columnIndex == 2) {
				if (element instanceof final AutoClosingPairConditional conditionalPair) {
					return String.join(", ", conditionalPair.getNotIn()); //$NON-NLS-1$
				}
				return ""; //$NON-NLS-1$
			}
			return super.getColumnText(element, columnIndex);
		}
	}

}
