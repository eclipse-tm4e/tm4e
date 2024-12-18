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
package org.eclipse.tm4e.ui.internal.text;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lateNonNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.tm4e.ui.text.ITMPresentationReconcilerListener;

public final class TMPresentationReconcilerTestGenerator
		implements ITMPresentationReconcilerListener, IDocumentListener, ITextListener {

	private ITextViewer viewer = lateNonNull();
	private IDocument document = lateNonNull();

	private final StringBuilder code = new StringBuilder();

	/*private List<Command> commands = new ArrayList<>();

	private static final class Command {
		final String command;
		StyleRange[] ranges;
		Throwable error;

		Command(String command) {
			this.command = command;
		}
	}*/

	@Override
	public void onInstalled(final ITextViewer viewer, final IDocument document) {
		this.viewer = viewer;
		this.document = document;
		document.addDocumentListener(this);
		viewer.addTextListener(this);

		write("package org.eclipse.tm4e.ui.text;", true);
		write("", true);

		write("import org.eclipse.jface.text.Document;", true);
		write("import org.eclipse.jface.text.IDocument;", true);
		write("import org.eclipse.jface.text.TextViewer;", true);
		write("import org.eclipse.swt.SWT;", true);
		write("import org.eclipse.swt.widgets.Display;", true);
		write("import org.eclipse.swt.widgets.Shell;", true);
		write("import org.eclipse.tm4e.core.grammar.IGrammar;", true);
		write("import org.eclipse.tm4e.core.registry.Registry;", true);
		write("import org.eclipse.tm4e.ui.text.TMPresentationReconciler;", true);
		write("import org.eclipse.tm4e.ui.themes.ITokenProvider;", true);
		write("import org.eclipse.tm4e.ui.themes.css.CSSTokenProvider;", true);
		write("import org.junit.Test;", true);
		write("", true);

		write("public class TMPresentationReconcilerTest {", true);
		write("", true);

		write("\t@Test", true);
		write("\tpublic void colorize() throws Exception {", true);
		write("", true);
		write("\t\tDisplay display = new Display();", true);
		write("\t\tShell shell = new Shell(display);", true);
		write("\t\tTextViewer viewer = new TextViewer(shell, SWT.NONE);", true);
		write("\t\tIDocument document = new Document();", true);
		write("\t\tviewer.setDocument(document);", true);
		write("\t\tdocument.set(\"");
		write(toText(document.get()));
		write("\");", true);

		// commands.add(new Command("document.set(...)"));
		write("", true);
		write("\t\tTMPresentationReconciler reconciler = new TMPresentationReconciler();", true);
		write("\t\treconciler.setTokenProvider(getTokenProvider());", true);
		write("\t\treconciler.setGrammar(getGrammar());", true);
		write("\t\treconciler.install(viewer);", true);
		write("", true);

	}

	private String toText(final String text) {
		final var newText = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			switch (c) {
				case '\n':
					newText.append("\\n");
					break;
				case '\r':
					newText.append("\\r");
					break;
				case '"':
					newText.append("\\\"");
					break;
				default:
					newText.append(c);
			}
		}
		return newText.toString();
	}

	@Override
	public void onUninstalled() {

		// for (Command command : commands) {
		// write(toString(command.ranges));
		// }
		//
		write("", true);
		write("\t\twhile (!shell.isDisposed()) {", true);
		write("\t\t}", true);

		write("\t}", true);

		write("\tprivate static ITokenProvider getTokenProvider() {", true);
		write("\t\treturn new CSSTokenProvider(TMPresentationReconcilerTest.class.getResourceAsStream(\"Solarized-light.css\"));",
				true);
		write("\t}", true);
		write("", true);

		write("\tprivate static IGrammar getGrammar() {", true);
		write("\t\tRegistry registry = new Registry();", true);
		write("\t\ttry {", true);
		write("\t\tString grammar=\"YouGrammar.tmLanguage\";", true);
		write("\t\t\treturn registry.loadGrammarFromPathSync(grammar,TMPresentationReconcilerTest.class.getResourceAsStream(grammar));",
				true);
		write("\t\t} catch (Exception e) {", true);
		write("\t\t\te.printStackTrace();", true);
		write("\t\treturn null;", true);
		write("\t\t}", true);
		write("\t}", true);

		write("}");

		System.err.println(code.toString());
		document.removeDocumentListener(this);
		viewer.removeTextListener(this);
		// commands.clear();
	}

	@Override
	public void onColorized(final TextPresentation presentation, final @Nullable Throwable e) {
		// Command command = commands.get(commands.size() - 1);
		// if (e != null) {
		// command.error = e;
		// } else {
		// command.ranges = viewer.getTextWidget().getStyleRanges();
		// }
	}

	private void write(final String s, final boolean newLine) {
		code.append(s);
		if (newLine) {
			code.append("\n");
		}
	}

	private void write(final String s) {
		write(s, false);
	}

	@Override
	public void documentAboutToBeChanged(final DocumentEvent event) {
	}

	@Override
	public void documentChanged(final DocumentEvent event) {
		final String command = "document.replace(" + event.getOffset() + ", " + event.getLength() + ", \""
				+ toText(event.getText()) + "\");";
		write("\t\t" + command, true);

		// commands.add(new Command(command));
	}

	@Override
	public void textChanged(final TextEvent event) {
		if (event.getDocumentEvent() != null) {
			return;
		}

		final String command = "viewer.invalidateTextPresentation(" + event.getOffset() + ", " + event.getLength()
				+ ");";
		write("\t\t" + command, true);

		// commands.add(new Command(command));
	}
}
