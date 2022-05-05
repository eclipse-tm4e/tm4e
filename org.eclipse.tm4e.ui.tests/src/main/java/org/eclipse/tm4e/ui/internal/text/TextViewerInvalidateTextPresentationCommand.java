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
import org.eclipse.jface.text.TextViewer;

public class TextViewerInvalidateTextPresentationCommand extends Command {

	private final int offset;
	private final int length;
	private final TextViewer viewer;

	public TextViewerInvalidateTextPresentationCommand(int offset, int length, TextViewer viewer) {
		super(getName(offset, length));
		this.offset = offset;
		this.length = length;
		this.viewer = viewer;
	}

	public static String getName(int offset, int length) {
		return "viewer.invalidateTextPresentation(" + offset + ", " + length + ");";
	}

	@Override
	protected void doExecute() {
		viewer.getTextWidget().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				viewer.invalidateTextPresentation(offset, length);
			}
		});
	}

	@Override
	protected Integer getLineTo() {
		try {
			return viewer.getDocument().getLineOfOffset(offset + length);
		} catch (BadLocationException e) {
			return null;
		}
	}

}
