/**
 * Copyright (c) 2019 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mickael Istria (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.assertj.core.api.Assertions.assertThat;

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

public class TestAutoClosing {

	@AfterEach
	public void tearDown() throws Exception {
		UI.getActivePage().closeAllEditors(false);
		for (final IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, null);
		}
	}

	@Test
	public void testAutoClose() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("test.lc-test");
		file.create(new ByteArrayInputStream(new byte[0]), true, null);
		final ITextEditor editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final StyledText text = (StyledText) editor.getAdapter(Control.class);

		// insert closing
		text.setText("");
		text.replaceTextRange(0, 0, "(");
		assertThat(text.getText()).isEqualTo("()");
		assertThat(text.getCaretOffset()).isEqualTo(1);

		// nested insert closing
		text.setText("foo(String::from)");
		text.replaceTextRange(16, 0, "(");
		assertThat(text.getText()).isEqualTo("foo(String::from())");
		assertThat(text.getCaretOffset()).isEqualTo(17);

		// ignore already opened
		text.setText("()");
		text.replaceTextRange(0, 0, "(");
		assertThat(text.getText()).isEqualTo("()");
		assertThat(text.getCaretOffset()).isEqualTo(1);

		// ignore already closed
		text.setText("()");
		text.replaceTextRange(1, 0, ")");
		assertThat(text.getText()).isEqualTo("()");
		assertThat(text.getCaretOffset()).isEqualTo(2);

		// extra closing
		text.setText("()");
		text.replaceTextRange(2, 0, ")");
		assertThat(text.getText()).isEqualTo("())");

		// double quotes
		text.setText("");
		text.replaceTextRange(0, 0, "\"");
		assertThat(text.getText()).isEqualTo("\"\"");
		assertThat(text.getCaretOffset()).isEqualTo(1);

		// continued
		text.replaceTextRange(1, 0, "\"");
		assertThat(text.getText()).isEqualTo("\"\"");
		assertThat(text.getCaretOffset()).isEqualTo(2);

		// continued
		text.replaceTextRange(2, 0, "\"");
		assertThat(text.getText()).isEqualTo("\"\"\"\"");
		assertThat(text.getCaretOffset()).isEqualTo(3);
	}
}
