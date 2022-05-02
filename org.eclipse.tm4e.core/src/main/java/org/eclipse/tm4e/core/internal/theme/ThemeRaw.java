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
package org.eclipse.tm4e.core.internal.theme;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.theme.IRawTheme;
import org.eclipse.tm4e.core.theme.IRawThemeSetting;
import org.eclipse.tm4e.core.theme.IThemeSetting;

public final class ThemeRaw extends HashMap<String, @Nullable Object>
		implements IRawTheme, IRawThemeSetting, IThemeSetting {

	private static final long serialVersionUID = 1L;

	@Nullable
	@Override
	public String getName() {
		return (String) super.get("name");
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public List<IRawThemeSetting> getSettings() {
		return (List<IRawThemeSetting>) super.get("settings");
	}

	@Nullable
	@Override
	public Object getScope() {
		return super.get("scope");
	}

	@Nullable
	@Override
	public IThemeSetting getSetting() {
		return (IThemeSetting) super.get("settings");
	}

	@Nullable
	@Override
	public Object getFontStyle() {
		return super.get("fontStyle");
	}

	@Nullable
	@Override
	public String getBackground() {
		return (String) super.get("background");
	}

	@Nullable
	@Override
	public String getForeground() {
		return (String) super.get("foreground");
	}
}
