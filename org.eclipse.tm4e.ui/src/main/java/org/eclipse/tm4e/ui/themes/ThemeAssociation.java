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
package org.eclipse.tm4e.ui.themes;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.assertNonNull;

import java.util.Objects;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Theme association implementation.
 */
public final class ThemeAssociation implements IThemeAssociation {

	private static final String THEME_ID_ATTR = "themeId"; //$NON-NLS-1$
	private static final String SCOPE_NAME_ATTR = "scopeName"; //$NON-NLS-1$
	private static final String WHEN_DARK_ATTR = "whenDark"; //$NON-NLS-1$

	private final String themeId;

	@Nullable
	private String scopeName;

	private boolean whenDark;

	@Nullable
	private String pluginId;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public ThemeAssociation() {
		themeId = "<set-by-gson>";
	}

	/**
	 * Constructor to register theme associations for a given scope name.
	 */
	public ThemeAssociation(final String themeId, final String scopeName, final boolean whenDark) {
		this.themeId = themeId;
		this.scopeName = scopeName;
		this.whenDark = whenDark;
	}

	public ThemeAssociation(final IConfigurationElement ce) {
		this(assertNonNull(ce.getAttribute(THEME_ID_ATTR)),
				assertNonNull(ce.getAttribute(SCOPE_NAME_ATTR)),
				"true".equals(ce.getAttribute(WHEN_DARK_ATTR)));
		this.pluginId = ce.getNamespaceIdentifier();
	}

	@Nullable
	@Override
	public String getPluginId() {
		return pluginId;
	}

	@Override
	public String getThemeId() {
		return themeId;
	}

	@Nullable
	@Override
	public String getScopeName() {
		return scopeName;
	}

	@Override
	public boolean isWhenDark() {
		return whenDark;
	}

	@Override
	public int hashCode() {
		int result = 31 + Boolean.hashCode(whenDark);
		result = 31 * result + Objects.hashCode(pluginId);
		result = 31 * result + Objects.hashCode(scopeName);
		return 31 * result + themeId.hashCode();
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof final ThemeAssociation other)
			return whenDark == other.whenDark
					&& Objects.equals(pluginId, other.pluginId)
					&& Objects.equals(scopeName, other.scopeName)
					&& Objects.equals(themeId, other.themeId);
		return false;
	}

}
