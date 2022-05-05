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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Utilities class for {@link IDocument}.
 *
 */
final class DocumentHelper {

	static int getStartLine(final DocumentEvent event) throws BadLocationException {
		return event.getDocument().getLineOfOffset(event.getOffset());
	}

	static int getEndLine(final DocumentEvent event, final boolean documentAboutToBeChanged) throws BadLocationException {
		final int length = documentAboutToBeChanged ? event.getLength() : event.getText().length();
		return event.getDocument().getLineOfOffset(event.getOffset() + length);
	}

	static boolean isRemove(final DocumentEvent event) {
		return event.getText() == null || event.getText().isEmpty();
	}

	static boolean isInsert(final DocumentEvent event) {
		return event.getLength() == 0 && event.getText() != null;
	}

	static String getLineText(final IDocument document, final int line, final boolean withLineDelimiter)
			throws BadLocationException {
		final int lo = document.getLineOffset(line);
		int ll = document.getLineLength(line);
		if (!withLineDelimiter) {
			final String delim = document.getLineDelimiter(line);
			ll = ll - (delim != null ? delim.length() : 0);
		}
		return document.get(lo, ll);
	}

	private static IRegion getRegion(final IDocument document, final int fromLine, final int toLine) throws BadLocationException {
		final int startOffset = document.getLineOffset(fromLine);
		final int endOffset = document.getLineOffset(toLine) + document.getLineLength(toLine);
		return new Region(startOffset, endOffset - startOffset);
	}
}
