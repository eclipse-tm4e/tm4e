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
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TestIndentationRules {
	@AfterEach
	public void tearDown() throws Exception {
		UI.getActivePage().closeAllEditors(false);
		for (final IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, null);
		}
	}

	@Test
	public void testIndentAdjustmentOnPaste() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("whatever.lc-test");
		file.create(new ByteArrayInputStream(new byte[0]), true, null);
		final ITextEditor editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final StyledText text = (StyledText) editor.getAdapter(Control.class);

		// insert an indented code snippet on top level and expect it to be inserted without the leading whitespaces
		text.setText("");
		text.setSelection(0);
		text.insert("   function bar() {\n   }");
		assertEquals("function bar() {\n}", text.getText());

		// insert an unindented code snippet into the body of a class and expect it to be indented
		text.setText("public class Foo {\n\n}");
		text.setSelection(19);
		text.insert("function bar() {\n}");
		assertEquals("public class Foo {\n\tfunction bar() {\n\t}\n}", text.getText());
	}

	@Test
	public void testIndentAdjustmentOnEnter() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("whatever.lc-test");
		file.create(new ByteArrayInputStream(new byte[0]), true, null);
		final ITextEditor editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final StyledText text = (StyledText) editor.getAdapter(Control.class);

		// pressing enter after { should indent the next line since we are inside a class body
		text.setText("public class Foo {\n\n}");
		text.setSelection(18);
		text.insert("\n");
		assertEquals("public class Foo {\n\t\n\n}", text.getText());
	}
}
