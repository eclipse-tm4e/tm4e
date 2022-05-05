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

	private final Map<IStyle, IToken> tokenMaps = new HashMap<>();
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
			TMUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	@Override
	public IToken getToken(final String type) {
		if (type == null) {
			return null;
		}
		final IStyle style = parser.getBestStyle(BY_DOT_SPLITTER.splitToStream(type).toArray(String[]::new));
		if (style != null) {
			final IToken t = tokenMaps.get(style);
			if (t != null) {
				return t;
			}
		}
		return null;
	}

	@Override
	public Color getEditorForeground() {
		final IStyle style = parser.getBestStyle("editor");
		if (style != null && style.getColor() != null) {
			return ColorManager.getInstance().getColor(style.getColor());
		}
		return null;
	}

	@Override
	public Color getEditorBackground() {
		final IStyle style = parser.getBestStyle("editor");
		if (style != null && style.getBackgroundColor() != null) {
			return ColorManager.getInstance().getColor(style.getBackgroundColor());
		}
		return null;
	}

	@Override
	public Color getEditorSelectionForeground() {
		final IStyle style = parser.getBestStyle("editor", "selection");
		if (style != null && style.getColor() != null) {
			return ColorManager.getInstance().getColor(style.getColor());
		}
		return null;
	}

	@Override
	public Color getEditorSelectionBackground() {
		final IStyle style = parser.getBestStyle("editor", "selection");
		if (style != null && style.getBackgroundColor() != null) {
			return ColorManager.getInstance().getColor(style.getBackgroundColor());
		}
		return null;
	}

	@Override
	public Color getEditorCurrentLineHighlight() {
		final IStyle style = parser.getBestStyle("editor", "lineHighlight");
		if (style != null && style.getBackgroundColor() != null) {
			return ColorManager.getInstance().getColor(style.getBackgroundColor());
		}
		return null;
	}

}
