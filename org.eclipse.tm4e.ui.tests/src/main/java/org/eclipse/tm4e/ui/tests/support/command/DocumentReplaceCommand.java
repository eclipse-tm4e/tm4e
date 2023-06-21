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
package org.eclipse.tm4e.ui.tests.support.command;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

public class DocumentReplaceCommand extends Command {

	private final int pos;
	private final int length;
	private final String text;
	private final Document document;

	public DocumentReplaceCommand(final int pos, final int length, final String text, final Document document) {
		super(getName(pos, length, text));
		this.pos = pos;
		this.length = length;
		this.text = text;
		this.document = document;
	}

	public static String getName(final int pos, final int length, final String text) {
		return "document.replace(" + pos + ", " + length + ", \"" + toText(text) + "\");";
	}

	@Override
	protected void doExecute() {
		try {
			document.replace(pos, length, text);
		} catch (final BadLocationException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public Integer getLineTo() {
		try {
			return document.getLineOfOffset(pos + length);
		} catch (final BadLocationException e) {
			return null;
		}
	}
}
