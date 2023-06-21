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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ThemeIdConstants;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TMEditorColorTest implements ThemeIdConstants {

	private static final String EDITOR_CURRENTLINE_HIGHLIGHT = "currentLineColor";

	private IThemeManager manager;
	private IEditorDescriptor editorDescriptor;
	private File f;
	private IEditorPart editor;

	@BeforeEach
	public void init() {

		editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry()
				.findEditor("org.eclipse.ui.genericeditor.GenericEditor");
		assumeTrue(editorDescriptor != null);

		manager = TMUIPlugin.getThemeManager();

	}

	@AfterEach
	public void tearDown() {
		manager = null;
		editor.getEditorSite().getPage().closeEditor(editor, false);
		editor = null;
		f.delete();
		f = null;
	}

	@Test
	void systemDefaultEditorColorTest() throws IOException, PartInitException {
		f = File.createTempFile("test" + System.currentTimeMillis(), ".ts");

		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), f.toURI(),
				editorDescriptor.getId(), true);

		final StyledText styledText = (StyledText) editor.getAdapter(Control.class);

		final String themeId = manager.getDefaultTheme().getId();
		final ITheme theme = manager.getThemeById(themeId);
		assertEquals(SolarizedLight, themeId, "Default light theme isn't set");
		assertEquals(theme.getEditorBackground(), styledText.getBackground(), "Background colors isn't equals");
		assertEquals(theme.getEditorForeground(), styledText.getForeground(), "Foreground colors isn't equals");
		assertNull(theme.getEditorSelectionBackground(), "System default selection background should be null");
		assertNull(theme.getEditorSelectionForeground(), "System default selection foreground should be null");

		final Color lineHighlight = ColorManager.getInstance().getPreferenceEditorColor(EDITOR_CURRENTLINE_HIGHLIGHT);
		assertNotNull(theme.getEditorCurrentLineHighlight(), "Highlight shouldn't be a null");
		assertNotEquals(lineHighlight, theme.getEditorCurrentLineHighlight(),
				"Default Line highlight should be from TM theme");

	}

	@Test
	void userDefinedEditorColorTest() throws Exception {
		final var testColorVal = "255,128,0";
		final var testColor = new Color(UI.getDisplay(), 255, 128, 0);
		final var prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.ui.editors");
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, testColorVal);
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);

		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND, testColorVal);
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT, false);

		f = File.createTempFile("test" + System.currentTimeMillis(), ".ts");

		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), f.toURI(),
				editorDescriptor.getId(), true);

		final StyledText styledText = (StyledText) editor.getAdapter(Control.class);

		final String themeId = manager.getDefaultTheme().getId();
		final ITheme theme = manager.getThemeById(themeId);
		assertEquals(SolarizedLight, themeId, "Default light theme isn't set");

		assertEquals(styledText.getBackground(), testColor, "Background color should be user defined");
		assertEquals(theme.getEditorForeground(), styledText.getForeground(), "Foreground colors should be ");
		assertEquals(theme.getEditorSelectionBackground(), testColor,
				"Selection background color should be user defined");
		assertNull(theme.getEditorSelectionForeground(), "Selection foreground should be System default (null)");

		final Color lineHighlight = ColorManager.getInstance().getPreferenceEditorColor(EDITOR_CURRENTLINE_HIGHLIGHT);
		assertNotNull(lineHighlight, "Highlight shouldn't be a null");
		assertEquals(lineHighlight, theme.getEditorCurrentLineHighlight(),
				"Line highlight should be from preferences (because of user defined background)");
	}

}
