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
package org.eclipse.tm4e.ui.internal.preferences;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public abstract class AbstractPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private final @Nullable String title;

	protected AbstractPreferencePage(final @Nullable String title, final @Nullable String description) {
		this.title = title;
		setDescription(description);
	}

	@Override
	public void init(final @NonNullByDefault({}) IWorkbench workbench) {
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (visible && title != null && !title.isEmpty())
			setTitle(title);
	}
}
