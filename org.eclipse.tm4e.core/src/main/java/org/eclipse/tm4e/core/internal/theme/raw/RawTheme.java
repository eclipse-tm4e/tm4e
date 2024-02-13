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
package org.eclipse.tm4e.core.internal.theme.raw;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.parser.PropertySettable;

public final class RawTheme extends PropertySettable.HashMap<@Nullable Object>
		implements IRawTheme, IRawThemeSetting, IThemeSetting {

	private static final long serialVersionUID = 1L;

	/*
	 * IRawTheme
	 */
	@Override
	public @Nullable String getName() {
		return (String) get("name");
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nullable Collection<IRawThemeSetting> getSettings() {
		// vscode themes only
		if (get("tokenColors") instanceof final Collection settings)
			return settings;

		return (Collection<IRawThemeSetting>) get("settings");
	}

	// custom tm4e code, not from upstream
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, String> getEditorColors() {
		// vscode themes only
		if (get("colors") instanceof final Map colors)
			return colors;

		final var settings = getSettings();
		return settings == null
				? Collections.emptyMap()
				: settings.stream()
						.filter(s -> s.getScope() == null)
						.map(s -> ((Map<String, Map<String, String>>) s).get("settings"))
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(Collections.emptyMap());
	}

	/*
	 * IRawThemeSetting
	 */
	@Override
	public @Nullable Object getScope() {
		return get("scope");
	}

	@Override
	public @Nullable IThemeSetting getSetting() {
		return (IThemeSetting) get("settings");
	}

	/*
	 * IThemeSetting
	 */
	@Override
	public @Nullable String getFontStyle() {
		return (String) get("fontStyle");
	}

	@Override
	public @Nullable String getBackground() {
		return (String) get("background");
	}

	@Override
	public @Nullable String getForeground() {
		return (String) get("foreground");
	}
}
