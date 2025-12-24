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
 * - Sebastian Thomschke (Vegard IT) - reusable code pushed down to AbstractTokenProvider
 */
package org.eclipse.tm4e.ui.themes.css;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.graphics.Color;
import org.eclipse.tm4e.core.internal.utils.StringUtils;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.css.CSSParser;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.themes.AbstractTokenProvider;
import org.eclipse.tm4e.ui.themes.ColorManager;

public class CSSTokenProvider extends AbstractTokenProvider {

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

	private final CSSParser parser;

	public CSSTokenProvider(final InputStream in) {
		CSSParser parser = null;
		try {
			parser = new CSSParser(in);
		} catch (final Exception ex) {
			TMUIPlugin.logError(ex);
		}

		this.parser = parser == null ? new NoopCSSParser() : parser;
	}

	@Override
	protected @Nullable IStyle getBestStyle(final String textMateTokenType) {
		return parser.getBestStyle(StringUtils.splitToArray(textMateTokenType, '.'));
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

	@Override
	public IToken getToken(final TMToken token) {
		return getToken(token.type);
	}
}
