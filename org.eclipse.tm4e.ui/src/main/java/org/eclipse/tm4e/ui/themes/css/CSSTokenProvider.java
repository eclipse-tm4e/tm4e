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
package org.eclipse.tm4e.ui.themes.css;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.tm4e.core.internal.utils.StringUtils;
import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.core.theme.css.CSSParser;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.eclipse.tm4e.ui.themes.ITokenProvider;

public class CSSTokenProvider implements ITokenProvider {

	private static final class NoopCSSParser extends CSSParser {
		@Override
		public List<IStyle> getStyles() {
			return Collections.emptyList();
		}

		@Override
		public @Nullable IStyle getBestStyle(final String... names) {
			return null;
		}
	}

	private final Map<IStyle, @Nullable IToken> tokenMaps = new HashMap<>();
	private final Map<String, IToken> getTokenReturnValueCache = new ConcurrentHashMap<>();

	private final CSSParser parser;

	public CSSTokenProvider(final InputStream in) {
		CSSParser parser = null;
		final var colors = ColorManager.getInstance();
		try {
			parser = new CSSParser(in);
			for (final IStyle style : parser.getStyles()) {
				final @Nullable RGB styleFGColor = style.getColor();
				final @Nullable RGB styleBGColor = style.getBackgroundColor();
				tokenMaps.put(style, new Token(new TextAttribute(
						styleFGColor == null ? null : colors.getColor(styleFGColor),
						styleBGColor == null ? null : colors.getColor(styleBGColor),
						SWT.NORMAL
								| (style.isBold() ? SWT.BOLD : 0)
								| (style.isItalic() ? SWT.ITALIC : 0)
								| (style.isUnderline() ? TextAttribute.UNDERLINE : 0)
								| (style.isStrikeThrough() ? TextAttribute.STRIKETHROUGH : 0))));
			}
		} catch (final Exception ex) {
			TMUIPlugin.logError(ex);
		}

		this.parser = parser == null ? new NoopCSSParser() : parser;
	}

	@Override
	public IToken getToken(final String textMateTokenType) {
		if (textMateTokenType.isEmpty())
			return DEFAULT_TOKEN;

		return getTokenReturnValueCache.computeIfAbsent(textMateTokenType, this::getTokenInternal);
	}

	private IToken getTokenInternal(final String type) {
		final IStyle style = parser.getBestStyle(StringUtils.splitToArray(type, '.'));
		if (style == null)
			return DEFAULT_TOKEN;
		final IToken token = tokenMaps.get(style);
		return token == null ? DEFAULT_TOKEN : token;
	}

	private @Nullable Color getColor(final boolean isForeground, final String... cssClassNames) {
		final var style = parser.getBestStyle(cssClassNames);
		if (style == null)
			return null;

		final var rgb = isForeground ? style.getColor() : style.getBackgroundColor();
		if (rgb == null)
			return null;

		return ColorManager.getInstance().getColor(rgb);
	}

	@Override
	public @Nullable Color getEditorForeground() {
		return getColor(true, "editor");
	}

	@Override
	public @Nullable Color getEditorBackground() {
		return getColor(false, "editor");
	}

	@Override
	public @Nullable Color getEditorSelectionForeground() {
		return getColor(true, "editor", "selection");
	}


	@Override
	public @Nullable Color getEditorSelectionBackground() {
		return getColor(false, "editor", "selection");
	}


	@Override
	public @Nullable Color getEditorCurrentLineHighlight() {
		return getColor(false, "editor", "lineHighlight");
	}
}
