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
package org.eclipse.tm4e.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class TMinGenericEditorTest {
	
	private IEditorDescriptor editorDescriptor;
	private File f;
	private IEditorPart editor;

	private Collection<Thread> getTM4EThreads() {
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		Set<Thread> res = new HashSet<>();
		for (Thread thread : threads) {
			if (thread.getClass().getName().contains("tm4e")) {
				res.add(thread);
			}
		}
		return res;
	}
	
	@Before
	public void checkHasGenericEditor() {
		editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry().findEditor("org.eclipse.ui.genericeditor.GenericEditor");
		Assume.assumeNotNull(editorDescriptor);
		Assert.assertTrue("TM4E threads still running", getTM4EThreads().isEmpty());
	}
	
	@After
	public void tearDown() {
		editor.getEditorSite().getPage().closeEditor(editor, false);
		editor = null;
		f.delete();
		f = null;
	}

	@Test
	public void testTMHighlightInGenericEditor() throws IOException, PartInitException {
		f = File.createTempFile("test" + System.currentTimeMillis(), ".ts");
		FileOutputStream fileOutputStream = new FileOutputStream(f);
		fileOutputStream.write("let a = '';\nlet b = 10;\nlet c = true;".getBytes());
		fileOutputStream.close();
		f.deleteOnExit();
		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				f.toURI(), editorDescriptor.getId(), true);
		StyledText text = (StyledText)editor.getAdapter(Control.class);
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return text.getStyleRanges().length > 1;
			}
		}.waitForCondition(text.getDisplay(), 3000);
		Assert.assertTrue(text.getStyleRanges().length > 1);
	}
	
	@Test
	public void testTMHighlightInGenericEditorEdit() throws IOException, PartInitException {
		f = File.createTempFile("test" + System.currentTimeMillis(), ".ts");
		FileOutputStream fileOutputStream = new FileOutputStream(f);
		fileOutputStream.write("let a = '';".getBytes());
		fileOutputStream.close();
		f.deleteOnExit();
		editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				f.toURI(), editorDescriptor.getId(), true);
		StyledText text = (StyledText)editor.getAdapter(Control.class);
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return text.getStyleRanges().length > 1;
			}
		}.waitForCondition(text.getDisplay(), 3000);
		int numberOfRanges = text.getStyleRanges().length;
		Assert.assertTrue(numberOfRanges > 1);
		text.setText("let a = '';\nlet b = 10;\nlet c = true;");
		DisplayHelper.runEventLoop(text.getDisplay(), 3000);
		Assert.assertTrue("More styles should have been added", text.getStyleRanges().length > numberOfRanges + 3);
	}

	@Test
	public void testReconcilierStartsAndDisposeThread() throws Exception {
		testTMHighlightInGenericEditor();
		editor.getEditorSite().getPage().closeEditor(editor, false);
		Thread.sleep(500); // give time to dispose
		Assert.assertTrue(getTM4EThreads().isEmpty());
	}

}
