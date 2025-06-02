/**
 * Copyright (c) 2021, 2022 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages;
import org.eclipse.tm4e.languageconfiguration.internal.ToggleLineCommentHandler;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TestComment {

	@AfterEach
	public void tearDown() throws Exception {
		UI.getActivePage().closeAllEditors(false);
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		String text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo("/*a*/\n\n/*b*/\n\n/*c*/");
		checktTextSelection(editor.getSelectionProvider().getSelection(), 2, 15);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo("a\n\nb\n\nc");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(1, 5)); // [* a *]
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);

		text = doc.get();
		assertThat(text).isEqualTo(" a ");
		final ISelection selection = editor.getSelectionProvider().getSelection();
		assertThat(selection).isNotNull();
		assertThat(selection).isInstanceOf(ITextSelection.class);
		final var textSelection = (ITextSelection) selection;
		assertThat(textSelection.getOffset()).isEqualTo(0);
		assertThat(textSelection.getLength()).isEqualTo(3);
		checktTextSelection(editor.getSelectionProvider().getSelection(), 0, 3);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo("/* a */");
		checktTextSelection(editor.getSelectionProvider().getSelection(), 2, 3);
	}

	/**
	 * Test case for https://github.com/eclipse/wildwebdeveloper/issues/909
	 */
	@Test
	public void testToggleLineCommentUseBlockCommentAndWindowsEOL() throws Exception {
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noLineComment");
		file.create(new ByteArrayInputStream("a\r\n\r\nb\r\n\r\nc".getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(0, 0)); // No matter the selection length
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo("/*a*/\r\n\r\nb\r\n\r\nc");
		checktTextSelection(editor.getSelectionProvider().getSelection(), 2, 0);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		editor.getSelectionProvider().setSelection(new TextSelection(0, 0)); // No matter the selection length
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo("a\r\n\r\nb\r\n\r\nc");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		String text = doc.get();
		int indexOfA = text.indexOf('a');
		int lengthToC = text.indexOf('c') + 1 - indexOfA;
		editor.getSelectionProvider().setSelection(new TextSelection(indexOfA, lengthToC));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		String commented = padding + "//a\n//b\n//c\n" + padding;
		assertThat(doc.get()).isEqualTo(commented);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(input);
		checktTextSelection(editor.getSelectionProvider().getSelection(), indexOfA, lengthToC);
	}

	@Test
	public void testToggleLineCommentOnReadOnlyFileAndMakeWritable() throws Exception {
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("readonlytest.lc-test");
		String content = "line1\nline2\n";
		file.create(new ByteArrayInputStream(content.getBytes()), true, null);

		// Make the file read-only on disk
		ResourceAttributes attrs = file.getResourceAttributes();
		attrs.setReadOnly(true);
		file.setResourceAttributes(attrs);

		// Verify that the file is read-only
		assertThat(file.getResourceAttributes().isReadOnly()).isTrue();

		// Open the editor on the read-only file
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);

		// Before executing the toggle command, schedule an asyncExec to click "Yes" on the MessageDialog
		Display.getDefault().asyncExec(new Runnable() {
			Button findYesButton(final Composite parent) {
				for (final Control child : parent.getChildren()) {
					if (child instanceof final Button button
							&& button.getText().toLowerCase().contains("yes"))
						return button;
					if (child instanceof final Composite composite) {
						final Button result = findYesButton(composite);
						if (result != null)
							return result;
					}
				}
				return null;
			}

			@Override
			public void run() {
				for (final Shell shell : Display.getDefault().getShells()) {
					if (LanguageConfigurationMessages.ToggleLineCommentHandler_ReadOnlyEditor_title.equals(shell.getText())) {
						Button yesButton = findYesButton(shell);
						if (yesButton != null) {
							yesButton.notifyListeners(SWT.Selection, new Event());
						}
						break;
					}
				}
			}
		});

		// Attempt to toggle-line-comment; this will open the dialog and then click "Yes"
		String text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);

		// After dialog, the file should have been made writable by the handler internally
		assertThat(file.getResourceAttributes().isReadOnly()).isFalse();

		// Now that the file is writable, comments should be applied
		assertThat(doc.get()).isEqualTo("//line1\n//line2\n");
	}

	@Test
	public void testToggleBlockCommentUseLineComment() throws Exception {
		final var now = System.currentTimeMillis();
		final var proj = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + now);
		proj.create(null);
		proj.open(null);
		final var file = proj.getFile("whatever.noBlockComment");
		file.create(new ByteArrayInputStream("a\n\nb\n\nc".getBytes()), true, null);
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		String text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.ADD_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo("//a\n//\n//b\n//\n//c");
		checktTextSelection(editor.getSelectionProvider().getSelection(), 2, 15);

		// Repeatedly executed toggle comment command should remove the comments inserted previously
		text = doc.get();
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo("a\n\nb\n\nc");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(" a ");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(0, text.length()));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(" a  b  c ");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(1, text.length() - 2));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(" a ");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(5, 7)); // [*/ b /*]
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(" a  b  c ");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(6, 5)); // [/ b /]
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(" a  b  c ");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(2, 1)); // [b]
		service.executeCommand(ToggleLineCommentHandler.ADD_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo("a /*b*/ c");
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
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		editor.getSelectionProvider().setSelection(new TextSelection(4, 1)); // [b]
		service.executeCommand(ToggleLineCommentHandler.ADD_BLOCK_COMMENT_COMMAND_ID, null);

		// No comment is to be added because the selection is already inside a block comment
		assertThat(doc.get()).isEqualTo(text);
		checktTextSelection(editor.getSelectionProvider().getSelection(), 4, 1);
	}

	private void checktTextSelection(final ISelection selection, final int expectedOffset, final int expectedLength) {
		assertThat(selection).isNotNull();
		assertThat(selection).isInstanceOf(ITextSelection.class);
		final var textSelection = (ITextSelection) selection;
		assertThat(textSelection.getOffset()).isEqualTo(expectedOffset);
		assertThat(textSelection.getLength()).isEqualTo(expectedLength);
	}
}
