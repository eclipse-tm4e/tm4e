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
package org.eclipse.tm4e.ui.themes;

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
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Theme association implementation.
 *
 */
public class ThemeAssociation implements IThemeAssociation {

	private static final String THEME_ID_ATTR = "themeId"; //$NON-NLS-1$
	private static final String SCOPE_NAME_ATTR = "scopeName"; //$NON-NLS-1$
	private static final String WHEN_DARK_ATTR = "whenDark"; //$NON-NLS-1$

	private String themeId;
	private String scopeName;
	private boolean whenDark;
	private String pluginId;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public ThemeAssociation() {
		super();
	}

	/**
	 * Constructor to register theme associations for a given scope name.
	 *
	 * @param themeId
	 * @param scopeName
	 * @param whenDark
	 */
	public ThemeAssociation(String themeId, String scopeName, boolean whenDark) {
		this.themeId = themeId;
		this.scopeName = scopeName;
		this.whenDark = whenDark;
	}

	public ThemeAssociation(IConfigurationElement ce) {
		this(ce.getAttribute(THEME_ID_ATTR), ce.getAttribute(SCOPE_NAME_ATTR),
				"true".equals(ce.getAttribute(WHEN_DARK_ATTR)));
		this.pluginId = ce.getNamespaceIdentifier();
	}

	@Override
	public String getPluginId() {
		return pluginId;
	}

	@Override
	public String getThemeId() {
		return themeId;
	}

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pluginId == null) ? 0 : pluginId.hashCode());
		result = prime * result + ((scopeName == null) ? 0 : scopeName.hashCode());
		result = prime * result + ((themeId == null) ? 0 : themeId.hashCode());
		result = prime * result + (whenDark ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThemeAssociation other = (ThemeAssociation) obj;
		if (pluginId == null) {
			if (other.pluginId != null)
				return false;
		} else if (!pluginId.equals(other.pluginId))
			return false;
		if (scopeName == null) {
			if (other.scopeName != null)
				return false;
		} else if (!scopeName.equals(other.scopeName))
			return false;
		if (themeId == null) {
			if (other.themeId != null)
				return false;
		} else if (!themeId.equals(other.themeId))
			return false;
		if (whenDark != other.whenDark)
			return false;
		return true;
	}

}
