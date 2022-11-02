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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

/**
 * Widget which display a table on the left and buttons on the right.
 */
public abstract class TableAndButtonsWidget extends Composite {

	private TableViewer viewer = lazyNonNull();
	private Composite buttonsArea = lazyNonNull();

	protected TableAndButtonsWidget(final Composite parent, final int style, final String title) {
		super(parent, style);
		final var layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		super.setLayout(layout);
		createUI(title, this);
	}

	private void createUI(final String title, final Composite ancestor) {
		final var parent = new Composite(ancestor, SWT.NONE);
		var layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Title
		createTitle(title, parent);

		// Table
		createTable(parent);

		// Buttons
		buttonsArea = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		buttonsArea.setLayout(layout);
		buttonsArea.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_VERTICAL));
	}

	private void createTitle(final String title, final Composite ancestor) {
		final var label = new Label(ancestor, SWT.NONE);
		label.setText(title);
		final var data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}

	private void createTable(final Composite parent) {
		final var table = new Table(parent,
				SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);

		viewer = new TableViewer(table);
		table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected Composite getButtonsArea() {
		return buttonsArea;
	}

	public void setInput(@Nullable final Object input) {
		viewer.setInput(input);
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void setLabelProvider(final IBaseLabelProvider labelProvider) {
		viewer.setLabelProvider(labelProvider);
	}

	public void setContentProvider(final IContentProvider provider) {
		viewer.setContentProvider(provider);
	}

	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}

	public void setSelection(final IStructuredSelection selection) {
		viewer.setSelection(selection);
	}

	public IStructuredSelection getSelection() {
		return viewer.getStructuredSelection();
	}
}
