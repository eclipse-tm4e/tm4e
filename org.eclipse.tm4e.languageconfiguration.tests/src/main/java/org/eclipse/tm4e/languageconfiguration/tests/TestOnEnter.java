/**
 * Copyright (c) 2022 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

public class TestOnEnter {
	@AfterEach
	public void tearDown() throws Exception {
		UI.getActivePage().closeAllEditors(false);
		for (final IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, null);
		}
	}

	@Test
	public void testOnEnter() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("whatever.lc-test");
		file.create(new ByteArrayInputStream("  ///".getBytes()), true, null);
		final ITextEditor editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final StyledText text = (StyledText) editor.getAdapter(Control.class);
		text.setSelection(text.getText().length());
		text.insert("\n");
		assertThat(text.getText()).isEqualTo("  ///\n  /// ");
	}
}
