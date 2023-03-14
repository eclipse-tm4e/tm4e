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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.custom.StyledText;

/**
 * TextMate theme.
 *
 */
public interface ITheme extends ITokenProvider {

	/**
	 * Returns the id of the theme.
	 *
	 * @return the id of the theme.
	 */
	String getId();

	/**
	 * Returns the name of the theme.
	 *
	 * @return the name of the theme.
	 */
	String getName();

	/**
	 * Returns the path of the theme.
	 *
	 * @return the path of the theme.
	 */
	@Nullable
	String getPath();

	/**
	 * Returns the plugin id.
	 *
	 * @return the plugin id
	 */
	@Nullable
	String getPluginId();

	/**
	 * Returns the theme content as CSS style sheet.
	 *
	 * @return the theme content as CSS style sheet.
	 */
	@Nullable
	String toCSSStyleSheet();

	boolean isDark();

	boolean isDefault();

	/**
	 * Initialize foreground, background color of the given {@link StyledText} with theme.
	 *
	 * @param styledText the StyledText to initialize
	 */
	void initializeViewerColors(StyledText styledText);

}
