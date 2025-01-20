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
package org.eclipse.tm4e.ui.tests.themes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ThemeIdConstants;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TMEditorColorTest implements ThemeIdConstants {

	private static final String EDITOR_CURRENTLINE_HIGHLIGHT = "currentLineColor";

	private final IThemeManager manager = TMUIPlugin.getThemeManager();

	private IEditorDescriptor genericEditorDescr;
	private IEditorPart editor;

	@BeforeEach
	public void setup() {
		genericEditorDescr = TestUtils.assertHasGenericEditor();
	}

	@AfterEach
	public void tearDown() {
		TestUtils.closeEditor(editor);
		editor = null;
	}

	@Test
	void systemDefaultEditorColorTest() throws IOException, PartInitException {
		final var f = TestUtils.createTempFile(".ts");

		editor = IDE.openEditor(UI.getActivePage(), f.toURI(), genericEditorDescr.getId(), true);

		final StyledText styledText = (StyledText) editor.getAdapter(Control.class);

		final String themeId = manager.getDefaultTheme().getId();
		final ITheme theme = manager.getThemeById(themeId);

		assertThat(themeId).as("Default light theme isn't set").isEqualTo(SolarizedLight);
		assertThat(styledText.getBackground())
				.as("Background colors aren't equal")
				.isEqualTo(theme.getEditorBackground());
		assertThat(styledText.getForeground())
				.as("Foreground colors aren't equal")
				.isEqualTo(theme.getEditorForeground());
		assertThat(theme.getEditorSelectionBackground())
				.as("System default selection background should be null")
				.isNull();
		assertThat(theme.getEditorSelectionForeground())
				.as("System default selection foreground should be null")
				.isNull();

		final Color eclipseLineHighlight = ColorManager.getInstance()
				.getPreferenceEditorColor(EDITOR_CURRENTLINE_HIGHLIGHT);
		final Color themeLineHighlight = theme.getEditorCurrentLineHighlight();

		assertThat(themeLineHighlight).as("Highlight shouldn't be null").isNotNull();
		assertThat(themeLineHighlight)
				.as("Default Line highlight should be from TM theme")
				.isNotEqualTo(eclipseLineHighlight);
	}

	@Test
	void userDefinedEditorColorTest() throws Exception {
		final var testColorVal = "255,128,0";
		final var testColor = new Color(Display.getCurrent(), 255, 128, 0);

		final var prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.ui.editors");
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, testColorVal);
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND, testColorVal);
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT, false);

		final var f = TestUtils.createTempFile(".ts");

		editor = IDE.openEditor(UI.getActivePage(), f.toURI(), genericEditorDescr.getId(), true);

		final StyledText styledText = (StyledText) editor.getAdapter(Control.class);

		final String themeId = manager.getDefaultTheme().getId();
		final ITheme theme = manager.getThemeById(themeId);

		assertThat(themeId).as("Default light theme isn't set").isEqualTo(SolarizedLight);

		assertThat(styledText.getBackground())
				.as("Background color should be user defined")
				.isEqualTo(testColor);
		assertThat(styledText.getForeground())
				.as("Foreground colors should be user defined")
				.isEqualTo(theme.getEditorForeground());
		assertThat(theme.getEditorSelectionBackground())
				.as("Selection background color should be user defined")
				.isEqualTo(testColor);
		assertThat(theme.getEditorSelectionForeground())
				.as("Selection foreground should be system default (null)")
				.isNull();

		final Color lineHighlight = ColorManager.getInstance()
				.getPreferenceEditorColor(EDITOR_CURRENTLINE_HIGHLIGHT);

		assertThat(lineHighlight).as("Highlight shouldn't be null").isNotNull();
		assertThat(lineHighlight)
				.as("Line highlight should be from preferences (because of user-defined background)")
				.isEqualTo(theme.getEditorCurrentLineHighlight());
	}
}
