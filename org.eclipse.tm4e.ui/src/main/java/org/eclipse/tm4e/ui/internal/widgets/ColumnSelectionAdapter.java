/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.widgets;

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

	private final TableColumn fTableColumn;
	private final TableViewer tableViewer;
	private final int fColumnIndex;
	private final ColumnViewerComparator viewerComparator;

	public ColumnSelectionAdapter(TableColumn column, TableViewer tableViewer, int index, ColumnViewerComparator vc) {
		fTableColumn = column;
		this.tableViewer = tableViewer;
		fColumnIndex = index;
		viewerComparator = vc;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		viewerComparator.setColumn(fColumnIndex);
		int dir = viewerComparator.getDirection();
		Table table = tableViewer.getTable();
		table.setSortDirection(dir);
		table.setSortColumn(fTableColumn);
		tableViewer.refresh();
	}
}
