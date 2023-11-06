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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.google.common.base.Strings;

/**
 * Utilities class for {@link IDocument}.
 */
public final class DocumentHelper {

	enum DocumentEventType {
		INSERT,
		REPLACE,
		REMOVE
	}

	static DocumentEventType getEventType(final DocumentEvent event) {
		if (Strings.isNullOrEmpty(event.getText())) {
			return DocumentEventType.REMOVE;
		}
		return event.getLength() == 0
				? DocumentEventType.INSERT
				: DocumentEventType.REPLACE;
	}

	/**
	 * @return the index of the first char that has been modified of the start line
	 */
	static int getStartLineCharIndex(final DocumentEvent event) throws BadLocationException {
		return event.getOffset() - event.getDocument().getLineOffset(getStartLineIndex(event));
	}

	static int getStartLineIndex(final DocumentEvent event) throws BadLocationException {
		return event.getDocument().getLineOfOffset(event.getOffset());
	}

	static int getEndLineIndexOfAddedText(final DocumentEvent event) throws BadLocationException {
		final var doc = event.getDocument();
		final var offsetAfterAddedText = event.getOffset() + event.getText().length();
		final var isAppendToDocumentEnd = offsetAfterAddedText == doc.getLength();
		if (isAppendToDocumentEnd) {
			return doc.getNumberOfLines() - 1;
		}
		return doc.getLineOfOffset(offsetAfterAddedText);
	}

	/**
	 * Must only be called in {@link IDocumentListener#documentAboutToBeChanged(DocumentEvent)} and not
	 * {@link IDocumentListener#documentChanged(DocumentEvent)} where it might result in a {@link BadLocationException}
	 * exception
	 */
	static int getEndLineIndexOfRemovedText(final DocumentEvent event) throws BadLocationException {
		return event.getDocument().getLineOfOffset(event.getOffset() + event.getLength());
	}

	/**
	 * @param lineIndex 0-based
	 */
	public static String getLineText(final IDocument document, final int lineIndex, final boolean withLineDelimiter)
			throws BadLocationException {
		final int lo = document.getLineOffset(lineIndex);
		int ll = document.getLineLength(lineIndex);
		if (!withLineDelimiter) {
			final String delim = document.getLineDelimiter(lineIndex);
			ll = ll - (delim != null ? delim.length() : 0);
		}
		return document.get(lo, ll);
	}

	/**
	 * @param fromLineIndex 0-based
	 * @param toLineIndex 0-based
	 */
	private static IRegion getRegion(final IDocument document, final int fromLineIndex, final int toLineIndex)
			throws BadLocationException {
		final int startOffset = document.getLineOffset(fromLineIndex);
		final int endOffset = document.getLineOffset(toLineIndex) + document.getLineLength(toLineIndex);
		return new Region(startOffset, endOffset - startOffset);
	}

	private DocumentHelper() {
	}
}
