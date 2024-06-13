/*******************************************************************************
 * Copyright (c) 2024 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.themes;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.tm4e.core.internal.grammar.ScopeStack;
import org.eclipse.tm4e.core.internal.theme.FontStyle;
import org.eclipse.tm4e.core.internal.theme.Style;
import org.eclipse.tm4e.core.internal.theme.StyleAttributes;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.ui.themes.ColorManager;

/**
 * TextMate theme token provider
 */
public class TMThemeTokenProvider extends AbstractTokenProvider {

	private final Theme theme;
	private final List<String> colors;

	public TMThemeTokenProvider(final IThemeSource.ContentType contentType, final InputStream in) throws Exception {
		final var rawTheme = RawThemeReader
				.readTheme(IThemeSource.fromString(contentType, new String(in.readAllBytes(), StandardCharsets.UTF_8)));
		theme = Theme.createFromRawTheme(rawTheme, null);
		colors = theme.getColorMap();
	}

	@Override
	protected @Nullable IStyle getBestStyle(String textMateTokenType) {
		StyleAttributes styleAttrs = null;
		while (styleAttrs == null) {
			styleAttrs = theme.match(ScopeStack.from(textMateTokenType));
			if (styleAttrs == null || styleAttrs.equals(StyleAttributes.NO_STYLE)) {
				styleAttrs = null;
				final var dotIdx = textMateTokenType.indexOf('.');
				if (dotIdx == -1) {
					break;
				}
				// this is a workaround because org.eclipse.tm4e.core.model.TMTokenizationSupport.decodeTextMateToken(DecodeMap, List<String>)
				// simply concatenates scopes into one and here we don't know how to separate them, e.g. "meta.package.java" + "keyword.other" = "meta.package.java.keyword.other"
				// this results in style definitions for "keyword.other" are not returned by Theme#match() which only matches like "^keyword\.other.*"
				// -> time to upgrade to IGrammar.tokenizeLine2?
				textMateTokenType = textMateTokenType.substring(dotIdx + 1);
			}
		}

		if (styleAttrs != null) {
			final var style = new Style();
			if (styleAttrs.foregroundId > 0)
				style.setColor(RGB.fromHex(colors.get(styleAttrs.foregroundId)));
			if (styleAttrs.backgroundId > 0)
				style.setBackgroundColor(RGB.fromHex(colors.get(styleAttrs.backgroundId)));

			if (styleAttrs.fontStyle > 0) {
				style.setBold(FontStyle.isBold(styleAttrs.fontStyle));
				style.setItalic(FontStyle.isItalic(styleAttrs.fontStyle));
				style.setUnderline(FontStyle.isUnderline(styleAttrs.fontStyle));
				style.setStrikeThrough(FontStyle.isStrikethrough(styleAttrs.fontStyle));
			}
			return style;
		}
		return null;
	}

	protected @Nullable Color getEditorColor(final String... names) {
		for (final String name : names) {
			final String colorHexCode = theme.getEditorColors().get(name);
			if (colorHexCode == null)
				continue;
			final var rgb = RGB.fromHex(colorHexCode);
			if (rgb == null)
				continue;
			return ColorManager.getInstance().getColor(rgb);
		}
		return null;
	}

	// https://code.visualstudio.com/api/references/theme-color#editor-colors

	@Override
	public @Nullable Color getEditorForeground() {
		return getEditorColor(/*sublime:*/ "foreground", /*vscode:*/ "editor.foreground");
	}

	@Override
	public @Nullable Color getEditorBackground() {
		return getEditorColor(/*sublime:*/ "background", /*vscode:*/ "editor.background");
	}

	@Override
	public @Nullable Color getEditorSelectionForeground() {
		return getEditorColor(/*sublime:*/ "selectionForeground", /*vscode:*/ "editor.selectionForeground", "selection.foreground");
	}

	@Override
	public @Nullable Color getEditorSelectionBackground() {
		return getEditorColor(/*sublime:*/ "selection", /*vscode:*/ "editor.selectionBackground", "editor.selectionHighlightBackground",
				"selection.background");
	}

	@Override
	public @Nullable Color getEditorCurrentLineHighlight() {
		return getEditorColor(/*sublime:*/ "lineHighlight", /*vscode:*/ "editor.lineHighlightBackground");
	}
}
