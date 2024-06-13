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
 * Sebastian Thomschke (Vegard IT) - major refactoring, type safe elements, addition of optional filter bar
 */
package org.eclipse.tm4e.ui.internal.widgets;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Widget which display a table on the left and buttons on the right
 * and an optional filter bar on top.
 */
public abstract class TableWithControlsWidget<T> extends Composite {

	protected final TableWidget<T> table;
	private final Composite buttonsArea;

	protected TableWithControlsWidget(final Composite parent, final @Nullable String title, final boolean enableFiltering) {
		super(parent, SWT.NONE);

		setLayout(GridLayoutFactory.fillDefaults()
				.numColumns(2)
				.spacing(5, 2)
				.equalWidth(false)
				.create());

		if (title != null && !title.isEmpty())
			createTitle(title, this);

		if (enableFiltering) {
			createFilter(this);
		}

		table = createTable(this);

		buttonsArea = new Composite(this, SWT.NONE);
		buttonsArea.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 5).create());
		buttonsArea.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_VERTICAL));
		createButtons();
		if (buttonsArea.getChildren().length == 0) {
			(castNonNull((GridLayout) parent.getLayout())).horizontalSpacing = 0;
		}
	}

	protected void createButtons() {
	}

	protected Button createButton(final String title, final Runnable onClick) {
		final var button = new Button(buttonsArea, SWT.PUSH);
		button.setText(title);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.addListener(SWT.Selection, e -> onClick.run());
		return button;
	}

	private void createTitle(final String title, final Composite parent) {
		final var label = new Label(parent, SWT.NONE);
		label.setText(title);
		final var data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}

	private void createFilter(final Composite parent) {
		final var filterInput = new Text(this, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH | SWT.FLAT);
		filterInput.setMessage(" type filter text");
		filterInput.addModifyListener(e -> onFilterChanged(filterInput.getText()));
		filterInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		@SuppressWarnings("unused")
		final var spacing = new Label(parent, SWT.NONE);
	}

	protected abstract TableWidget<T> createTable(final Composite parent);

	protected void onFilterChanged(final String newFilterText) {
		if (newFilterText.isEmpty()) {
			table.resetFilters();
		} else {
			final var filterText = newFilterText.toLowerCase();
			final var swtTable = table.getTable();
			final var colCount = swtTable.getColumnCount();

			table.resetFilters();
			table.setFilters(new ViewerFilter() {
				@Override
				public boolean select(final Viewer viewer, final @Nullable Object parentElement, final Object element) {
					for (int i = 0, l = swtTable.getItemCount(); i < l; i++) {
						if (table.getElementAt(i) == element) {
							final var row = swtTable.getItem(i);
							for (int j = 0; j < colCount; j++) {
								if (row.getText(j).toLowerCase().contains(filterText)) {
									return true;
								}
							}
							return false;
						}
					}
					return false;
				}
			});
		}
	}

	public TableWidget<T> getTable() {
		return table;
	}
}
