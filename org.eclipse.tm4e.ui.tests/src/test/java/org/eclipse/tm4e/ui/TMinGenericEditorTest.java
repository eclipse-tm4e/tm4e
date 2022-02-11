/**
 * Copyright (c) 2015, 2021 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TMinGenericEditorTest {

	private IEditorDescriptor editorDescriptor;
	private File f;
	private IEditorPart editor;

	private Set<Thread> getTM4EThreads() {
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		Set<Thread> res = new HashSet<>();
		for (Thread thread : threads) {
			if (thread.getClass().getName().contains("tm4e")) {
				res.add(thread);
			}
		}
		return res;
	}

	@BeforeEach
	public void checkHasGenericEditor() {
		editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry().findEditor("org.eclipse.ui.genericeditor.GenericEditor");
		assumeTrue(editorDescriptor!=null);
		assertTrue(getTM4EThreads().isEmpty(), "TM4E threads still running");
	}

	@AfterEach
	public void tearDown() {
		final IEditorPart currentEditor = editor;
		if (currentEditor != null) {
			final IWorkbenchPartSite currentSite = currentEditor.getSite();
			if (currentSite != null) {
				final IWorkbenchPage currentPage = currentSite.getPage();
				if (currentPage != null) {
					currentPage.closeEditor(currentEditor, false);
				}
			}
		}
		editor = null;
		f.delete();
		f = null;
	}

	@Test
	void testTMHighlightInGenericEditor() throws IOException, PartInitException {
		f = File.createTempFile("test" + System.currentTimeMillis(), ".ts");
		try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
			fileOutputStream.write("let a = '';\nlet b = 10;\nlet c = true;".getBytes());
		}
		f.deleteOnExit();
		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				f.toURI(), editorDescriptor.getId(), true);
		StyledText text = (StyledText)editor.getAdapter(Control.class);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return text.getStyleRanges().length > 1;
			}
		}.waitForCondition(text.getDisplay(), 3000));
	}

	@Test
	void testTMHighlightInGenericEditorEdit() throws IOException, PartInitException {
		f = File.createTempFile("test" + System.currentTimeMillis(), ".ts");
		try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
			fileOutputStream.write("let a = '';".getBytes());
		}
		f.deleteOnExit();
		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				f.toURI(), editorDescriptor.getId(), true);
		StyledText text = (StyledText)editor.getAdapter(Control.class);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return text.getStyleRanges().length > 1;
			}
		}.waitForCondition(text.getDisplay(), 3000));
		int initialNumberOfRanges = text.getStyleRanges().length;
		text.setText("let a = '';\nlet b = 10;\nlet c = true;");
		assertTrue(new DisplayHelper() {
			@Override protected boolean condition() {
				return text.getStyleRanges().length > initialNumberOfRanges + 3;
			}
		}.waitForCondition(text.getDisplay(), 300000), "More styles should have been added");
	}

	@Test
	void testReconcilierStartsAndDisposeThread() throws Exception {
		testTMHighlightInGenericEditor();
		editor.getEditorSite().getPage().closeEditor(editor, false);
		Thread.sleep(500); // give time to dispose
		assertEquals(Collections.emptySet(), getTM4EThreads());
	}

}
