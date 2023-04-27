/**
 * Copyright (c) 2021, 2022 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.tm4e.languageconfiguration.internal.ToggleLineCommentHandler;
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
		for (final IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, null);
		}
	}

	@Test
	public void testToggleLineCommentUseBlockComment() throws Exception {
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream("a\n\nb\n\nc".getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		String text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertEquals("/*a*/\n\n/*b*/\n\n/*c*/", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 2, 15);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertEquals("a\n\nb\n\nc", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 0, 7);
	}

	@Test
	public void testToggleLineCommentUseBlockCommentnPartiallyIncludedEnds() throws Exception {
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		String text = "/* a */";
		file.create(new ByteArrayInputStream(text.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(1, 5)); // [* a *]
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);

		text = doc.get();
		assertEquals(" a ", text);
		final ISelection selection = editor.getSelectionProvider().getSelection();
		assertNotNull(selection);
		assertInstanceOf(ITextSelection.class, selection);
		final var textSelection = (ITextSelection) selection;
		assertEquals(0, textSelection.getOffset());
		assertEquals(3, textSelection.getLength());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 0, 3);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertEquals("/* a */", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 2, 3);
	}

	/**
	 * Test case for https://github.com/eclipse/wildwebdeveloper/issues/909
	 *
	 * @throws Exception
	 */
	@Test
	public void testToggleLineCommentUseBlockCommentAndWindowsEOL() throws Exception {
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream("a\r\n\r\nb\r\n\r\nc".getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(0, 0)); // No matter the selection length
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertEquals("/*a*/\r\n\r\nb\r\n\r\nc", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 2, 0);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		editor.getSelectionProvider().setSelection(new TextSelection(0, 0)); // No matter the selection length
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertEquals("a\r\n\r\nb\r\n\r\nc", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 0, 0);
	}
	
	@Test
	public void testToggleLineComment() throws Exception {
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.lc-test");
		// smallest padding so that the selected lines would not come out of a HashSet in order.
		String padding = "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n";
		String input = padding + "a\nb\nc\n" + padding;
		file.create(new ByteArrayInputStream(input.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		String text = doc.get();
		int indexOfA = text.indexOf('a');
		int lengthToC = text.indexOf('c') + 1 - indexOfA;
		editor.getSelectionProvider().setSelection(new TextSelection(indexOfA, lengthToC));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		String commented = padding + "//a\n//b\n//c\n" + padding;
		assertEquals(commented, doc.get());

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertEquals(input, doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), indexOfA, lengthToC);
	}

	@Test
	public void testToggleBlockCommentUseLineComment() throws Exception {
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noBlockComment");
		file.create(new ByteArrayInputStream("a\n\nb\n\nc".getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		String text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.ADD_BLOCK_COMMENT_COMMAND_ID, null);
		assertEquals("//a\n//\n//b\n//\n//c", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 2, 15);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertEquals("a\n\nb\n\nc", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 0, 7);
	}

	@Test
	public void testRemoveBlockComment() throws Exception {
		final String text = "/* a */";
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream(text.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertEquals(" a ", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 0, 0);
	}

	@Test
	public void testRemoveBlockCommentMultiplesComments() throws Exception {
		final String text = "/* a */ b /* c */";
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream(text.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertEquals(" a  b  c ", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 0, 0);
	}

	@Test
	public void testRemoveBlockCommentPartiallyIncludedEnds() throws Exception {
		final String text = "/* a */";
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream(text.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(1, text.length() - 2));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertEquals(" a ", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 0, 0);
	}

	@Test
	public void testRemoveBlockCommentMultiplesCommentsBrokenEnds() throws Exception {
		final String text = "/* a */ b /* c */";
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream(text.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(5, 7)); // [*/ b /*]
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertEquals(" a  b  c ", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 3, 0);
	}

	@Test
	public void testRemoveBlockCommentMultiplesCommentsBrokenPartiallyIncludedEnds() throws Exception {
		final String text = "/* a */ b /* c */";
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream(text.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(6, 5)); // [/ b /]
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertEquals(" a  b  c ", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 3, 0);
	}

	@Test
	public void testAddBlockComment() throws Exception {
		final String text = "a b c";
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream(text.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(2, 1)); // [b]
		service.executeCommand(ToggleLineCommentHandler.ADD_BLOCK_COMMENT_COMMAND_ID, null);
		assertEquals("a /*b*/ c", doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 4, 0);
	}

	@Test
	public void testAddBlockCommentInsideExistingBockComment() throws Exception {
		final String text = "/*a b c*/";
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream(text.getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file,
				"org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(4, 1)); // [b]
		service.executeCommand(ToggleLineCommentHandler.ADD_BLOCK_COMMENT_COMMAND_ID, null);

		// No comment is to be added because the selection is already inside a block comment
		assertEquals(text, doc.get());
		checktTextSelection(editor.getSelectionProvider().getSelection(), 4, 1);
	}

	private void checktTextSelection(final ISelection selection, final int expectedOffset, final int expectedLength) {
		assertNotNull(selection);
		assertInstanceOf(ITextSelection.class, selection);
		final var textSelection = (ITextSelection) selection;
		assertEquals(expectedOffset, textSelection.getOffset());
		assertEquals(expectedLength, textSelection.getLength());
	}
}
