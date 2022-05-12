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
package org.eclipse.tm4e.languageconfiguration.internal.widgets;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;

class CharacterPairsTableWidget extends TableViewer {

	CharacterPairsTableWidget(final Table table) {
		super(table);
		setContentProvider(new CharacterPairContentProvider());
		setLabelProvider(new CharacterPairLabelProvider());

		final GC gc = new GC(table.getShell());
		gc.setFont(JFaceResources.getDialogFont());
		final var columnLayout = new TableColumnLayout();

		final var column1 = new TableColumn(table, SWT.NONE);
		column1.setText(LanguageConfigurationMessages.CharacterPairsTableWidget_start);
		int minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.CharacterPairsTableWidget_start);
		columnLayout.setColumnData(column1, new ColumnWeightData(2, minWidth, true));

		final var column2 = new TableColumn(table, SWT.NONE);
		column2.setText(LanguageConfigurationMessages.CharacterPairsTableWidget_end);
		minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.CharacterPairsTableWidget_end);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));

		gc.dispose();
	}

	protected int computeMinimumColumnWidth(final GC gc, final String string) {
		return gc.stringExtent(string).x + 10;
	}

	private static final class CharacterPairContentProvider implements IStructuredContentProvider {

		private List<CharacterPair> characterPairList = Collections.emptyList();

		@Override
		public Object[] getElements(@Nullable final Object input) {
			return characterPairList.toArray(CharacterPair[]::new);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(@Nullable final Viewer viewer, @Nullable final Object oldInput,
				@Nullable final Object newInput) {
			if (newInput == null) {
				characterPairList = Collections.emptyList();
			} else {
				characterPairList = (List<CharacterPair>) newInput;
			}
		}

		@Override
		public void dispose() {
			characterPairList = Collections.emptyList();
		}
	}

	protected static class CharacterPairLabelProvider extends LabelProvider implements ITableLabelProvider {

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

			final CharacterPair pair = (CharacterPair) element;

			return switch (columnIndex) {
			case 0 -> pair.getKey();
			case 1 -> pair.getValue();
			default -> ""; //$NON-NLS-1$
			};
		}
	}

}
