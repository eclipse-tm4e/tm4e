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

import org.eclipse.jdt.annotation.Nullable;

/**
 * A single theme setting.
 *
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/theme.ts#L91">
 *      github.com/microsoft/vscode-textmate/blob/main/src/theme.ts</a>
 */
public interface IRawThemeSetting {

	@Nullable
	String getName();

	/**
	 * @return String || List<String>
	 */
	@Nullable
	Object getScope();

	@Nullable
	IThemeSetting getSetting();

}
