/**
 * Copyright (c) 2021 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TestComment {

	@AfterEach
	public void tearDown() throws Exception {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, null);
		}
	}

	@Test
	public void testIndentOnNewLine() throws Exception {
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		IFile file = p.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream("a\nb\nc".getBytes()), true, null);
		ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		IHandlerService service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(0, 5));
		service.executeCommand("org.eclipse.tm4e.languageconfiguration.togglelinecommentcommand", null);
		assertEquals("/*a*/\n/*b*/\n/*c*/", doc.get());
	}
}
