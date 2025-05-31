/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.core.internal.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.nio.file.Path;

import org.eclipse.tm4e.core.internal.grammar.ScopeStack;
import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.eclipse.tm4e.core.registry.IThemeSource.ContentType;
import org.junit.jupiter.api.Test;

class ThemeTypeTest {

	@Test
	void testTMPlistTheme() throws Exception {
		final var rawTheme = RawThemeReader.readTheme(
				IThemeSource.fromFile(Path.of("../org.eclipse.tm4e.core.tests/src/main/resources/test-cases/themes/QuietLight.tmTheme")));

		assertThat(rawTheme.getName()).isEqualTo("Quiet Light");
		final var theme = Theme.createFromRawTheme(rawTheme, null);
		final var colors = theme.getColorMap();

		final var editorColors = rawTheme.getEditorColors();
		assertThat(editorColors.get("background")).isEqualTo("#F5F5F5");
		assertThat(editorColors.get("caret")).isEqualTo("#000000");
		assertThat(editorColors.get("foreground")).isEqualTo("#333333");
		assertThat(editorColors.get("invisibles")).isEqualTo("#AAAAAA");
		assertThat(editorColors.get("lineHighlight")).isEqualTo("#E4F6D4");
		assertThat(editorColors.get("selection")).isEqualTo("#C9D0D9");

		var attrs = castNonNull(theme.match(ScopeStack.from()));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#333333");
		assertThat(colors.get(attrs.backgroundId)).isEqualTo("#F5F5F5");

		attrs = castNonNull(theme.match(ScopeStack.from("comment")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#AAAAAA");
		assertThat(attrs.fontStyle & FontStyle.Italic).isEqualTo(FontStyle.Italic);

		attrs = castNonNull(theme.match(ScopeStack.from("punctuation.definition.comment")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#AAAAAA");
		assertThat(attrs.fontStyle & FontStyle.Italic).isEqualTo(FontStyle.Italic);

		attrs = castNonNull(theme.match(ScopeStack.from("keyword")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#4B83CD");
		attrs = castNonNull(theme.match(ScopeStack.from("keyword.operator")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#777777");
	}

	@Test
	void testTMJsonTheme() throws Exception {
		final var rawTheme = RawThemeReader.readTheme(
				IThemeSource.fromFile(Path.of("../org.eclipse.tm4e.core.tests/src/main/resources/test-cases/themes/dark_vs.json")));

		assertThat(rawTheme.getName()).isEqualTo("Dark Visual Studio");
		final var theme = Theme.createFromRawTheme(rawTheme, null);
		final var colors = theme.getColorMap();

		final var editorColors = rawTheme.getEditorColors();
		assertThat(editorColors.get("foreground")).isEqualTo("#D4D4D4");
		assertThat(editorColors.get("background")).isEqualTo("#1E1E1E");

		var attrs = castNonNull(theme.match(ScopeStack.from()));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#D4D4D4");
		assertThat(colors.get(attrs.backgroundId)).isEqualTo("#1E1E1E");

		attrs = castNonNull(theme.match(ScopeStack.from("comment")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#608B4E");
		assertThat(attrs.fontStyle & FontStyle.Italic).isEqualTo(FontStyle.Italic);

		attrs = castNonNull(theme.match(ScopeStack.from("keyword")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#569CD6");
		attrs = castNonNull(theme.match(ScopeStack.from("keyword.operator")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#D4D4D4");
		attrs = castNonNull(theme.match(ScopeStack.from("keyword.operator.expression")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#569CD6");
	}

	@Test
	void testVSCodeJsonTheme() throws Exception {
		final var rawTheme = RawThemeReader.readTheme(
				IThemeSource.fromString(ContentType.JSON, """
					{
					  "name": "My theme",
					  "tokenColors": [
					    {
					      "settings": {
					        "foreground": "#ABCDEF",
					        "background": "#012345"
					      }
					    },
					    {
					      "name": "Comment",
					      "scope": "comment",
					      "settings": {
					        "fontStyle": "italic",
					        "foreground": "#FF0000"
					      }
					    },
					    {
					      "name": "Keyword",
					      "scope": "keyword",
					      "settings": {
					        "foreground": "#00FF00"
					      }
					    }
					  ],
					  "colors": {
					    "editor.foreground": "#FFFFFF",
					    "editor.background": "#000000",
					    "editor.selectionForeground": "#EEEEEE",
					    "editor.selectionBackground": "#333333",
					    "editor.lineHighlightBackground": "#999999"
					  },
					  "semanticHighlighting": true
					}
					"""));

		assertThat(rawTheme.getName()).isEqualTo("My theme");
		final var theme = Theme.createFromRawTheme(rawTheme, null);
		final var colors = theme.getColorMap();

		final var editorColors = rawTheme.getEditorColors();
		assertThat(editorColors.get("editor.foreground")).isEqualTo("#FFFFFF");
		assertThat(editorColors.get("editor.background")).isEqualTo("#000000");

		var attrs = castNonNull(theme.match(ScopeStack.from()));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#ABCDEF");
		assertThat(colors.get(attrs.backgroundId)).isEqualTo("#012345");

		attrs = castNonNull(theme.match(ScopeStack.from("comment")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#FF0000");
		assertThat(attrs.fontStyle & FontStyle.Italic).isEqualTo(FontStyle.Italic);

		attrs = castNonNull(theme.match(ScopeStack.from("keyword.something")));
		assertThat(colors.get(attrs.foregroundId)).isEqualTo("#00FF00");
	}
}
