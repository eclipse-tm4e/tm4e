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
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/theme.ts#L91">
 *      github.com/microsoft/vscode-textmate/blob/main/src/theme.ts</a>
 */
public interface IRawTheme {

	@Nullable
	String getName();

	@Nullable
	Collection<IRawThemeSetting> getSettings();

	Map<String, String> getEditorColors(); // custom tm4e code, not from upstream
}
