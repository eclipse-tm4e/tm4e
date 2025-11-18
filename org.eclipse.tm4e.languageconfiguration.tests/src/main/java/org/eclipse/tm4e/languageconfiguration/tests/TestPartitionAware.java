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

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.languageconfiguration.internal.ToggleLineCommentHandler;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.tm4e.ui.text.TMPartitions;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies TM partition mapping to content types and that auto-edit strategies
 * use partition-aware content types (basic smoke).
 */
public class TestPartitionAware {

	@AfterEach
	public void tearDown() throws Exception {
		UI.getActivePage().closeAllEditors(false);
		for (final IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, true, null);
		}
	}

	@Test
	public void testPartitionMapsToContentTypes() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("embedded.html");
		final String html = """
			<html>
			<head>
			  <style>
			    body { color: red; }
			  </style>
			</head>
			<body>
			  <script>
			    function x() { return 1; }
			  </script>
			</body>
			</html>""";
		file.create(new ByteArrayInputStream(html.getBytes()), true, null);

		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		// ensure TM partitioner installed and model tokenized
		TestUtils.waitForModelReady(doc, 5_000);
		TestUtils.waitForAndAssertCondition(5_000, () -> TMPartitions.hasPartitioning(doc));

		final int cssIdx = doc.get().indexOf("color");
		final IContentType[] cssTypes = TMPartitions.getContentTypesForOffset(doc, cssIdx);
		assertThat(cssTypes).isNotEmpty();
		assertThat(cssTypes).anySatisfy(ct -> assertThat(ct.getId()).isEqualTo("org.eclipse.tm4e.language_pack.css"));

		final int jsIdx = doc.get().indexOf("function x");
		final IContentType[] jsTypes = TMPartitions.getContentTypesForOffset(doc, jsIdx);
		assertThat(jsTypes).isNotEmpty();
		assertThat(jsTypes).anySatisfy(ct -> assertThat(ct.getId()).isEqualTo("org.eclipse.tm4e.language_pack.javascript"));
	}

	@Test
	public void testAutoClosingUsesPartitionAtOffset() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("embedded.html");
		final String html = """
			<html>
			<head>
			 <style>
			    body { color: red; }
			  </style>
			</head>
			<body>
			  <script>
			    let a = 1;
			  </script>
			</body>
			</html>""";
		file.create(new ByteArrayInputStream(html.getBytes()), true, null);

		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TestUtils.waitForModelReady(doc, 5_000);
		TestUtils.waitForAndAssertCondition(5_000, () -> TMPartitions.hasPartitioning(doc));
		final StyledText styled = (StyledText) editor.getAdapter(Control.class);

		// Move caret to inside <script>, end of 'let a = 1;'
		final int caret = doc.get().indexOf("let a = 1;")
				+ "let a = 1;".length();
		styled.setCaretOffset(caret);

		// Type '(' and expect auto-close ')'. JS language configuration must be active here.
		final String before = styled.getText();
		styled.replaceTextRange(styled.getCaretOffset(), 0, "(");
		final String after = styled.getText();
		assertThat(after.length()).isEqualTo(before.length() + 2);
		assertThat(after.substring(caret, caret + 2)).isEqualTo("()");
		assertThat(styled.getCaretOffset()).isEqualTo(caret + 1);
	}

	@Test
	public void testToggleLineCommentPartitionAwareEmbeddedScript() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("partition-comments.html");
		final String html = """
			<html>
			<head>
			  <style>
			    body { color: red; }
			  </style>
			</head>
			<body>
			  <script>
			    const first = 1;
			    const second = 2;
			  </script>
			</body>
			</html>""";
		file.create(new ByteArrayInputStream(html.getBytes()), true, null);

		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TestUtils.waitForModelReady(doc, 5_000);
		TestUtils.waitForAndAssertCondition(5_000, () -> TMPartitions.hasPartitioning(doc));

		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final String original = doc.get();
		final int start = original.indexOf("const first = 1;");
		final int end = original.indexOf("const second = 2;") + "const second = 2;".length();
		editor.getSelectionProvider().setSelection(new TextSelection(start, end - start));

		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		final String commented = doc.get();
		final String expected = original.replace("    const first = 1;", "//    const first = 1;")
				.replace("    const second = 2;", "//    const second = 2;");
		assertThat(commented).isEqualTo(expected);

		// Wait until the JS partition is restored for the commented lines, so that
		// the second toggle can again resolve the JS line comment token from
		// TMPartitions instead of temporarily seeing base/HTML.
		TestUtils.waitForAndAssertCondition(5_000, () -> {
			final int jsIdx = doc.get().indexOf("const first = 1;");
			if (jsIdx < 0)
				return false;
			return TMPartitions.getContentTypesForOffset(doc, jsIdx).length > 0;
		});

		service.executeCommand(ToggleLineCommentHandler.TOGGLE_LINE_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(original);
	}

	@Test
	public void testBlockCommentCommandsPartitionAwareEmbeddedScript() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("partition-block-comments.html");
		final String html = """
			<html>
			<body>
			  <script>
			    const alpha = 1;
			    const beta = 2;
			  </script>
			</body>
			</html>""";
		file.create(new ByteArrayInputStream(html.getBytes()), true, null);

		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TestUtils.waitForModelReady(doc, 5_000);
		TestUtils.waitForAndAssertCondition(5_000, () -> TMPartitions.hasPartitioning(doc));

		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final String original = doc.get();
		final int start = original.indexOf("const alpha = 1;");
		final int end = original.indexOf("const beta = 2;") + "const beta = 2;".length();
		editor.getSelectionProvider().setSelection(new TextSelection(start, end - start));

		service.executeCommand(ToggleLineCommentHandler.ADD_BLOCK_COMMENT_COMMAND_ID, null);
		final String afterAdd = doc.get();
		final String expectedAdd = original.substring(0, start) + "/*"
				+ original.substring(start, end) + "*/" + original.substring(end);
		assertThat(afterAdd).isEqualTo(expectedAdd);

		editor.getSelectionProvider().setSelection(new TextSelection(start, end - start + 4));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(original);
	}

	@Test
	public void testBlockCommentCommandsHtmlPartition() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("html-block-comments.html");
		final String html = """
			<html>
			<body>
			  <p>Alpha</p>
			</body>
			</html>""";
		file.create(new ByteArrayInputStream(html.getBytes()), true, null);

		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		TestUtils.waitForModelReady(doc, 5_000);
		TestUtils.waitForAndAssertCondition(5_000, () -> TMPartitions.hasPartitioning(doc));

		final var service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final String original = doc.get();
		final int start = original.indexOf("Alpha");
		final int length = "Alpha".length();
		editor.getSelectionProvider().setSelection(new TextSelection(start, length));

		service.executeCommand(ToggleLineCommentHandler.ADD_BLOCK_COMMENT_COMMAND_ID, null);
		final String afterAdd = doc.get();
		final String expected = original.substring(0, start) + "<!--Alpha-->" + original.substring(start + length);
		assertThat(afterAdd).isEqualTo(expected);

		final int commentedOffset = afterAdd.indexOf("<!--Alpha-->");
		editor.getSelectionProvider()
				.setSelection(new TextSelection(commentedOffset, "<!--Alpha-->".length()));
		service.executeCommand(ToggleLineCommentHandler.REMOVE_BLOCK_COMMENT_COMMAND_ID, null);
		assertThat(doc.get()).isEqualTo(original);
	}

	@Test
	public void testGetPartitionAndScopesInEmbeddedHtml() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("embedded.html");
		final String html = """
			<html>
			<head>
			  <style>
			    body { color: red; }
			  </style>
			</head>
			<body>
			  <script>
			    function x() { return 1; }
			  </script>
			</body>
			</html>""";
		file.create(new ByteArrayInputStream(html.getBytes()), true, null);

		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		TestUtils.waitForModelReady(doc, 5_000);
		TestUtils.waitForAndAssertCondition(5_000, () -> TMPartitions.hasPartitioning(doc));

		// Base HTML at the opening tag
		final int htmlIdx = doc.get().indexOf("<html>") + 1;
		final var htmlPart = TMPartitions.getPartition(doc, htmlIdx);
		assertThat(htmlPart.getType()).isEqualTo("tm4e:text.html");
		assertThat(htmlPart.getGrammarScope()).isEqualTo("text.html");

		// CSS inside <style>
		final int cssIdx = doc.get().indexOf("color");
		final var cssPart = TMPartitions.getPartition(doc, cssIdx);
		assertThat(cssPart.getType()).isEqualTo("tm4e:source.css");
		assertThat(cssPart.getGrammarScope()).isEqualTo("source.css");

		// JavaScript inside <script>
		final int jsIdx = doc.get().indexOf("function x");
		final var jsPart = TMPartitions.getPartition(doc, jsIdx);
		assertThat(jsPart.getType()).isEqualTo("tm4e:source.js");
		assertThat(jsPart.getGrammarScope()).isEqualTo("source.js");
	}
}
