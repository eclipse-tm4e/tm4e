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
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
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
		final var columnLayout = new TableColumnLayout();

		final var column1 = new TableColumn(table, SWT.NONE);
		column1.setText(OnEnterRuleTableWidget_beforeText);
		int minWidth = computeMinimumColumnWidth(gc, OnEnterRuleTableWidget_beforeText);
		columnLayout.setColumnData(column1, new ColumnWeightData(2, minWidth, true));

		final var column2 = new TableColumn(table, SWT.NONE);
		column2.setText(OnEnterRuleTableWidget_afterText);
		minWidth = computeMinimumColumnWidth(gc, OnEnterRuleTableWidget_afterText);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));

		final var column3 = new TableColumn(table, SWT.NONE);
		column3.setText(OnEnterRuleTableWidget_indentAction);
		minWidth = computeMinimumColumnWidth(gc, OnEnterRuleTableWidget_indentAction);
		columnLayout.setColumnData(column3, new ColumnWeightData(1, minWidth, true));

		final var column4 = new TableColumn(table, SWT.NONE);
		column4.setText(OnEnterRuleTableWidget_appendText);
		minWidth = computeMinimumColumnWidth(gc, OnEnterRuleTableWidget_appendText);
		columnLayout.setColumnData(column4, new ColumnWeightData(1, minWidth, true));

		final var column5 = new TableColumn(table, SWT.NONE);
		column5.setText(OnEnterRuleTableWidget_removeText);
		minWidth = computeMinimumColumnWidth(gc, OnEnterRuleTableWidget_removeText);
		columnLayout.setColumnData(column5, new ColumnWeightData(1, minWidth, true));

		gc.dispose();
	}

	private int computeMinimumColumnWidth(final GC gc, final String string) {
		return gc.stringExtent(string).x + 10;
	}

	private static final class OnEnterRuleContentProvider implements IStructuredContentProvider {

		private List<OnEnterRule> onEnterRulesList = Collections.emptyList();

		@Override
		public Object[] getElements(@Nullable final Object input) {
			assert onEnterRulesList != null;
			return onEnterRulesList.toArray(OnEnterRule[]::new);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(@Nullable final Viewer viewer, @Nullable final Object oldInput,
				@Nullable final Object newInput) {
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

		@Override
		public @Nullable String getColumnText(@Nullable final Object element, final int columnIndex) {
			if (element == null)
				return "";

			final OnEnterRule rule = (OnEnterRule) element;
			final EnterAction action = rule.action;

			return switch (columnIndex) {
			case 0 -> Optional.ofNullable(rule.beforeText).map(OnEnterRuleTableWidget::nonNull)
					.map(Pattern::pattern).orElse("");
			case 1 -> Optional.ofNullable(rule.afterText).map(OnEnterRuleTableWidget::nonNull)
					.map(Pattern::pattern).orElse("");
			case 2 -> action.indentAction.toString();
			case 3 -> Optional.ofNullable(action.appendText).orElse("");
			case 4 -> Optional.ofNullable(action.removeText).map(OnEnterRuleTableWidget::nonNull).map(Object::toString)
					.orElse("");
			default -> ""; //$NON-NLS-1$
			};
		}
	}

	private static <T> @NonNull T nonNull(final @Nullable T obj) {
		if (obj != null) {
			return obj;
		}
		throw new IllegalArgumentException("argument mustn't be null");
	}
}
