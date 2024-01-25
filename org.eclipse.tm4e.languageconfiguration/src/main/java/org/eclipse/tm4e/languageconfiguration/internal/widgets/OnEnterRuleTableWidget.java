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

import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import java.util.Collections;
import java.util.LinkedHashMap;
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
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.OnEnterRule;

final class OnEnterRuleTableWidget extends TableViewer {

	OnEnterRuleTableWidget(final Table table) {
		super(table);
		setContentProvider(new OnEnterRuleContentProvider());
		setLabelProvider(new OnEnterRuleLabelProvider());

		final GC gc = new GC(table.getShell());
		gc.setFont(JFaceResources.getDialogFont());

		final var colDefs = new LinkedHashMap<String /* label */, Integer /* column weight */>();
		colDefs.put(OnEnterRuleTableWidget_beforeText, 2);
		colDefs.put(OnEnterRuleTableWidget_afterText, 2);
		colDefs.put(OnEnterRuleTableWidget_indentAction, 1);
		colDefs.put(OnEnterRuleTableWidget_appendText, 1);
		colDefs.put(OnEnterRuleTableWidget_removeText, 1);

		final var colLayout = new TableColumnLayout();
		for (final var colDef : colDefs.entrySet()) {
			final var col = new TableColumn(table, SWT.NONE);
			col.setText(colDef.getKey());
			final int minWidth = computeMinimumColumnWidth(gc, colDef.getKey());
			colLayout.setColumnData(col, new ColumnWeightData(colDef.getValue(), minWidth, true));
		}

		gc.dispose();
	}

	private int computeMinimumColumnWidth(final GC gc, final String string) {
		return gc.stringExtent(string).x + 10;
	}

	private static final class OnEnterRuleContentProvider implements IStructuredContentProvider {

		private List<OnEnterRule> onEnterRulesList = Collections.emptyList();

		@Override
		public Object[] getElements(final @Nullable Object input) {
			return onEnterRulesList.toArray(OnEnterRule[]::new);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(final @Nullable Viewer viewer, final @Nullable Object oldInput, final @Nullable Object newInput) {
			if (newInput == null) {
				onEnterRulesList = Collections.emptyList();
			} else {
				onEnterRulesList = (List<OnEnterRule>) newInput;
			}
		}

		@Override
		public void dispose() {
			onEnterRulesList = Collections.emptyList();
		}
	}

	private static final class OnEnterRuleLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public @Nullable Image getColumnImage(final @Nullable Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getText(final @Nullable Object element) {
			return getColumnText(element, 0);
		}

		@Override
		public String getColumnText(@Nullable final Object element, final int columnIndex) {
			if (element == null)
				return "";

			final OnEnterRule rule = (OnEnterRule) element;
			final EnterAction action = rule.action;

			return switch (columnIndex) {
				case 0 -> rule.beforeText.pattern();
				case 1 -> rule.afterText != null ? rule.afterText.pattern() : "";
				case 2 -> action.indentAction.toString();
				case 3 -> action.appendText != null ? action.appendText : "";
				case 4 -> action.removeText != null ? action.removeText.toString() : "";
				default -> "";
			};
		}
	}
}
