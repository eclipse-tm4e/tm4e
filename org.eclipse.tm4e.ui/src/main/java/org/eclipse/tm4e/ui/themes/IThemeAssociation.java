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

import org.eclipse.tm4e.registry.ITMDefinition;

/**
 * Theme association API.
 *
 */
public interface IThemeAssociation extends ITMDefinition {

	/**
	 * Returns the TextMate theme id.
	 * 
	 * @return the TextMate theme id.
	 */
	String getThemeId();

	/**
	 * Returns the TextMate grammar scope linked to the theme id and null otherwise.
	 * 
	 * @return the TextMate grammar scope to the theme id and null otherwise.
	 */
	String getScopeName();

	boolean isWhenDark();
}
