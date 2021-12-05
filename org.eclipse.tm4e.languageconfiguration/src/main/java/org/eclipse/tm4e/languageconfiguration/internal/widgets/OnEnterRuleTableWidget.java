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
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterRule;

public class OnEnterRuleTableWidget extends TableViewer {

	public OnEnterRuleTableWidget(Table table) {
		super(table);
		setContentProvider(new OnEnterRuleContentProvider());
		setLabelProvider(new OnEnterRuleLabelProvider());

		GC gc = new GC(table.getShell());
		gc.setFont(JFaceResources.getDialogFont());
		TableColumnLayout columnLayout = new TableColumnLayout();

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(LanguageConfigurationMessages.OnEnterRuleTableWidget_beforeText);
		int minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.OnEnterRuleTableWidget_beforeText);
		columnLayout.setColumnData(column1, new ColumnWeightData(2, minWidth, true));

		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(LanguageConfigurationMessages.OnEnterRuleTableWidget_afterText);
		minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.OnEnterRuleTableWidget_afterText);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));

		TableColumn column3 = new TableColumn(table, SWT.NONE);
		column3.setText(LanguageConfigurationMessages.OnEnterRuleTableWidget_indentAction);
		minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.OnEnterRuleTableWidget_indentAction);
		columnLayout.setColumnData(column3, new ColumnWeightData(1, minWidth, true));

		TableColumn column4 = new TableColumn(table, SWT.NONE);
		column4.setText(LanguageConfigurationMessages.OnEnterRuleTableWidget_appendText);
		minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.OnEnterRuleTableWidget_appendText);
		columnLayout.setColumnData(column4, new ColumnWeightData(1, minWidth, true));

		TableColumn column5 = new TableColumn(table, SWT.NONE);
		column5.setText(LanguageConfigurationMessages.OnEnterRuleTableWidget_removeText);
		minWidth = computeMinimumColumnWidth(gc, LanguageConfigurationMessages.OnEnterRuleTableWidget_removeText);
		columnLayout.setColumnData(column5, new ColumnWeightData(1, minWidth, true));
	}

	protected int computeMinimumColumnWidth(GC gc, String string) {
		return gc.stringExtent(string).x + 10;
	}

	protected static class OnEnterRuleContentProvider implements IStructuredContentProvider {

		private List<OnEnterRule> onEnterRulesList;

		@Override
		public Object[] getElements(Object input) {
			return onEnterRulesList.toArray(new OnEnterRule[onEnterRulesList.size()]);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null) {
				onEnterRulesList = Collections.EMPTY_LIST;
			} else {
				onEnterRulesList = (List<OnEnterRule>) newInput;
			}
		}

		@Override
		public void dispose() {
			onEnterRulesList = null;
		}
	}

	protected static class OnEnterRuleLabelProvider extends LabelProvider implements ITableLabelProvider {

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
			OnEnterRule rule = (OnEnterRule) element;
			EnterAction action = rule.getAction();

			switch (columnIndex) {
			case 0:
				return rule.getBeforeText().pattern();
			case 1:
				return rule.getAfterText() == null ? "" : rule.getAfterText().pattern(); //$NON-NLS-1$
			case 2:
				return action.getIndentAction().toString();
			case 3:
				return action.getAppendText() == null ? "" : action.getAppendText(); //$NON-NLS-1$
			case 4:
				return action.getRemoveText() == null ? "" : action.getRemoveText().toString(); //$NON-NLS-1$
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}
}
