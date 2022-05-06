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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm4e.ui.themes.IThemeManager;

/**
 * A content provider for the template theme page's table viewer.
 *
 */
public final class ThemeContentProvider implements IStructuredContentProvider {

	@Nullable
	private IThemeManager registry;

	@Override
	public Object @Nullable [] getElements(@Nullable final Object input) {
		final var registry = this.registry;
		if (registry == null)
			return null;
		return registry.getThemes();
	}

	@Override
	public void inputChanged(@Nullable final Viewer viewer, @Nullable final Object oldInput,
			@Nullable final Object newInput) {
		registry = (IThemeManager) newInput;
	}

	@Override
	public void dispose() {
		registry = null;
	}

}
