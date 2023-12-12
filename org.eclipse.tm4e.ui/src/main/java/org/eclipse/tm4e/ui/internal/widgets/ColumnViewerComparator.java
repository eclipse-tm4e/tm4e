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
	 * Sets the sort column. If the newly set sort column equals the previous
	 * set sort column, the sort direction changes.
	 *
	 * @param primaryColumn
	 *            New sort column
	 */
	public void setColumns(final int primaryColumn, int... secondaryColumns) {
		if (primaryColumn == sortColumns[0]) {
			sortOrder *= -1;
		} else {
			if (secondaryColumns.length == 0) {
				sortColumns = new int[] { primaryColumn };
			} else {
				sortColumns = new int[secondaryColumns.length + 1];
				System.arraycopy(secondaryColumns, 0, sortColumns, 1, secondaryColumns.length);
				sortColumns[0] = primaryColumn;
			}
			sortOrder = 1;
		}
	}

	@Override
	public int compare(@Nullable final Viewer viewer, @Nullable final Object e1, @Nullable final Object e2) {
		if (viewer instanceof final TableViewer tableViewer
				&& tableViewer.getLabelProvider() instanceof ITableLabelProvider labelProvider) {
			for (var column : sortColumns) {
				final String left = labelProvider.getColumnText(e1, column);
				final String right = labelProvider.getColumnText(e2, column);
				final int sortResult = getComparator().compare(nullToEmpty(left), nullToEmpty(right));
				if (sortResult != 0)
					return sortResult * sortOrder;
			}
			return 0;
		}
		return super.compare(viewer, e1, e2);
	}
}
