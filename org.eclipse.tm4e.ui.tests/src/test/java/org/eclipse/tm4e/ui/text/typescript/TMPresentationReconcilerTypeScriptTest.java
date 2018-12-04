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
package org.eclipse.tm4e.ui.text.typescript;

import java.util.List;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.ui.text.ICommand;
import org.eclipse.tm4e.ui.text.TMEditor;
import org.eclipse.tm4e.ui.themes.ITokenProvider;
import org.eclipse.tm4e.ui.themes.css.CSSTokenProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TMPresentationReconcilerTypeScriptTest {

	@Ignore ("Remove this annotation when org.eclipse.swt.SWTError: No more handles [gtk_init_check() failed] will be fixed")
	@Test
	public void colorizeTypescript() throws Exception {

		TMEditor editor = new TMEditor(getGrammar(), getTokenProvider(), "let a = '';\nlet b = 10;\nlet c = true;");
		List<ICommand> commands = editor.execute();

		Assert.assertEquals(1, commands.size());
		ICommand command = commands.get(0);

		Assert.assertEquals(
				"["
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
				+ "]",
				command.getStyleRanges());
	}

	@Ignore ("Remove this annotation when org.eclipse.swt.SWTError: No more handles [gtk_init_check() failed] will be fixed")
	@Test
	public void colorizeTypescriptWithInvalidate1() throws Exception {

		TMEditor editor = new TMEditor(getGrammar(), getTokenProvider(), "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(0, 3);
		List<ICommand> commands = editor.execute();
		
		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		ICommand command0 = commands.get(0);
		Assert.assertEquals(
				"["
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
				+ "]",
				command0.getStyleRanges());
		
		// viewer.invalidateTextPresentation(0, 3);
		ICommand command1 = commands.get(1);
		Assert.assertEquals(
				"["
				+ "StyleRange {0, 3, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"				
				+ "]",
				command1.getStyleRanges());
		
	}

	@Ignore
	@Test
	public void colorizeTypescriptWithInvalidate2() throws Exception {

		TMEditor editor = new TMEditor(getGrammar(), getTokenProvider(), "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(0, 2);
		List<ICommand> commands = editor.execute();
		
		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		ICommand command0 = commands.get(0);
		Assert.assertEquals(
				"["
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
				+ "]",
				command0.getStyleRanges());
		
		// viewer.invalidateTextPresentation(0, 2);
		ICommand command1 = commands.get(1);
		Assert.assertEquals(
				"["
				+ "StyleRange {0, 2, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"				
				+ "]",
				command1.getStyleRanges());
		
	}

	@Ignore ("Remove this annotation when org.eclipse.swt.SWTError: No more handles [gtk_init_check() failed] will be fixed")
	@Test
	public void colorizeTypescriptWithInvalidate3() throws Exception {

		TMEditor editor = new TMEditor(getGrammar(), getTokenProvider(), "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(1, 2);
		List<ICommand> commands = editor.execute();
		
		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		ICommand command0 = commands.get(0);
		Assert.assertEquals(
				"["
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
				+ "]",
				command0.getStyleRanges());
		
		// viewer.invalidateTextPresentation(1, 2);
		ICommand command1 = commands.get(1);
		Assert.assertEquals(
				"["
				+ "StyleRange {1, 2, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"				
				+ "]",
				command1.getStyleRanges());
		
	}

	@Ignore
	@Test
	public void colorizeTypescriptWithInvalidate4() throws Exception {

		TMEditor editor = new TMEditor(getGrammar(), getTokenProvider(), "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(1, 1);
		List<ICommand> commands = editor.execute();
		
		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		ICommand command0 = commands.get(0);
		Assert.assertEquals(
				"["
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
				+ "]",
				command0.getStyleRanges());
		
		// viewer.invalidateTextPresentation(1, 1);
		ICommand command1 = commands.get(1);
		Assert.assertEquals(
				"["
				+ "StyleRange {1, 1, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"				
				+ "]",
				command1.getStyleRanges());
		
	}
	
	@Ignore
	@Test
	public void colorizeTypescriptWithInvalidate8() throws Exception {

		TMEditor editor = new TMEditor(getGrammar(), getTokenProvider(), "let a = '';\nlet b = 10;\nlet c = true;");
		editor.invalidateTextPresentation(1, 8);
		List<ICommand> commands = editor.execute();
		
		// document.set("let a = '';\nlet b = 10;\nlet c = true;");
		ICommand command0 = commands.get(0);
		Assert.assertEquals(
				"["
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
				+ "]",
				command0.getStyleRanges());
		
		// viewer.invalidateTextPresentation(1, 8);
		ICommand command1 = commands.get(1);
		Assert.assertEquals(
				"["
				+ "StyleRange {1, 2, fontStyle=bold, foreground=Color {7, 54, 66, 255}}"				
				+ "]",
				command1.getStyleRanges());
		
	}
	
	@Ignore ("Remove this annotation when org.eclipse.swt.SWTError: No more handles [gtk_init_check() failed] will be fixed")
	@Test
	public void colorizeTypescriptWithInvalidateAndSeveralLines() throws Exception {
		
		TMEditor editor = new TMEditor(getGrammar(), getTokenProvider(), "a\r\n\r\nb");
		editor.invalidateTextPresentation(0, 6);
		
		List<ICommand> commands = editor.execute();

		Assert.assertEquals(2, commands.size());
		
		for (ICommand command : commands) {
			Assert.assertEquals(
					"["
					+ "StyleRange {0, 3, fontStyle=normal, foreground=Color {38, 139, 210, 255}}, "
					+ "StyleRange {3, 2, fontStyle=normal}, "
					+ "StyleRange {5, 1, fontStyle=normal, foreground=Color {38, 139, 210, 255}}"
					+ "]",
					command.getStyleRanges());			
		}		
	}

	private static ITokenProvider getTokenProvider() {
		return new CSSTokenProvider(TMEditor.class.getResourceAsStream("Solarized-light.css"));
	}

	private static IGrammar getGrammar() {
		Registry registry = new Registry();
		try {
			return registry.loadGrammarFromPathSync("TypeScript.tmLanguage.json",
					TMPresentationReconcilerTypeScriptTest.class.getResourceAsStream("TypeScript.tmLanguage.json"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}