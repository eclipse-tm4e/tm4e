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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class VerticalSplitPane {

	private final SashForm sash;

	protected VerticalSplitPane(final Composite parent, int upperAreaWeight, int lowerAreaWeight) {
		sash = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
		sash.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		sash.setSashWidth(5);

		final var aboveSashArea = new Composite(sash, SWT.NONE);
		aboveSashArea.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
		configureUpperArea(aboveSashArea);

		var spacer = new Composite(aboveSashArea, SWT.NONE);
		spacer.setLayoutData(GridDataFactory.fillDefaults().hint(1, 2).create());

		final var belowSashArea = new Composite(sash, SWT.NONE);
		belowSashArea.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());

		spacer = new Composite(belowSashArea, SWT.NONE);
		spacer.setLayoutData(GridDataFactory.fillDefaults().hint(1, 2).create());

		configureLowerArea(belowSashArea);

		sash.setWeights(upperAreaWeight, lowerAreaWeight);
	}

	protected abstract void configureUpperArea(final Composite parent);

	protected abstract void configureLowerArea(final Composite parent);

	public Control getControl() {
		return sash;
	}
}
