/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke - initial implementation
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

abstract class AbstractTableWidget<T> extends TableViewer {

	private final class CellLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public @Nullable Image getColumnImage(final @Nullable Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getText(final @Nullable Object element) {
			return getColumnText(element, 0);
		}

		@Override
		@SuppressWarnings("unchecked")
		public String getColumnText(@Nullable final Object element, final int columnIndex) {
			return element == null ? "" : AbstractTableWidget.this.getColumnText((T) element, columnIndex);
		}
	}

	private static final class RowContentProvider<T> implements IStructuredContentProvider {
		private List<T> items = Collections.emptyList();

		@Override
		public void dispose() {
			items = Collections.emptyList();
		}

		@Override
		public Object[] getElements(final @Nullable Object input) {
			return items.toArray(Object[]::new);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void inputChanged(final @Nullable Viewer viewer, final @Nullable Object oldInput, final @Nullable Object newInput) {
			items = newInput == null ? Collections.emptyList() : (List<T>) newInput;
		}
	}

	private final TableColumnLayout tableColumnLayout = new TableColumnLayout();

	protected AbstractTableWidget(Composite parent) {
		this(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	protected AbstractTableWidget(Composite parent, int style) {
		super(new Composite(parent, SWT.NONE), style);

		setContentProvider(new RowContentProvider<>());
		setLabelProvider(new CellLabelProvider());

		var table = getTable();
		var container = table.getParent();
		container.setLayout(new TableColumnLayout());
		if (parent.getLayout() instanceof GridLayout) {
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumns();
	}

	protected abstract void createColumns();

	protected void createColumn(String label, int columnWeight, int minColWidth, boolean resizable) {
		final var col = new TableColumn(getTable(), SWT.NONE);
		col.setText(label);
		final GC gc = new GC(getTable().getShell());
		try {
			gc.setFont(JFaceResources.getDialogFont());
			final int labelWidth = gc.stringExtent(label).x + 15;
			tableColumnLayout.setColumnData(col, new ColumnWeightData(columnWeight, Math.max(labelWidth, minColWidth), resizable));
		} finally {
			gc.dispose();
		}
	}

	protected abstract String getColumnText(final T element, final int columnIndex);
}
