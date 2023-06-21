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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

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

	@BeforeEach
	public void checkHasGenericEditor() {
		editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry()
				.findEditor("org.eclipse.ui.genericeditor.GenericEditor");
		assertNotNull(editorDescriptor);
	}

	@BeforeEach
	public void checkNoTM4EThreadsRunning() throws InterruptedException {
		var tm4eThreads = Thread.getAllStackTraces();
		tm4eThreads.entrySet().removeIf(e -> !e.getKey().getClass().getName().startsWith("org.eclipse.tm4e"));

		if (!tm4eThreads.isEmpty()) {
			Thread.sleep(5_000); // give threads time to finish
		}

		tm4eThreads = Thread.getAllStackTraces();
		tm4eThreads.entrySet().removeIf(e -> !e.getKey().getClass().getName().startsWith("org.eclipse.tm4e"));

		if (!tm4eThreads.isEmpty()) {
			// print the stacktrace of one of the hung threads
			final var tm4eThread = tm4eThreads.entrySet().iterator().next();
			final var ex = new IllegalStateException("Thread " + tm4eThread.getKey() + " is still busy");
			ex.setStackTrace(tm4eThread.getValue());
			ex.printStackTrace(System.out);

			fail("TM4E threads still running:\n" + tm4eThreads.keySet().stream()
					.map(t -> " - " + t + " " + t.getClass().getName()).collect(Collectors.joining("\n")));
		}
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
		try (var fileOutputStream = new FileOutputStream(f)) {
			fileOutputStream.write("let a = '';\nlet b = 10;\nlet c = true;".getBytes());
		}
		f.deleteOnExit();
		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				f.toURI(), editorDescriptor.getId(), true);
		final StyledText text = (StyledText) editor.getAdapter(Control.class);
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
		try (var fileOutputStream = new FileOutputStream(f)) {
			fileOutputStream.write("let a = '';".getBytes());
		}
		f.deleteOnExit();
		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				f.toURI(), editorDescriptor.getId(), true);
		final StyledText text = (StyledText) editor.getAdapter(Control.class);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return text.getStyleRanges().length > 1;
			}
		}.waitForCondition(text.getDisplay(), 3000));
		final int initialNumberOfRanges = text.getStyleRanges().length;
		text.setText("let a = '';\nlet b = 10;\nlet c = true;");
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return text.getStyleRanges().length > initialNumberOfRanges + 3;
			}
		}.waitForCondition(text.getDisplay(), 300000), "More styles should have been added");
	}

	@Test
	void testReconcilierStartsAndDisposeThread() throws Exception {
		testTMHighlightInGenericEditor();
		editor.getEditorSite().getPage().closeEditor(editor, false);
		checkNoTM4EThreadsRunning();
	}
}
