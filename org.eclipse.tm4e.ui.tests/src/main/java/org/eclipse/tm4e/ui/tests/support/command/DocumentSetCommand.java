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

import org.eclipse.jface.text.Document;

public class DocumentSetCommand extends Command {

	private final String text;
	private final Document document;

	public DocumentSetCommand(final String text, final Document document) {
		super(getName(text));
		this.text = text;
		this.document = document;
	}

	public static String getName(final String text) {
		return "document.set(\"" + toText(text) + "\");";
	}

	@Override
	protected void doExecute() {
		document.set(text);
	}

	@Override
	public Integer getLineTo() {
		final int numberOfLines = document.getNumberOfLines();
		return numberOfLines > 0 ? numberOfLines - 1 : null;
	}
}
