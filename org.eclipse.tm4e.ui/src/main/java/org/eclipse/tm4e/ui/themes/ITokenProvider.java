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
import org.eclipse.tm4e.core.model.TMToken;

/**
 * Provider to retrieve Eclipse JFace Text {@link IToken}s for TextMate token types.
 */
public interface ITokenProvider {

	IToken DEFAULT_TOKEN = new Token(null);

	/**
	 * @return the Eclipse JFace Text {@link IToken} for the given TexMate token type or {@link #DEFAULT_TOKEN} if the given type is empty.
	 */
	IToken getToken(String tmTokenType);

	/**
	 * Allows token providers to compute styles using the token's full TextMate scopes stack.
	 *
	 * @return the Eclipse JFace Text {@link IToken} for the given TexMate token or {@link #DEFAULT_TOKEN} if {@link TMToken#scopes} is
	 *         empty.
	 */
	IToken getToken(final TMToken token);

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
