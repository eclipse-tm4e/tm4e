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
package org.eclipse.tm4e.ui.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.tests.support.TMEditor;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.tm4e.ui.tests.support.command.ICommand;
import org.eclipse.tm4e.ui.themes.ITokenProvider;
import org.eclipse.tm4e.ui.themes.ThemeIdConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

@DisabledIf("org.eclipse.tm4e.ui.tests.TMPresentationReconcilerTypeScriptTest#isDisabled")
class TMPresentationReconcilerTypeScriptTest {

	static boolean isDisabled() {
		// TODO ignore these tests on GitHub Actions Windows runner because they are extremely slow (4-5min) for an unknown reason
		return TestUtils.isGitHubActions() && System.getProperty("os.name").toLowerCase().contains("windows");
	}

	private IGrammar grammar;
	private ITokenProvider theme;
	private TMEditor editor;

	@BeforeEach
	void setup() throws Exception {
		TestUtils.assertNoTM4EThreadsRunning();

		grammar = new Registry().addGrammar(IGrammarSource.fromResource(getClass(), "/grammars/TypeScript.tmLanguage.json"));
		theme = TMUIPlugin.getThemeManager().getThemeById(ThemeIdConstants.SolarizedLight);
	}

	@AfterEach
	void tearDown() throws InterruptedException {
		if (editor != null)
			editor.dispose();

		TestUtils.assertNoTM4EThreadsRunning();
	}

	@Test
	void colorizeTypescript() {
		editor = new TMEditor(grammar, theme, "let a = '';\nlet b = 10;\nlet c = true;");

		final List<ICommand> commands = editor.execute();
		assertThat(commands).hasSize(1);

		final ICommand command0 = commands.get(0);
		assertThat(command0.getStyleRanges()).isEqualTo("["
				+ "StyleRange {0, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {3, 1, fontStyle=normal}, "
				+ "StyleRange {4, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {5, 1, fontStyle=normal}, "
				+ "StyleRange {6, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {7, 1, fontStyle=normal}, "
				+ "StyleRange {8, 2, fontStyle=normal, foreground=Color {42, 161, 152, 255}}, "
				+ "StyleRange {10, 2, fontStyle=normal}, "
				+ "StyleRange {12, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {15, 1, fontStyle=normal}, "
				+ "StyleRange {16, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {17, 1, fontStyle=normal}, "
				+ "StyleRange {18, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {19, 1, fontStyle=normal}, "
				+ "StyleRange {20, 2, fontStyle=normal, foreground=Color {211, 54, 130, 255}}, "
				+ "StyleRange {22, 2, fontStyle=normal}, "
				+ "StyleRange {24, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {27, 1, fontStyle=normal}, "
				+ "StyleRange {28, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {29, 1, fontStyle=normal}, "
				+ "StyleRange {30, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {31, 1, fontStyle=normal}, "
				+ "StyleRange {32, 4, fontStyle=normal, foreground=Color {181, 137, 0, 255}}, "
				+ "StyleRange {36, 1, fontStyle=normal}"
				+ "]");
	}

	@Test
	void colorizeTypescriptWithInvalidate1() {
		editor = new TMEditor(grammar, theme, "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(0, 3);

		final List<ICommand> commands = editor.execute();
		assertThat(commands).hasSize(2);

		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		final ICommand command0 = commands.get(0);
		assertThat(command0.getStyleRanges()).isEqualTo("["
				+ "StyleRange {0, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {3, 1, fontStyle=normal}, "
				+ "StyleRange {4, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {5, 1, fontStyle=normal}, "
				+ "StyleRange {6, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {7, 1, fontStyle=normal}, "
				+ "StyleRange {8, 2, fontStyle=normal, foreground=Color {42, 161, 152, 255}}, "
				+ "StyleRange {10, 2, fontStyle=normal}, "
				+ "StyleRange {12, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {15, 1, fontStyle=normal}, "
				+ "StyleRange {16, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {17, 1, fontStyle=normal}, "
				+ "StyleRange {18, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {19, 1, fontStyle=normal}, "
				+ "StyleRange {20, 2, fontStyle=normal, foreground=Color {211, 54, 130, 255}}, "
				+ "StyleRange {22, 2, fontStyle=normal}, "
				+ "StyleRange {24, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {27, 1, fontStyle=normal}, "
				+ "StyleRange {28, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {29, 1, fontStyle=normal}, "
				+ "StyleRange {30, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {31, 1, fontStyle=normal}, "
				+ "StyleRange {32, 4, fontStyle=normal, foreground=Color {181, 137, 0, 255}}, "
				+ "StyleRange {36, 1, fontStyle=normal}"
				+ "]");

		// viewer.invalidateTextPresentation(0, 3);
		final ICommand command1 = commands.get(1);
		assertThat(command1.getStyleRanges()).isEqualTo("["
				+ "StyleRange {0, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"
				+ "]");
	}

	@Test
	void colorizeTypescriptWithInvalidate2() {
		editor = new TMEditor(grammar, theme, "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(0, 2);

		final List<ICommand> commands = editor.execute();
		assertThat(commands).hasSize(2);

		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		final ICommand command0 = commands.get(0);
		assertThat(command0.getStyleRanges()).isEqualTo("["
				+ "StyleRange {0, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {3, 1, fontStyle=normal}, "
				+ "StyleRange {4, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {5, 1, fontStyle=normal}, "
				+ "StyleRange {6, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {7, 1, fontStyle=normal}, "
				+ "StyleRange {8, 2, fontStyle=normal, foreground=Color {42, 161, 152, 255}}, "
				+ "StyleRange {10, 2, fontStyle=normal}, "
				+ "StyleRange {12, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {15, 1, fontStyle=normal}, "
				+ "StyleRange {16, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {17, 1, fontStyle=normal}, "
				+ "StyleRange {18, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {19, 1, fontStyle=normal}, "
				+ "StyleRange {20, 2, fontStyle=normal, foreground=Color {211, 54, 130, 255}}, "
				+ "StyleRange {22, 2, fontStyle=normal}, "
				+ "StyleRange {24, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {27, 1, fontStyle=normal}, "
				+ "StyleRange {28, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {29, 1, fontStyle=normal}, "
				+ "StyleRange {30, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {31, 1, fontStyle=normal}, "
				+ "StyleRange {32, 4, fontStyle=normal, foreground=Color {181, 137, 0, 255}}, "
				+ "StyleRange {36, 1, fontStyle=normal}"
				+ "]");

		// viewer.invalidateTextPresentation(0, 2);
		final ICommand command1 = commands.get(1);
		assertThat(command1.getStyleRanges()).isEqualTo("["
				+ "StyleRange {0, 2, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"
				+ "]");
	}

	@Test
	void colorizeTypescriptWithInvalidate3() {
		editor = new TMEditor(grammar, theme, "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(1, 2);

		final List<ICommand> commands = editor.execute();
		assertThat(commands).hasSize(2);

		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		final ICommand command0 = commands.get(0);
		assertThat(command0.getStyleRanges()).isEqualTo("["
				+ "StyleRange {0, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {3, 1, fontStyle=normal}, "
				+ "StyleRange {4, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {5, 1, fontStyle=normal}, "
				+ "StyleRange {6, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {7, 1, fontStyle=normal}, "
				+ "StyleRange {8, 2, fontStyle=normal, foreground=Color {42, 161, 152, 255}}, "
				+ "StyleRange {10, 2, fontStyle=normal}, "
				+ "StyleRange {12, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {15, 1, fontStyle=normal}, "
				+ "StyleRange {16, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {17, 1, fontStyle=normal}, "
				+ "StyleRange {18, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {19, 1, fontStyle=normal}, "
				+ "StyleRange {20, 2, fontStyle=normal, foreground=Color {211, 54, 130, 255}}, "
				+ "StyleRange {22, 2, fontStyle=normal}, "
				+ "StyleRange {24, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {27, 1, fontStyle=normal}, "
				+ "StyleRange {28, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {29, 1, fontStyle=normal}, "
				+ "StyleRange {30, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {31, 1, fontStyle=normal}, "
				+ "StyleRange {32, 4, fontStyle=normal, foreground=Color {181, 137, 0, 255}}, "
				+ "StyleRange {36, 1, fontStyle=normal}"
				+ "]");

		// viewer.invalidateTextPresentation(1, 2);
		final ICommand command1 = commands.get(1);
		assertThat(command1.getStyleRanges()).isEqualTo("["
				+ "StyleRange {1, 2, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"
				+ "]");
	}

	@Test
	void colorizeTypescriptWithInvalidate4() {
		editor = new TMEditor(grammar, theme, "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(1, 1);

		final List<ICommand> commands = editor.execute();
		assertThat(commands).hasSize(2);

		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		final ICommand command0 = commands.get(0);
		assertThat(command0.getStyleRanges()).isEqualTo("["
				+ "StyleRange {0, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {3, 1, fontStyle=normal}, "
				+ "StyleRange {4, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {5, 1, fontStyle=normal}, "
				+ "StyleRange {6, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {7, 1, fontStyle=normal}, "
				+ "StyleRange {8, 2, fontStyle=normal, foreground=Color {42, 161, 152, 255}}, "
				+ "StyleRange {10, 2, fontStyle=normal}, "
				+ "StyleRange {12, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {15, 1, fontStyle=normal}, "
				+ "StyleRange {16, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {17, 1, fontStyle=normal}, "
				+ "StyleRange {18, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {19, 1, fontStyle=normal}, "
				+ "StyleRange {20, 2, fontStyle=normal, foreground=Color {211, 54, 130, 255}}, "
				+ "StyleRange {22, 2, fontStyle=normal}, "
				+ "StyleRange {24, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {27, 1, fontStyle=normal}, "
				+ "StyleRange {28, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {29, 1, fontStyle=normal}, "
				+ "StyleRange {30, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {31, 1, fontStyle=normal}, "
				+ "StyleRange {32, 4, fontStyle=normal, foreground=Color {181, 137, 0, 255}}, "
				+ "StyleRange {36, 1, fontStyle=normal}"
				+ "]");

		// viewer.invalidateTextPresentation(1, 1);
		final ICommand command1 = commands.get(1);
		assertThat(command1.getStyleRanges()).isEqualTo("["
				+ "StyleRange {1, 1, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"
				+ "]");
	}

	@Disabled
	@Test
	void colorizeTypescriptWithInvalidate8() {
		editor = new TMEditor(grammar, theme, "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(1, 8);

		final List<ICommand> commands = editor.execute();
		assertThat(commands).hasSize(2);

		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		final ICommand command0 = commands.get(0);
		assertThat(command0.getStyleRanges()).isEqualTo("["
				+ "StyleRange {0, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {3, 1, fontStyle=normal}, "
				+ "StyleRange {4, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {5, 1, fontStyle=normal}, "
				+ "StyleRange {6, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {7, 1, fontStyle=normal}, "
				+ "StyleRange {8, 2, fontStyle=normal, foreground=Color {42, 161, 152, 255}}, "
				+ "StyleRange {10, 2, fontStyle=normal}, "
				+ "StyleRange {12, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {15, 1, fontStyle=normal}, "
				+ "StyleRange {16, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {17, 1, fontStyle=normal}, "
				+ "StyleRange {18, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {19, 1, fontStyle=normal}, "
				+ "StyleRange {20, 2, fontStyle=normal, foreground=Color {211, 54, 130, 255}}, "
				+ "StyleRange {22, 2, fontStyle=normal}, "
				+ "StyleRange {24, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}, "
				+ "StyleRange {27, 1, fontStyle=normal}, "
				+ "StyleRange {28, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
				+ "StyleRange {29, 1, fontStyle=normal}, "
				+ "StyleRange {30, 1, fontStyle=normal, foreground=Color {133, 153, 0, 255}}, "
				+ "StyleRange {31, 1, fontStyle=normal}, "
				+ "StyleRange {32, 4, fontStyle=normal, foreground=Color {181, 137, 0, 255}}, "
				+ "StyleRange {36, 1, fontStyle=normal}"
				+ "]");

		//viewer.invalidateTextPresentation(1, 8);
		final ICommand command1 = commands.get(1);
		assertThat(command1.getStyleRanges()).isEqualTo("["
				+ "StyleRange {1, 2, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"
				+ "]");
	}

	@Test
	void colorizeTypescriptWithInvalidateAndSeveralLines() {
		editor = new TMEditor(grammar, theme, "a\r\n\r\nb");
		editor.invalidateTextPresentation(0, 6);

		final List<ICommand> commands = editor.execute();
		assertThat(commands).hasSize(2);

		for (final ICommand command : commands) {
			assertThat(command.getStyleRanges()).isEqualTo("["
					+ "StyleRange {0, 3, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
					+ "StyleRange {3, 2, fontStyle=normal}, "
					+ "StyleRange {5, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}"
					+ "]");
		}
	}
}
