/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Sebastian Thomschke - code extracted from CSSTokenProvider and refactored
 */
package org.eclipse.tm4e.ui.internal.themes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.tm4e.core.internal.utils.ScopeNames;
import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.eclipse.tm4e.ui.themes.ITokenProvider;

public abstract class AbstractTokenProvider implements ITokenProvider {

	private final Map<IStyle, IToken> getJFaceTextTokenReturnValueCache = new HashMap<>();
	private final Map<String /* TextMate Token Type */, IToken> getTokenReturnValueCache = new ConcurrentHashMap<>();

	@Override
	public IToken getToken(final String textMateTokenType) {
		if (textMateTokenType.isEmpty())
			return DEFAULT_TOKEN;

		return getTokenReturnValueCache.computeIfAbsent(
				ScopeNames.withoutContributor(textMateTokenType),
				this::getTokenUncached);
	}

	protected IToken getTokenUncached(final String textMateTokenType) {
		final IStyle style = getBestStyle(textMateTokenType);
		if (style == null)
			return DEFAULT_TOKEN;
		return getJFaceTextToken(style);
	}

	protected IToken getJFaceTextToken(final IStyle style) {
		return getJFaceTextTokenReturnValueCache.computeIfAbsent(style, this::getJFaceTextTokenUncached);
	}

	private IToken getJFaceTextTokenUncached(final IStyle style) {
		final @Nullable RGB styleFGColor = style.getColor();
		final @Nullable RGB styleBGColor = style.getBackgroundColor();
		final var colors = ColorManager.getInstance();
		return new Token(new TextAttribute(
				styleFGColor == null ? null : colors.getColor(styleFGColor),
				styleBGColor == null ? null : colors.getColor(styleBGColor),
				SWT.NORMAL
						| (style.isBold() ? SWT.BOLD : 0)
						| (style.isItalic() ? SWT.ITALIC : 0)
						| (style.isUnderline() ? TextAttribute.UNDERLINE : 0)
						| (style.isStrikeThrough() ? TextAttribute.STRIKETHROUGH : 0)));
	}

	protected abstract @Nullable IStyle getBestStyle(final String textMateTokenType);
}
