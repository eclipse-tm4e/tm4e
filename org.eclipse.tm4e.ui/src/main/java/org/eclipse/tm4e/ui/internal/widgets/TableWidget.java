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
package org.eclipse.tm4e.ui.internal.widgets;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm4e.ui.internal.utils.UI;

public abstract class TableWidget<T> extends TableViewer {

	private final class CellLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public @Nullable Image getColumnImage(final @Nullable Object element, final int columnIndex) {
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public @Nullable String getColumnText(final @Nullable Object element, final int columnIndex) {
			if (element == null)
				return null;
			final var text = TableWidget.this.getColumnText((T) element, columnIndex);
			return text == null ? null : text.toString();
		}

		@Override
		public @Nullable String getText(final @Nullable Object element) {
			return getColumnText(element, 0);
		}
	}

	private final TableColumnLayout tableColumnLayout = new TableColumnLayout();
	private final Set<TableColumn> autoResizeColumns = new HashSet<>();
	private final ColumnViewerComparator viewerComparator = new ColumnViewerComparator();

	protected TableWidget(Composite parent) {
		this(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
	}

	protected TableWidget(Composite parent, int style) {
		super(new Composite(parent, SWT.NONE), style);

		setContentProvider((IStructuredContentProvider) TableWidget.this::getElements);
		setLabelProvider(new CellLabelProvider());

		final var table = getTable();
		final var container = table.getParent();
		container.setLayout(tableColumnLayout);
		if (parent.getLayout() instanceof GridLayout) {
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumns();

		setComparator(viewerComparator);
		table.setSortDirection(viewerComparator.getDirection());
		setSortColumn(0);

		BidiUtils.applyTextDirection(getControl(), BidiUtils.BTD_DEFAULT);
	}

	protected void createAutoResizeColumn(final String label, final int... secondarySortColumns) {
		createColumn(label, 0, 0, secondarySortColumns);
		final var col = getTable().getColumn(getTable().getColumnCount() - 1);
		autoResizeColumns.add(col);
	}

	protected void createColumn(final String label, final int columnWeight, final int minColWidth,
			final int... secondarySortColumns) {
		final var col = new TableColumn(getTable(), SWT.NONE);
		col.setText(label);
		col.addSelectionListener(new ColumnSelectionAdapter(this, viewerComparator, secondarySortColumns));

		final GC gc = new GC(getTable().getShell());
		try {
			gc.setFont(JFaceResources.getDialogFont());
			final int labelWidth = gc.stringExtent(label).x + 15;
			tableColumnLayout.setColumnData(col, new ColumnWeightData(columnWeight, Math.max(labelWidth, minColWidth), true));
		} finally {
			gc.dispose();
		}
	}

	protected abstract void createColumns();

	/**
	 * @return an object whose #toString() method is called to get the element's text for the given column
	 */
	protected abstract @Nullable Object getColumnText(T element, int columnIndex);

	protected Object[] getElements(final @Nullable Object input) {
		if (input == null)
			return new Object[0];
		if (input instanceof Collection<?> coll)
			return coll.toArray(Object[]::new);
		return (Object[]) input;
	}

	@Override
	protected void inputChanged(final @Nullable Object input, final @Nullable Object oldInput) {
		super.inputChanged(input, oldInput);
		for (final TableColumn column : getTable().getColumns()) {
			if (autoResizeColumns.contains(column)) {
				column.pack();
			}
		}
	}

	public void onSelected(final Consumer<List<T>> consumer) {
		addSelectionChangedListener(e -> {
			if (e.getSelection() instanceof final IStructuredSelection sel && !sel.isEmpty()) {
				consumer.accept(sel.toList());
			}
		});
	}

	@SuppressWarnings("unchecked")
	public @Nullable T getFirstSelectedElement() {
		if (getSelection() instanceof final IStructuredSelection selection)
			return (T) selection.getFirstElement();
		return null;
	}

	public void setSelection(@SuppressWarnings("unchecked") T... selection) {
		setSelection(new StructuredSelection(selection));
	}

	public void selectFirstRow() {
		UI.selectFirstElement(this);
	}

	/**
	 * @param col 0-based column index
	 */
	public void setSortColumn(final int col) {
		getTable().setSortColumn(getTable().getColumn(col));
	}
}
