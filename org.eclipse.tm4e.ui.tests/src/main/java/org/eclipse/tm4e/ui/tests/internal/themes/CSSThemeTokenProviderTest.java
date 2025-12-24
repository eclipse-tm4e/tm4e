/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 */
package org.eclipse.tm4e.ui.tests.internal.themes;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.eclipse.tm4e.ui.themes.css.CSSTokenProvider;
import org.junit.jupiter.api.Test;

class CSSThemeTokenProviderTest {

	private final ColorManager colors = ColorManager.getInstance();

	private TextAttribute getTextAttribute(final CSSTokenProvider provider, final String tokenType) {
		final var tokenData = provider.getToken(tokenType).getData();
		assertThat(tokenData).isInstanceOf(TextAttribute.class);
		return (TextAttribute) tokenData;
	}

	@Test
	void testBuiltInDarkCssTheme() throws Exception {
		try (var in = Files.newInputStream(Path.of("../org.eclipse.tm4e.ui/themes/Dark.css"))) {
			final var provider = new CSSTokenProvider(in);

			assertThat(provider.getEditorForeground()).isEqualTo(colors.getColor(new RGB(212, 212, 212)));
			assertThat(provider.getEditorBackground()).isEqualTo(colors.getColor(new RGB(30, 30, 30)));
			assertThat(provider.getEditorCurrentLineHighlight()).isEqualTo(colors.getColor(new RGB(40, 40, 40)));

			assertThat(getTextAttribute(provider, "entity.other.attribute-name").getForeground())
					.isEqualTo(colors.getColor(new RGB(156, 220, 254)));

			assertThat(getTextAttribute(provider, "storage.type.java").getForeground())
					.isEqualTo(colors.getColor(new RGB(78, 201, 176)));

			assertThat(provider.getToken("this.selector.does.not.exist").getData()).isNull();
		}
	}

	@Test
	void testBuiltInMonokaiCssTheme() throws Exception {
		try (var in = Files.newInputStream(Path.of("../org.eclipse.tm4e.ui/themes/Monokai.css"))) {
			final var provider = new CSSTokenProvider(in);

			assertThat(provider.getEditorForeground()).isEqualTo(colors.getColor(new RGB(248, 248, 242)));
			assertThat(provider.getEditorBackground()).isEqualTo(colors.getColor(new RGB(39, 40, 34)));

			assertThat(getTextAttribute(provider, "keyword").getForeground())
					.isEqualTo(colors.getColor(new RGB(249, 38, 114)));

			final var storageTypeAttrs = getTextAttribute(provider, "storage.type");
			assertThat(storageTypeAttrs.getForeground()).isEqualTo(colors.getColor(new RGB(102, 217, 239)));
			assertThat(storageTypeAttrs.getStyle() & SWT.ITALIC).isEqualTo(SWT.ITALIC);

			final var inheritedClassAttrs = getTextAttribute(provider, "entity.other.inherited-class.java");
			assertThat(inheritedClassAttrs.getForeground()).isEqualTo(colors.getColor(new RGB(166, 226, 46)));
			assertThat(inheritedClassAttrs.getStyle() & SWT.ITALIC).isEqualTo(SWT.ITALIC);
			assertThat(inheritedClassAttrs.getStyle() & TextAttribute.UNDERLINE).isEqualTo(TextAttribute.UNDERLINE);
		}
	}
}
