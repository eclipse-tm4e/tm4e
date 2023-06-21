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
import org.eclipse.jface.text.TextViewer;

public class TextViewerInvalidateTextPresentationCommand extends Command {

	private final int offset;
	private final int length;
	private final TextViewer viewer;

	public TextViewerInvalidateTextPresentationCommand(final int offset, final int length, final TextViewer viewer) {
		super(getName(offset, length));
		this.offset = offset;
		this.length = length;
		this.viewer = viewer;
	}

	public static String getName(final int offset, final int length) {
		return "viewer.invalidateTextPresentation(" + offset + ", " + length + ");";
	}

	@Override
	protected void doExecute() {
		viewer.getTextWidget().getDisplay().syncExec(() -> viewer.invalidateTextPresentation(offset, length));
	}

	@Override
	public Integer getLineTo() {
		try {
			return viewer.getDocument().getLineOfOffset(offset + length);
		} catch (final BadLocationException e) {
			return null;
		}
	}

}
