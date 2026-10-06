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
package org.eclipse.tm4e.core.internal.theme.css;

import static org.assertj.core.api.Assertions.*;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.core.theme.css.CSSParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CSSParserTest {

	private static final Path TEST_THEME = Path.of("src/main/resources/css/test-theme.css");

	@Test
	void testCSSParser() throws Exception {
		final var parser = new CSSParser("""
			.invalid { background-color: rgb(255,128,128); }
			.storage.invalid { background-color: red; }
			""");

		assertThat(parser.getBestStyle("undefined")).isNull();
		assertThat(parser.getBestStyle("invalid").getBackgroundColor()).isEqualTo(new RGB(255, 128, 128));
		assertThat(parser.getBestStyle("storage", "invalid").getBackgroundColor()).isEqualTo(new RGB(255, 0, 0));
		assertThat(parser.getBestStyle("storage", "modifier", "invalid", "deprecated").getBackgroundColor()).isEqualTo(new RGB(255, 0, 0));
	}

	/**
	 * Verifies that each scope listed in test-theme.css.result resolves to the style recorded next to it.
	 */
	@Test
	void testTestTheme() throws Exception {
		final CSSParser parser;
		try (var in = new FileInputStream(TEST_THEME.toFile())) {
			parser = new CSSParser(in);
		}

		final List<String> expected = Files.readAllLines(Path.of(TEST_THEME + ".result"));
		final var actual = new ArrayList<String>();
		for (final String line : expected) {
			final String scope = line.substring(0, line.indexOf(" -> "));
			actual.add(scope + " -> " + describe(parser.getBestStyle(scope.split("\\."))));
		}
		assertThat(actual).containsExactlyElementsOf(expected);
	}

	/**
	 * Verifies that a stylesheet with a selector or color the theme model cannot represent fails as a whole.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"a:link { color: red; }",
			"#id { color: red; }",
			".keyword[lang] { color: red; }",
			".a > .b { color: red; }",
			".a .b { color: red; }",
			".a { color: unknowncolor; }" })
	void testUnsupportedStylesheetIsRejected(final String css) {
		assertThatThrownBy(() -> new CSSParser(".before { color: red; }\n" + css)).isInstanceOf(Exception.class);
	}

	private static String describe(final IStyle style) {
		if (style == null)
			return "none";
		return "color=" + style.getColor()
				+ " background=" + style.getBackgroundColor()
				+ (style.isBold() ? " bold" : "")
				+ (style.isItalic() ? " italic" : "")
				+ (style.isUnderline() ? " underline" : "")
				+ (style.isStrikeThrough() ? " strikethrough" : "");
	}
}
