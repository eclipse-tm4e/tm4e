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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.core.theme.css.CSSParser;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.themes.AbstractTokenProvider;
import org.eclipse.tm4e.ui.themes.ColorManager;

import com.google.common.base.Splitter;

public class CSSTokenProvider extends AbstractTokenProvider {

	private static final Splitter BY_DOT_SPLITTER = Splitter.on('.');

	private final Map<IStyle, @Nullable IToken> tokenMaps = new HashMap<>();

	@Nullable
	private CSSParser parser;

	public CSSTokenProvider(final InputStream in) {
		try {
			parser = new CSSParser(in);
			for (final IStyle style : parser.getStyles()) {
				final RGB color = style.getColor();
				if (color != null) {
					int s = SWT.NORMAL;
					if (style.isBold()) {
						s = s | SWT.BOLD;
					}
					if (style.isItalic()) {
						s = s | SWT.ITALIC;
					}
					if (style.isUnderline()) {
						s = s | TextAttribute.UNDERLINE;
					}
					if (style.isStrikeThrough()) {
						s = s | TextAttribute.STRIKETHROUGH;
					}
					tokenMaps.put(style,
							new Token(new TextAttribute(ColorManager.getInstance().getColor(color), null, s)));
				}
			}
		} catch (final Exception e) {
			TMUIPlugin.log(new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	@Nullable
	@Override
	public IToken getToken(@Nullable final String type) {
		if (type == null)
			return null;

		final var parser = this.parser;
		if (parser == null)
			return null;

		final IStyle style = parser.getBestStyle(BY_DOT_SPLITTER.splitToStream(type).toArray(String[]::new));
		if (style == null)
			return null;

		return tokenMaps.get(style);
	}

	@Nullable
	private Color getColor(final boolean isForeground, final String... styles) {
		final var parser = this.parser;
		if (parser == null)
			return null;

		final var style = parser.getBestStyle(styles);
		if (style == null)
			return null;

		final var rgb = isForeground ? style.getColor() : style.getBackgroundColor();
		if (rgb == null)
			return null;

		return ColorManager.getInstance().getColor(rgb);
	}

	@Nullable
	@Override
	public Color getEditorForeground() {
		return getColor(true, "editor");
	}

	@Nullable
	@Override
	public Color getEditorBackground() {
		return getColor(false, "editor");
	}

	@Nullable
	@Override
	public Color getEditorSelectionForeground() {
		return getColor(true, "editor", "selection");
	}

	@Nullable
	@Override
	public Color getEditorSelectionBackground() {
		return getColor(false, "editor", "selection");

	}

	@Nullable
	@Override
	public Color getEditorCurrentLineHighlight() {
		return getColor(false, "editor", "lineHighlight");
	}
}
