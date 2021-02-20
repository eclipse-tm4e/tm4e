/**
 * Copyright (c) 2019 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TestAutoClosing {

	@AfterEach
	public void tearDown() throws Exception {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, null);
		}
	}

	@Test
	public void testAutoClose() throws Exception {
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		IFile file = p.getFile("test.lc-test");
		file.create(new ByteArrayInputStream(new byte[0]), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		StyledText text = (StyledText)editor.getAdapter(Control.class);
		// insert closing
		text.setText("");
		text.replaceTextRange(0, 0, "(");
		assertEquals("()", text.getText());
		assertEquals(1, text.getCaretOffset());
		// nested insert closing
		text.setText("foo(String::from)");
		text.replaceTextRange(16, 0, "(");
		assertEquals("foo(String::from())", text.getText());
		assertEquals(17, text.getCaretOffset());
		// ignore already opened
		text.setText("()");
		text.replaceTextRange(0, 0, "(");
		assertEquals("()", text.getText());
		assertEquals(1, text.getCaretOffset());
		// ignore already closed
		text.setText("()");
		text.replaceTextRange(1, 0, ")");
		assertEquals("()", text.getText());
		assertEquals(2, text.getCaretOffset());
		//
		text.setText("()");
		text.replaceTextRange(2, 0, ")");
		assertEquals("())", text.getText());
		//
		text.setText("");
		text.replaceTextRange(0, 0, "\"");
		assertEquals("\"\"", text.getText());
		assertEquals(1, text.getCaretOffset());
		// continued
		text.replaceTextRange(1, 0, "\"");
		assertEquals("\"\"", text.getText());
		assertEquals(2, text.getCaretOffset());
		// continued
		text.replaceTextRange(2, 0, "\"");
		assertEquals("\"\"\"\"", text.getText());
		assertEquals(3, text.getCaretOffset());
	}
}
