/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Sebastian Thomschke (Vegard IT) - add support for secondary sort columns
 */
package org.eclipse.tm4e.ui.internal.widgets;

import static org.eclipse.tm4e.core.internal.utils.StringUtils.nullToEmpty;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * {@link TableViewer} comparator which sorts based on the given column(s).
 */
public final class ColumnViewerComparator extends ViewerComparator {

	private int[] sortColumns = { 0 };
	private int sortOrder = 1; // 1 = ascending, -1 = descending

	/**
	 * Returns the {@linkplain SWT} style constant for the sort direction.
	 *
	 * @return {@link SWT#DOWN} for ascending sorting, {@link SWT#UP} otherwise
	 */
	public int getDirection() {
		return sortOrder == 1 ? SWT.DOWN : SWT.UP;
	}

	/**
	 * @param value {@link SWT#DOWN} or {@link SWT#UP}
	 */
	public void setDirection(final int value) {
		this.sortOrder = value == SWT.DOWN ? 1 : -1;
	}

	/**
	 * Sets the sort column. If the newly set sort column equals the previous
	 * set sort column, the sort direction changes.
	 *
	 * @param primaryColumn New sort column
	 */
	public void setColumnsOrDirection(final int primaryColumn, final int... secondaryColumns) {
		if (primaryColumn == sortColumns[0]) {
			sortOrder *= -1;
		} else {
			sortOrder = 1;
		}
		setColumns(primaryColumn, secondaryColumns);
	}

	/**
	 * Sets the sort column.
	 *
	 * @param primaryColumn New sort column
	 */
	public void setColumns(final int primaryColumn, final int... secondaryColumns) {
		if (secondaryColumns.length == 0) {
			sortColumns = new int[] { primaryColumn };
		} else {
			sortColumns = new int[secondaryColumns.length + 1];
			System.arraycopy(secondaryColumns, 0, sortColumns, 1, secondaryColumns.length);
			sortColumns[0] = primaryColumn;
		}
	}

	@Override
	public int compare(final @Nullable Viewer viewer, final @Nullable Object e1, final @Nullable Object e2) {
		if (viewer instanceof final TableViewer tableViewer
				&& tableViewer.getLabelProvider() instanceof final ITableLabelProvider labelProvider) {
			for (final var column : sortColumns) {
				final String left = nullToEmpty(labelProvider.getColumnText(e1, column));
				final String right = nullToEmpty(labelProvider.getColumnText(e2, column));

				// make empty strings to come after non-empty strings
				if (left.isEmpty() && right.isEmpty())
					continue;
				if (left.isEmpty())
					return 1 * sortOrder;
				if (right.isEmpty())
					return -1 * sortOrder;

				final int sortResult = getComparator().compare(left, right);
				if (sortResult != 0)
					return sortResult * sortOrder;
			}
			return 0;
		}
		return super.compare(viewer, e1, e2);
	}
}
