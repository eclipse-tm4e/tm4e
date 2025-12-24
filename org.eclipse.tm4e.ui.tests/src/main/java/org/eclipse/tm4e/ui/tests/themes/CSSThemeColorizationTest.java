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
package org.eclipse.tm4e.ui.tests.themes;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.ui.tests.support.TMEditor;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.tm4e.ui.themes.css.CSSTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CSSThemeColorizationTest {

	private static final String SAMPLE_TEXT = "let a = '';\nlet b = 10;\nlet c = true;";

	private IGrammar grammar;
	private TMEditor editor;

	@BeforeEach
	void setup() throws Exception {
		TestUtils.assertNoTM4EThreadsRunning();
		grammar = new Registry().addGrammar(IGrammarSource.fromResource(getClass(), "/grammars/TypeScript.tmLanguage.json"));
	}

	@AfterEach
	void tearDown() throws Exception {
		if (editor != null) {
			editor.dispose();
			editor = null;
		}
		TestUtils.assertNoTM4EThreadsRunning();
	}

	private static void assertStyleRange(
			final String styleRanges,
			final int offset,
			final int length,
			final String expectedFontStyle,
			final String expectedForegroundColor) {
		assertThat(styleRanges).contains(
				"StyleRange {" + offset + ", " + length + ", fontStyle=" + expectedFontStyle + ", foreground=Color {"
						+ expectedForegroundColor + ", 255}}");
	}

	@Test
	void darkCssThemeColorsAreApplied() throws Exception {
		try (var in = Files.newInputStream(Path.of("../org.eclipse.tm4e.ui/themes/Dark.css"))) {
			final var theme = new CSSTokenProvider(in);
			editor = new TMEditor(grammar, theme, SAMPLE_TEXT);

			final var commands = editor.execute();
			assertThat(commands).hasSize(1);
			final String ranges = commands.get(0).getStyleRanges();

			// let -> storage (matches .storage / .storage.type)
			assertStyleRange(ranges, 0, 3, "normal", "86, 156, 214");
			// '' -> string
			assertStyleRange(ranges, 8, 2, "normal", "206, 145, 120");
			// 10 -> constant.numeric
			assertStyleRange(ranges, 20, 2, "normal", "181, 206, 168");
			// true -> constant.language
			assertStyleRange(ranges, 32, 4, "normal", "86, 156, 214");
		}
	}

	@Test
	void monokaiCssThemeColorsAreApplied() throws Exception {
		try (var in = Files.newInputStream(Path.of("../org.eclipse.tm4e.ui/themes/Monokai.css"))) {
			final var theme = new CSSTokenProvider(in);
			editor = new TMEditor(grammar, theme, SAMPLE_TEXT);

			final var commands = editor.execute();
			assertThat(commands).hasSize(1);
			final String ranges = commands.get(0).getStyleRanges();

			// let -> storage.type
			assertStyleRange(ranges, 0, 3, "italic", "102, 217, 239");
			// '' -> string
			assertStyleRange(ranges, 8, 2, "normal", "230, 219, 116");
			// 10 -> constant.numeric
			assertStyleRange(ranges, 20, 2, "normal", "174, 129, 255");
			// true -> constant.language
			assertStyleRange(ranges, 32, 4, "normal", "174, 129, 255");
		}
	}
}
