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
package org.eclipse.tm4e.ui.internal.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.tm4e.core.model.AbstractLineList;
import org.eclipse.tm4e.ui.TMUIPlugin;

/**
 * TextMate {@link AbstractLineList} implementation with Eclipse
 * {@link IDocument}.
 *
 * Goal of this class is to synchronize Eclipse {@link DocumentEvent} with
 * TextMate model lines.
 *
 */
public class DocumentLineList extends AbstractLineList {

	private final IDocument document;
	private InternalListener listener;

	public DocumentLineList(IDocument document) {
		this.document = document;
		this.listener = new InternalListener();
		document.addDocumentListener(listener);
		for (int i = 0; i < document.getNumberOfLines(); i++) {
			addLine(i);
		}
	}

	private class InternalListener implements IDocumentListener {

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			try {
				if (!DocumentHelper.isInsert(event)) {
					// Remove or Replace (Remove + Insert)
					removeLine(event);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		private void removeLine(DocumentEvent event) throws BadLocationException {
			int startLine = DocumentHelper.getStartLine(event);
			int endLine = DocumentHelper.getEndLine(event, true);
			for (int i = endLine; i > startLine; i--) {
				DocumentLineList.this.removeLine(i);
			}
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			try {
				int startLine = DocumentHelper.getStartLine(event);
				if (!DocumentHelper.isRemove(event)) {
					int endLine = DocumentHelper.getEndLine(event, false);
					// Insert new lines
					for (int i = startLine; i < endLine; i++) {
						DocumentLineList.this.addLine(i + 1);
					}
					if (startLine == endLine) {
						DocumentLineList.this.updateLine(startLine);
					}
				} else {
					// Update line
					DocumentLineList.this.updateLine(startLine);
				}
				invalidateLine(startLine);
			} catch (BadLocationException e) {
				TMUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
	}

	@Override
	public int getNumberOfLines() {
		return document.getNumberOfLines();
	}

	@Override
	public String getLineText(int line) throws Exception {
		return DocumentHelper.getLineText(document, line, false);
	}

	@Override
	public int getLineLength(int line) throws Exception {
		return document.getLineLength(line);
	}

	@Override
	public void dispose() {
		document.removeDocumentListener(listener);
	}
}
