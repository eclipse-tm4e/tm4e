/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
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

/**
 * Verifies that inserting whitespace (e.g., Tab-as-spaces) on a blank line is not stripped
 * when indentation rules are active (issue #949).
 */
public class TestWhitespaceInsert {

	@AfterEach
	public void tearDown() throws Exception {
		UI.getActivePage().closeAllEditors(false);
		for (final IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, null);
		}
	}

	@Test
	public void testWhitespaceInsertOnBlankLine() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("whatever.lc-test");
		file.create(new ByteArrayInputStream(new byte[0]), true, null);
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final var text = (StyledText) editor.getAdapter(Control.class);

		// Insert 4 spaces on an empty line: should remain as inserted (not stripped)
		text.setText("");
		text.setSelection(0);
		text.insert("    ");
		assertThat(text.getText()).isEqualTo("    ");

		// Insert 4 more spaces at end of a whitespace-only line: should append (not stripped)
		text.setSelection(text.getCharCount());
		text.insert("    ");
		assertThat(text.getText()).isEqualTo("        ");
	}
}
