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

import java.util.Collections;
import java.util.List;

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

public class CharacterPairsTableWidget extends TableViewer {

	public CharacterPairsTableWidget(Table table) {
		super(table);
		setContentProvider(new CharacterPairContentProvider());
		setLabelProvider(new CharacterPairLabelProvider());

		GC gc = new GC(table.getShell());
		gc.setFont(JFaceResources.getDialogFont());
		TableColumnLayout columnLayout = new TableColumnLayout();

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(LanguageConfigurationMessages.CharacterPairsTableWidget_start);
		int minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.CharacterPairsTableWidget_start);
		columnLayout.setColumnData(column1, new ColumnWeightData(2, minWidth, true));

		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(LanguageConfigurationMessages.CharacterPairsTableWidget_end);
		minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.CharacterPairsTableWidget_end);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));

		gc.dispose();
	}

	protected int computeMinimumColumnWidth(GC gc, String string) {
		return gc.stringExtent(string).x + 10;
	}

	protected static class CharacterPairContentProvider implements IStructuredContentProvider {

		private List<CharacterPair> characterPairList;

		@Override
		public Object[] getElements(Object input) {
			return characterPairList.toArray(new CharacterPair[characterPairList.size()]);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null) {
				characterPairList = Collections.EMPTY_LIST;
			} else {
				characterPairList = (List<CharacterPair>) newInput;
			}
		}

		@Override
		public void dispose() {
			characterPairList = null;
		}
	}

	protected static class CharacterPairLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			CharacterPair pair = (CharacterPair) element;

			switch (columnIndex) {
			case 0:
				return pair.getKey();
			case 1:
				return pair.getValue();
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}

}
