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
package org.eclipse.tm4e.ui.themes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class TMEditorColorTest implements ThemeIdConstants {

	private static final String EDITOR_CURRENTLINE_HIGHLIGHT = "currentLineColor";

	private IThemeManager manager;
	private IEditorDescriptor editorDescriptor;
	private File f;
	private IEditorPart editor;

	@Before
	public void init() {

		editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry()
				.findEditor("org.eclipse.ui.genericeditor.GenericEditor");
		Assume.assumeNotNull(editorDescriptor);

		manager = TMUIPlugin.getThemeManager();

	}

	@After
	public void tearDown() {
		manager = null;
		editor.getEditorSite().getPage().closeEditor(editor, false);
		editor = null;
		f.delete();
		f = null;
	}

	@Test
	public void systemDefaultEditorColorTest() throws IOException, PartInitException {
		f = File.createTempFile("test" + System.currentTimeMillis(), ".ts");

		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), f.toURI(),
				editorDescriptor.getId(), true);

		StyledText styledText = (StyledText) editor.getAdapter(Control.class);

		String themeId = manager.getDefaultTheme().getId();
		ITheme theme = manager.getThemeById(themeId);
		assertEquals("Default light theme isn't set", themeId, SolarizedLight);
		assertEquals("Background colors isn't equals", theme.getEditorBackground(), styledText.getBackground());
		assertEquals("Foreground colors isn't equals", theme.getEditorForeground(), styledText.getForeground());
		assertNull("System default selection background should be null", theme.getEditorSelectionBackground());
		assertNull("System default selection foreground should be null", theme.getEditorSelectionForeground());

		Color lineHighlight = ColorManager.getInstance()
				.getPreferenceEditorColor(EDITOR_CURRENTLINE_HIGHLIGHT);
		assertNotNull("Highlight shouldn't be a null", theme.getEditorCurrentLineHighlight());
		assertNotEquals("Default Line highlight should be from TM theme", lineHighlight,
				theme.getEditorCurrentLineHighlight());

	}

	@Test
	public void userDefinedEditorColorTest() throws Exception {
		String testColorVal = "255,128,0";
		Color testColor = new Color(Display.getCurrent(), 255, 128, 0);
		IPreferenceStore prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.ui.editors");
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, testColorVal);
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);

		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND, testColorVal);
		prefs.setValue(AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT, false);

		f = File.createTempFile("test" + System.currentTimeMillis(), ".ts");

		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), f.toURI(),
				editorDescriptor.getId(), true);

		StyledText styledText = (StyledText) editor.getAdapter(Control.class);

		String themeId = manager.getDefaultTheme().getId();
		ITheme theme = manager.getThemeById(themeId);
		assertEquals("Default light theme isn't set", themeId, SolarizedLight);

		assertEquals("Background color should be user defined", styledText.getBackground(), testColor);
		assertEquals("Foreground colors should be ", theme.getEditorForeground(), styledText.getForeground());
		assertEquals("Selection background color should be user defined", theme.getEditorSelectionBackground(),
				testColor);
		assertNull("Selection foreground should be System default (null)", theme.getEditorSelectionForeground());

		Color lineHighlight = ColorManager.getInstance()
				.getPreferenceEditorColor(EDITOR_CURRENTLINE_HIGHLIGHT);
		assertNotNull("Highlight shouldn't be a null", lineHighlight);
		assertEquals("Line highlight should be from preferences (because of user defined background)", lineHighlight,
				theme.getEditorCurrentLineHighlight());
	}

}
