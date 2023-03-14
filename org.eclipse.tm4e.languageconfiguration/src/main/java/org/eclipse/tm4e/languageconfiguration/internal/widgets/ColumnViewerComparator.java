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
 */
package org.eclipse.tm4e.languageconfiguration.internal.widgets;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * Viewer compoarator which sort a given column.
 *
 */
public final class ColumnViewerComparator extends ViewerComparator {

	private int fSortColumn = 0;
	private int fSortOrder = 1; // 1 = asc, -1 = desc

	/**
	 * Returns the {@linkplain SWT} style constant for the sort direction.
	 *
	 * @return {@link SWT#DOWN} for asc sorting, {@link SWT#UP} otherwise
	 */
	public int getDirection() {
		return fSortOrder == 1 ? SWT.DOWN : SWT.UP;
	}

	/**
	 * Sets the sort column. If the newly set sort column equals the previous set
	 * sort column, the sort direction changes.
	 *
	 * @param column New sort column
	 */
	public void setColumn(final int column) {
		if (column == fSortColumn) {
			fSortOrder *= -1;
		} else {
			fSortColumn = column;
			fSortOrder = 1;
		}
	}

	@Override
	public int compare(@Nullable final Viewer viewer, @Nullable final Object e1, @Nullable final Object e2) {

		if (viewer instanceof final TableViewer tableViewer) {
			final IBaseLabelProvider baseLabel = tableViewer.getLabelProvider();
			final String left = ((ITableLabelProvider) baseLabel).getColumnText(e1, fSortColumn);
			final String right = ((ITableLabelProvider) baseLabel).getColumnText(e2, fSortColumn);
			final int sortResult = getComparator().compare(left != null ? left : "", right != null ? right : ""); //$NON-NLS-1$ //$NON-NLS-2$
			return sortResult * fSortOrder;
		}

		return super.compare(viewer, e1, e2);
	}
}
