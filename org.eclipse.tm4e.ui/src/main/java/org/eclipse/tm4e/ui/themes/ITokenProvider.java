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
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

/**
 * Provider to retrieve Eclipse {@link IToken} from the TextMate token type.
 *
 */
public interface ITokenProvider {

	IToken DEFAULT_TOKEN = new Token(null);

	/**
	 * Returns the Eclipse {@link IToken} from the given type and {@link #DEFAULT_TOKEN} otherwise.
	 *
	 * @param type
	 *
	 * @return the Eclipse {@link IToken} from the given type and {@link #DEFAULT_TOKEN} otherwise.
	 */
	IToken getToken(String type);

	@Nullable
	Color getEditorBackground();

	@Nullable
	Color getEditorForeground();

	@Nullable
	Color getEditorSelectionBackground();

	@Nullable
	Color getEditorSelectionForeground();

	@Nullable
	Color getEditorCurrentLineHighlight();
}
