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
package org.eclipse.tm4e.ui.tests.internal.themes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.tm4e.core.registry.IThemeSource.ContentType;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.ui.internal.themes.TMThemeTokenProvider;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.junit.jupiter.api.Test;

class TMTokenProviderTest {

	private final ColorManager colors = ColorManager.getInstance();

	@Test
	void testTMPlistTheme() throws Exception {
		try (var in = Files
				.newInputStream(Path.of("../org.eclipse.tm4e.core.tests/src/main/resources/test-cases/themes/QuietLight.tmTheme"))) {
			final var theme = new TMThemeTokenProvider(ContentType.XML, in);

			assertThat(theme.getEditorForeground()).isEqualTo(colors.getColor(RGB.fromHex("#333333")));
			assertThat(theme.getEditorBackground()).isEqualTo(colors.getColor(RGB.fromHex("#F5F5F5")));
			assertThat(theme.getEditorCurrentLineHighlight()).isEqualTo(colors.getColor(RGB.fromHex("#E4F6D4")));
			assertThat(theme.getEditorSelectionForeground()).isNull();
			assertThat(theme.getEditorSelectionBackground()).isEqualTo(colors.getColor(RGB.fromHex("#C9D0D9")));

			var token = theme.getToken("comment").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			var attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#AAAAAA")));
			assertThat(attrs.getStyle() | SWT.ITALIC).isEqualTo(SWT.ITALIC);

			token = theme.getToken("punctuation.definition.comment").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#AAAAAA")));
			assertThat(attrs.getStyle() | SWT.ITALIC).isEqualTo(SWT.ITALIC);

			token = theme.getToken("keyword").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#4B83CD")));

			token = theme.getToken("keyword.operator").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#777777")));
		}
	}

	@Test
	void testTMJsonTheme() throws Exception {
		try (var in = Files.newInputStream(Path.of("../org.eclipse.tm4e.core.tests/src/main/resources/test-cases/themes/dark_vs.json"))) {
			final var theme = new TMThemeTokenProvider(ContentType.JSON, in);

			assertThat(theme.getEditorForeground()).isEqualTo(colors.getColor(RGB.fromHex("#D4D4D4")));
			assertThat(theme.getEditorBackground()).isEqualTo(colors.getColor(RGB.fromHex("#1E1E1E")));
			assertThat(theme.getEditorCurrentLineHighlight()).isNull();
			assertThat(theme.getEditorSelectionForeground()).isNull();
			assertThat(theme.getEditorSelectionBackground()).isNull();

			var token = theme.getToken("comment").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			var attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#608B4E")));
			assertThat(attrs.getStyle() | SWT.ITALIC).isEqualTo(SWT.ITALIC);

			token = theme.getToken("keyword").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#569CD6")));

			token = theme.getToken("keyword.operator").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#D4D4D4")));

			token = theme.getToken("keyword.operator.expression").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#569CD6")));
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

			assertThat(theme.getEditorForeground()).isEqualTo(colors.getColor(RGB.fromHex("#FFFFFF")));
			assertThat(theme.getEditorBackground()).isEqualTo(colors.getColor(RGB.fromHex("#000000")));
			assertThat(theme.getEditorCurrentLineHighlight()).isEqualTo(colors.getColor(RGB.fromHex("#999999")));
			assertThat(theme.getEditorSelectionForeground()).isEqualTo(colors.getColor(RGB.fromHex("#EEEEEE")));
			assertThat(theme.getEditorSelectionBackground()).isEqualTo(colors.getColor(RGB.fromHex("#333333")));

			var token = theme.getToken("comment").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			var attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#FF0000")));
			assertThat(attrs.getStyle()).isEqualTo(SWT.ITALIC);

			token = theme.getToken("keyword").getData();
			assertThat(token).isInstanceOf(TextAttribute.class);
			attrs = (TextAttribute) token;
			assertThat(attrs.getForeground()).isEqualTo(colors.getColor(RGB.fromHex("#00FF00")));
		}
	}
}
