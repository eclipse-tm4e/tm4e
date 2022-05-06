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
package org.eclipse.tm4e.ui.internal.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
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
final class DocumentLineList extends AbstractLineList {

	private final IDocument document;
	private final InternalListener listener = new InternalListener();

	DocumentLineList(final IDocument document) {
		this.document = document;
		document.addDocumentListener(listener);
		for (int i = 0; i < document.getNumberOfLines(); i++) {
			addLine(i);
		}
	}

	private final class InternalListener implements IDocumentListener {

		@Override
		public void documentAboutToBeChanged(@Nullable final DocumentEvent event) {
			if (event == null)
				return;
			try {
				if (!DocumentHelper.isInsert(event)) {
					// Remove or Replace (Remove + Insert)
					removeLine(event);
				}
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		}

		private void removeLine(final DocumentEvent event) throws BadLocationException {
			final int startLine = DocumentHelper.getStartLine(event);
			final int endLine = DocumentHelper.getEndLine(event, true);
			for (int i = endLine; i > startLine; i--) {
				DocumentLineList.this.removeLine(i);
			}
		}

		@Override
		public void documentChanged(@Nullable final DocumentEvent event) {
			if (event == null)
				return;
			try {
				final int startLine = DocumentHelper.getStartLine(event);
				if (!DocumentHelper.isRemove(event)) {
					final int endLine = DocumentHelper.getEndLine(event, false);
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
			} catch (final BadLocationException e) {
				TMUIPlugin.log(new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
	}

	@Override
	public int getNumberOfLines() {
		return document.getNumberOfLines();
	}

	@Override
	public String getLineText(final int line) throws Exception {
		return DocumentHelper.getLineText(document, line, false);
	}

	@Override
	public int getLineLength(final int line) throws Exception {
		return document.getLineLength(line);
	}

	@Override
	public void dispose() {
		document.removeDocumentListener(listener);
	}
}
