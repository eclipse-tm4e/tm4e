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

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.tm4e.core.registry.IThemeSource.ContentType;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.junit.jupiter.api.Test;

class TMTokenProviderTest {

	private final ColorManager colors = ColorManager.getInstance();

	@Test
	void testTMPlistTheme() throws Exception {
		try (var in = Files
				.newInputStream(Path.of("../org.eclipse.tm4e.core.tests/src/main/resources/test-cases/themes/QuietLight.tmTheme"))) {
			final var theme = new TMThemeTokenProvider(ContentType.XML, in);

			assertEquals(colors.getColor(RGB.fromHex("#333333")), theme.getEditorForeground());
			assertEquals(colors.getColor(RGB.fromHex("#F5F5F5")), theme.getEditorBackground());
			assertEquals(colors.getColor(RGB.fromHex("#E4F6D4")), theme.getEditorCurrentLineHighlight());
			assertEquals(null, theme.getEditorSelectionForeground());
			assertEquals(colors.getColor(RGB.fromHex("#C9D0D9")), theme.getEditorSelectionBackground());

			if (theme.getToken("comment").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#AAAAAA")), attrs.getForeground());
				assertEquals(SWT.ITALIC, attrs.getStyle() | SWT.ITALIC);
			} else {
				fail();
			}

			if (theme.getToken("punctuation.definition.comment").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#AAAAAA")), attrs.getForeground());
				assertEquals(SWT.ITALIC, attrs.getStyle() | SWT.ITALIC);
			} else {
				fail();
			}

			if (theme.getToken("keyword").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#4B83CD")), attrs.getForeground());
			} else {
				fail();
			}

			if (theme.getToken("keyword.operator").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#777777")), attrs.getForeground());
			} else {
				fail();
			}
		}
	}

	@Test
	void testTMJsonTheme() throws Exception {
		try (var in = Files
				.newInputStream(Path.of("../org.eclipse.tm4e.core.tests/src/main/resources/test-cases/themes/dark_vs.json"))) {
			final var theme = new TMThemeTokenProvider(ContentType.JSON, in);

			assertEquals(colors.getColor(RGB.fromHex("#D4D4D4")), theme.getEditorForeground());
			assertEquals(colors.getColor(RGB.fromHex("#1E1E1E")), theme.getEditorBackground());
			assertEquals(null, theme.getEditorCurrentLineHighlight());
			assertEquals(null, theme.getEditorSelectionForeground());
			assertEquals(null, theme.getEditorSelectionBackground());

			if (theme.getToken("comment").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#608B4E")), attrs.getForeground());
				assertEquals(SWT.ITALIC, attrs.getStyle() | SWT.ITALIC);
			} else {
				fail();
			}

			if (theme.getToken("keyword").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#569CD6")), attrs.getForeground());
			} else {
				fail();
			}

			if (theme.getToken("keyword.operator").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#D4D4D4")), attrs.getForeground());
			} else {
				fail();
			}

			if (theme.getToken("keyword.operator.expression").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#569CD6")), attrs.getForeground());
			} else {
				fail();
			}
		}
	}

	@Test
	void testVSCodeJsonTheme() throws Exception {
		try (var in = new ByteArrayInputStream("""
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
			""".getBytes())) {
			final var theme = new TMThemeTokenProvider(ContentType.JSON, in);

			assertEquals(colors.getColor(RGB.fromHex("#FFFFFF")), theme.getEditorForeground());
			assertEquals(colors.getColor(RGB.fromHex("#000000")), theme.getEditorBackground());
			assertEquals(colors.getColor(RGB.fromHex("#999999")), theme.getEditorCurrentLineHighlight());
			assertEquals(colors.getColor(RGB.fromHex("#EEEEEE")), theme.getEditorSelectionForeground());

			assertEquals(colors.getColor(RGB.fromHex("#333333")), theme.getEditorSelectionBackground());
			if (theme.getToken("comment").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#FF0000")), attrs.getForeground());
				assertEquals(SWT.ITALIC, attrs.getStyle() | SWT.ITALIC);
			} else {
				fail();
			}

			if (theme.getToken("keyword").getData() instanceof TextAttribute attrs) {
				assertEquals(colors.getColor(RGB.fromHex("#00FF00")), attrs.getForeground());
			} else {
				fail();
			}
		}
	}
}
