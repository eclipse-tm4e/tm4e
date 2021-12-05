/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

public class DocumentReplaceCommand extends Command {

	private final int pos;
	private final int length;
	private final String text;
	private final Document document;

	public DocumentReplaceCommand(int pos, int length, String text, Document document) {
		super(getName(pos, length, text));
		this.pos = pos;
		this.length = length;
		this.text = text;
		this.document = document;
	}

	public static String getName(int pos, int length, String text) {
		return "document.replace(" + pos + ", " + length + ", \"" + toText(text) + "\");";
	}

	@Override
	protected void doExecute() {
		try {
			document.replace(pos, length, text);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Integer getLineTo() {
		try {
			return document.getLineOfOffset(pos + length);
		} catch (BadLocationException e) {
			return null;
		}
	}

}
