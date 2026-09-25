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
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies auto-closing of language configuration pairs, including conditional pairs whose {@code notIn} classification
 * is derived from the TextMate scopes at the edit position.
 */
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

	@Test
	public void testNotInSuppressesClosingInStringsAndComments() throws Exception {
		// JavaScript: ' is notIn string/comment, " is notIn string only
		assertThat(typeInJavaScript("const s = \"abc \";", "abc", "'")).isEqualTo("const s = \"abc' \";");
		assertThat(typeInJavaScript("const s = `abc `;", "abc", "'")).isEqualTo("const s = `abc' `;");
		assertThat(typeInJavaScript("// abc \n", "abc", "'")).isEqualTo("// abc' \n");
		assertThat(typeInJavaScript("// abc \n", "abc", "\"")).isEqualTo("// abc\"\" \n");
		// Regex literals are scoped string.regexp, which classifies as string
		assertThat(typeInJavaScript("const r = /abc /;", "abc", "'")).isEqualTo("const r = /abc' /;");
		// At line start there is no preceding character; the first token of a comment continuation line still applies
		assertThat(typeInJavaScript("/*\n abc\n*/", "/*\n", "'")).isEqualTo("/*\n' abc\n*/");
	}

	@Test
	public void testNotInUsesInnermostScopeInsideEmbeddedCode() throws Exception {
		// The ${...} content is scoped meta.embedded.line.js inside string.template.js, so it is code, not a string
		assertThat(typeInJavaScript("const s = `${ }`;", "${ ", "'")).isEqualTo("const s = `${ ''}`;");
		// A string inside the embedded code classifies the position as a string again
		assertThat(typeInJavaScript("const s = `${ \"abc \" }`;", "abc", "'")).isEqualTo("const s = `${ \"abc' \" }`;");
		// The same applies to a comment inside the embedded code
		assertThat(typeInJavaScript("const s = `${ /* abc */ }`;", "abc", "'")).isEqualTo("const s = `${ /* abc' */ }`;");
	}

	/**
	 * Opens a JavaScript file with the given content, types the given text directly after the first occurrence of
	 * {@code typeAfter}, and returns the resulting editor text.
	 */
	private String typeInJavaScript(final String content, final String typeAfter, final String typed) throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.nanoTime());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("test.js");
		file.create(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), true, null);
		final var editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		// notIn is evaluated against the TM tokens, so the line must be tokenized before typing
		TestUtils.waitForModelReady(doc, 5_000);
		final StyledText text = (StyledText) editor.getAdapter(Control.class);
		text.replaceTextRange(content.indexOf(typeAfter) + typeAfter.length(), 0, typed);
		return text.getText();
	}
}
