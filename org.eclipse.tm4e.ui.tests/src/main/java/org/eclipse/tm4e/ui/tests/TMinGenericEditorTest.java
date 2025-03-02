/**
 * Copyright (c) 2015, 2022 Angelo ZERR and others.
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

import java.io.FileOutputStream;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TMinGenericEditorTest {

	private IEditorDescriptor genericEditorDescr;
	private IEditorPart editor;
	private IEditorPart clonedEditor;

	@BeforeEach
	public void setup() throws Exception {
		genericEditorDescr = TestUtils.assertHasGenericEditor();
		TestUtils.assertNoTM4EThreadsRunning();
	}

	@AfterEach
	public void tearDown() throws Exception {
		TestUtils.closeEditor(editor);
		editor = null;

		TestUtils.closeEditor(clonedEditor);
		clonedEditor = null;

		TestUtils.assertNoTM4EThreadsRunning();
	}

	@Test
	void testTMHighlightInGenericEditor() throws Exception {
		final var f = TestUtils.createTempFile(".ts");
		try (var fileOutputStream = new FileOutputStream(f)) {
			fileOutputStream.write("let a = '';\nlet b = 10;\nlet c = true;".getBytes());
		}
		editor = IDE.openEditor(UI.getActivePage(), f.toURI(), genericEditorDescr.getId(), true);

		final var text = (StyledText) editor.getAdapter(Control.class);
		TestUtils.waitForAndAssertCondition(3_000, () -> text.getStyleRanges().length > 1);
	}

	@Test
	void testTMHighlightInGenericEditorEdit() throws Exception {
		final var f = TestUtils.createTempFile(".ts");
		try (var fileOutputStream = new FileOutputStream(f)) {
			fileOutputStream.write("let a = '';".getBytes());
		}
		editor = IDE.openEditor(UI.getActivePage(), f.toURI(), genericEditorDescr.getId(), true);

		final var text = (StyledText) editor.getAdapter(Control.class);
		TestUtils.waitForAndAssertCondition(3_000, () -> text.getStyleRanges().length > 1);

		final int initialNumberOfRanges = text.getStyleRanges().length;
		text.setText("let a = '';\nlet b = 10;\nlet c = true;");
		TestUtils.waitForAndAssertCondition("More styles should have been added", 3_000,
				() -> text.getStyleRanges().length > initialNumberOfRanges + 3);
	}

	@Test
	void testTMHighlightInClonedGenericEditor() throws Exception {
		final var f = TestUtils.createTempFile(".ts");
		try (var fileOutputStream = new FileOutputStream(f)) {
			fileOutputStream.write("let a = '';".getBytes());
		}
		editor = IDE.openEditor(UI.getActivePage(), f.toURI(), genericEditorDescr.getId(), true);
		clonedEditor = UI.getActivePage().openEditor(editor.getEditorInput(), genericEditorDescr.getId(), true, IWorkbenchPage.MATCH_NONE);

		final var text = (StyledText) clonedEditor.getAdapter(Control.class);
		TestUtils.waitForAndAssertCondition(3_000, () -> text.getStyleRanges().length > 1);
	}

	@Test
	void testReconcilierStartsAndDisposeThread() throws Exception {
		testTMHighlightInGenericEditor();
		editor.getEditorSite().getPage().closeEditor(editor, false);
		TestUtils.assertNoTM4EThreadsRunning();
	}
}
