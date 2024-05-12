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
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.ui.internal.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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
	private final List<int[]> secondarySortColumns = new ArrayList<>();
	private final ColumnViewerComparator viewerComparator = new ColumnViewerComparator();

	protected TableWidget(final Composite parent, final boolean allowMultiSelection) {
		this(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | (allowMultiSelection ? SWT.MULTI : SWT.SINGLE));
	}

	protected TableWidget(final Composite parent, final int style) {
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
		setSortColumn(0, SWT.DOWN);

		BidiUtils.applyTextDirection(getControl(), BidiUtils.BTD_DEFAULT);
	}

	protected void createAutoResizeColumn(final String label, final int... secondarySortColumns) {
		createColumn(label, 0, 0, secondarySortColumns);
		final var col = getTable().getColumn(getTable().getColumnCount() - 1);
		autoResizeColumns.add(col);
	}

	protected void createColumn(final @Nullable String label, final int columnWeight, final int minColWidth,
			final int... secondarySortColumns) {
		final var col = new TableColumn(getTable(), SWT.NONE);
		col.setText(label == null ? "" : label);
		this.secondarySortColumns.add(secondarySortColumns);
		col.addSelectionListener(new ColumnSelectionAdapter(this, viewerComparator, secondarySortColumns));

		tableColumnLayout.setColumnData(col,
				new ColumnWeightData(columnWeight, Math.max(UI.getTextWidth(col.getText()) + 15, minColWidth), true));
	}

	protected abstract void createColumns();

	/**
	 * @return an object whose #toString() method is called to get the element's text for the given column
	 */
	protected abstract @Nullable Object getColumnText(T element, int columnIndex);

	@SuppressWarnings({ "unchecked" })
	public List<T> getElements() {
		return (List<T>) List.of(getElements(getInput()));
	}

	protected Object[] getElements(final @Nullable Object input) {
		if (input == null)
			return new Object[0];
		if (input instanceof final Collection<?> coll)
			return coll.toArray(Object[]::new);
		return (Object[]) input;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getElementAt(int index) {
		return (T) super.getElementAt(index);
	}

	public int getElementCount() {
		return doGetItemCount();
	}

	public boolean isEmpty() {
		return doGetItemCount() == 0;
	}

	@Override
	protected void inputChanged(final @Nullable Object input, final @Nullable Object oldInput) {
		super.inputChanged(input, oldInput);
		for (final TableColumn column : getTable().getColumns()) {
			if (autoResizeColumns.contains(column)) {
				column.pack();
			}
		}

		// auto refresh when input has changed
		refresh();

		if (getFirstSelectedElement() == null) {
			selectFirstRow();
		}
	}

	public TableWidget<T> onSelectionChanged(final Consumer<List<T>> consumer) {
		addSelectionChangedListener(e -> {
			if (e.getSelection() instanceof final IStructuredSelection sel) {
				if (sel.isEmpty())
					consumer.accept(Collections.emptyList());
				else
					consumer.accept(sel.toList());
			}
		});
		return this;
	}

	public TableWidget<T> selectFirstRow() {
		UI.selectFirstElement(this);
		return this;
	}

	@SuppressWarnings("unchecked")
	public @Nullable T getFirstSelectedElement() {
		if (super.getSelection() instanceof final IStructuredSelection selection)
			return (T) selection.getFirstElement();
		return null;
	}

	/**
	 * @deprecated use {@link #getTypedSelection()}
	 */
	@Deprecated
	@Override
	public ISelection getSelection() {
		return super.getSelection();
	}

	public List<T> getTypedSelection() {
		if (super.getSelection() instanceof final IStructuredSelection selection)
			return selection.toList();
		return Collections.emptyList();
	}

	public void setSelection(@SuppressWarnings("unchecked") final T... selection) {
		setSelection(new StructuredSelection(selection));
	}

	public void setSelection(boolean reveal, @SuppressWarnings("unchecked") final T... selection) {
		setSelection(selection);
		if (reveal) {
			reveal(selection[0]);
		}
	}

	/**
	 * @param col 0-based column index
	 * @param sortDirection {@link SWT#DOWN} or {@link SWT#UP}
	 */
	public void setSortColumn(final int col, int sortDirection) {
		viewerComparator.setColumns(col, secondarySortColumns.get(col));
		viewerComparator.setDirection(sortDirection);
		getTable().setSortDirection(viewerComparator.getDirection());
		getTable().setSortColumn(getTable().getColumn(col));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void refresh(final @Nullable Object object) {
		final var swtTable = getTable();
		var selectedIndex = swtTable.getSelectionIndex();

		super.refresh(object);

		// restore selection
		if (selectedIndex > -1 && !isEmpty()) {
			if (selectedIndex >= getElementCount()) {
				selectedIndex--;
			}
			final var newSelection = getElementAt(selectedIndex);
			if (newSelection != null)
				setSelection(newSelection);
		}
	}
}
