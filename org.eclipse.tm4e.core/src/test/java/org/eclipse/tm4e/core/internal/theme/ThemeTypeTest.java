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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

		assertEquals("Quiet Light", rawTheme.getName());
		final var theme = Theme.createFromRawTheme(rawTheme, null);
		final var colors = theme.getColorMap();

		final var editorColors = rawTheme.getEditorColors();
		assertEquals("#F5F5F5", editorColors.get("background"));
		assertEquals("#000000", editorColors.get("caret"));
		assertEquals("#333333", editorColors.get("foreground"));
		assertEquals("#AAAAAA", editorColors.get("invisibles"));
		assertEquals("#E4F6D4", editorColors.get("lineHighlight"));
		assertEquals("#C9D0D9", editorColors.get("selection"));

		var attrs = castNonNull(theme.match(ScopeStack.from()));
		assertEquals("#333333", colors.get(attrs.foregroundId));
		assertEquals("#F5F5F5", colors.get(attrs.backgroundId));

		attrs = castNonNull(theme.match(ScopeStack.from("comment")));
		assertEquals("#AAAAAA", colors.get(attrs.foregroundId));
		assertEquals(FontStyle.Italic, attrs.fontStyle & FontStyle.Italic);

		attrs = castNonNull(theme.match(ScopeStack.from("punctuation.definition.comment")));
		assertEquals("#AAAAAA", colors.get(attrs.foregroundId));
		assertEquals(FontStyle.Italic, attrs.fontStyle & FontStyle.Italic);

		attrs = castNonNull(theme.match(ScopeStack.from("keyword")));
		assertEquals("#4B83CD", colors.get(attrs.foregroundId));
		attrs = castNonNull(theme.match(ScopeStack.from("keyword.operator")));
		assertEquals("#777777", colors.get(attrs.foregroundId));
	}

	@Test
	void testTMJsonTheme() throws Exception {
		final var rawTheme = RawThemeReader.readTheme(
				IThemeSource.fromFile(Path.of("../org.eclipse.tm4e.core.tests/src/main/resources/test-cases/themes/dark_vs.json")));

		assertEquals("Dark Visual Studio", rawTheme.getName());
		final var theme = Theme.createFromRawTheme(rawTheme, null);
		final var colors = theme.getColorMap();

		final var editorColors = rawTheme.getEditorColors();
		assertEquals("#D4D4D4", editorColors.get("foreground"));
		assertEquals("#1E1E1E", editorColors.get("background"));

		var attrs = castNonNull(theme.match(ScopeStack.from()));
		assertEquals("#D4D4D4", colors.get(attrs.foregroundId));
		assertEquals("#1E1E1E", colors.get(attrs.backgroundId));

		attrs = castNonNull(theme.match(ScopeStack.from("comment")));
		assertEquals("#608B4E", colors.get(attrs.foregroundId));
		assertEquals(FontStyle.Italic, attrs.fontStyle & FontStyle.Italic);

		attrs = castNonNull(theme.match(ScopeStack.from("keyword")));
		assertEquals("#569CD6", colors.get(attrs.foregroundId));
		attrs = castNonNull(theme.match(ScopeStack.from("keyword.operator")));
		assertEquals("#D4D4D4", colors.get(attrs.foregroundId));
		attrs = castNonNull(theme.match(ScopeStack.from("keyword.operator.expression")));
		assertEquals("#569CD6", colors.get(attrs.foregroundId));
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

		assertEquals("My theme", rawTheme.getName());
		final var theme = Theme.createFromRawTheme(rawTheme, null);
		final var colors = theme.getColorMap();

		final var editorColors = rawTheme.getEditorColors();
		assertEquals("#FFFFFF", editorColors.get("editor.foreground"));
		assertEquals("#000000", editorColors.get("editor.background"));

		var attrs = castNonNull(theme.match(ScopeStack.from()));
		assertEquals("#ABCDEF", colors.get(attrs.foregroundId));
		assertEquals("#012345", colors.get(attrs.backgroundId));

		attrs = castNonNull(theme.match(ScopeStack.from("comment")));
		assertEquals("#FF0000", colors.get(attrs.foregroundId));
		assertEquals(FontStyle.Italic, attrs.fontStyle & FontStyle.Italic);

		attrs = castNonNull(theme.match(ScopeStack.from("keyword.something")));
		assertEquals("#00FF00", colors.get(attrs.foregroundId));
	}
}
