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
package org.eclipse.tm4e.ui.internal.widgets;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Sort the selected column and refresh the viewer.
 *
 */
public final class ColumnSelectionAdapter extends SelectionAdapter {

	private final TableViewer tableViewer;
	private final ColumnViewerComparator viewerComparator;
	private final int[] secondarySortColumns;

	public ColumnSelectionAdapter(final TableViewer tableViewer,
			final ColumnViewerComparator vc, int... secondarySortColumns) {
		this.tableViewer = tableViewer;
		viewerComparator = vc;
		this.secondarySortColumns = secondarySortColumns;
	}

	@Override
	public void widgetSelected(@Nullable final SelectionEvent e) {
		if (e != null && e.getSource() instanceof TableColumn tableColumn) {
			int columnIndex = -1;
			TableColumn[] columns = tableViewer.getTable().getColumns();
			for (int i = 0; i < columns.length; i++) {
				if (columns[i].equals(tableColumn)) {
					columnIndex = i;
					break;
				}
			}
			if (columnIndex == -1)
				return;

			viewerComparator.setColumns(columnIndex, secondarySortColumns);
			final int dir = viewerComparator.getDirection();
			final Table table = tableViewer.getTable();
			table.setSortDirection(dir);
			table.setSortColumn(tableColumn);
			tableViewer.refresh();
		}
	}
}
