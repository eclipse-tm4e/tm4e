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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.tm4e.core.model.AbstractModelLines;
import org.eclipse.tm4e.ui.TMUIPlugin;

/**
 * TextMate {@link AbstractModelLines} implementation with Eclipse {@link IDocument}.
 *
 * Goal of this class is to synchronize Eclipse {@link DocumentEvent} with TextMate model lines.
 */
final class DocumentModelLines extends AbstractModelLines implements IDocumentListener {

	private final IDocument document;
	private int endLineIndexOfRemovedText = -1;

	DocumentModelLines(final IDocument document) {
		super(document.getNumberOfLines());
		this.document = document;
		document.addDocumentListener(this);
	}

	@Override
	public void documentAboutToBeChanged(@Nullable final DocumentEvent event) {
		if (event == null)
			return;
		try {
			switch (DocumentHelper.getEventType(event)) {
				case REMOVE, REPLACE /*= Remove + Insert */:
					endLineIndexOfRemovedText = DocumentHelper.getEndLineIndexOfRemovedText(event);
					// => cannot be calculated in documentChanged() where it will result in BadLocationException
					break;
				default:
			}
		} catch (final BadLocationException ex) {
			TMUIPlugin.logError(ex);
		}
	}

	@Override
	public void documentChanged(@Nullable final DocumentEvent event) {
		if (event == null)
			return;
		try {
			final int startLineIndex = DocumentHelper.getStartLineIndex(event);
			switch (DocumentHelper.getEventType(event)) {
				case INSERT: {
					final var endLineIndexOfAddedText = DocumentHelper.getEndLineIndexOfAddedText(event);
					final var isFullLineInsert = DocumentHelper.getStartLineCharIndex(event) == 0
							&& event.getText().endsWith("\n");

					final var linesAdded = (isFullLineInsert ? 0 : 1) + (endLineIndexOfAddedText - startLineIndex);
					replaceLines(startLineIndex, isFullLineInsert ? 0 : 1, linesAdded);
					break;
				}
				case REMOVE: {
					replaceLines(startLineIndex, 1 + (endLineIndexOfRemovedText - startLineIndex), 1);
					break;
				}
				case REPLACE: {
					final var endLineIndexOfAddedText = DocumentHelper.getEndLineIndexOfAddedText(event);
					final var isFullLineInsert = DocumentHelper.getStartLineCharIndex(event) == 0
							&& event.getText().endsWith("\n");

					replaceLines(startLineIndex,
							(isFullLineInsert ? 0 : 1) + (endLineIndexOfRemovedText - startLineIndex),
							(isFullLineInsert ? 0 : 1) + (endLineIndexOfAddedText - startLineIndex));
					break;
				}
			}
		} catch (final BadLocationException ex) {
			TMUIPlugin.logError(ex);
		}
	}

	@Override
	public String getLineText(final int lineIndex) throws Exception {
		return DocumentHelper.getLineText(document, lineIndex, false);
	}

	@Override
	public void dispose() {
		document.removeDocumentListener(this);
	}
}
