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
public class DocumentHelper {

	/**
	 * Returns the number of lines in this document.
	 * <p>
	 * Note that a document always has at least one line.
	 * </p>
	 * 
	 * @return the number of lines in this document.
	 */
	public static int getNumberOfLines(IDocument document) {
		return document.getNumberOfLines();
	}

	public static int getStartLine(DocumentEvent event) throws BadLocationException {
		return event.getDocument().getLineOfOffset(event.getOffset());
	}

	public static int getEndLine(DocumentEvent event, boolean documentAboutToBeChanged) throws BadLocationException {
		int length = documentAboutToBeChanged ? event.getLength() : event.getText().length();
		return event.getDocument().getLineOfOffset(event.getOffset() + length);
	}

	public static boolean isRemove(DocumentEvent event) {
		return event.getText() == null || event.getText().length() == 0;
	}

	public static boolean isInsert(DocumentEvent event) {
		return event.getLength() == 0 && event.getText() != null;
	}

	public static String getLineText(IDocument document, int line, boolean withLineDelimiter)
			throws BadLocationException {
		int lo = document.getLineOffset(line);
		int ll = document.getLineLength(line);
		if (!withLineDelimiter) {
			String delim = document.getLineDelimiter(line);
			ll = ll - (delim != null ? delim.length() : 0);
		}
		return document.get(lo, ll);
	}

	public static int getLineLength(IDocument document, int line) throws BadLocationException {
		// String delim = document.getLineDelimiter(line);
		return document.getLineLength(line); // - (delim != null ?
												// delim.length() : 0);
	}

	public static IRegion getRegion(IDocument document, int fromLine, int toLine) throws BadLocationException {
		int startOffset = document.getLineOffset(fromLine);
		int endOffset = document.getLineOffset(toLine) + document.getLineLength(toLine);
		return new Region(startOffset, endOffset - startOffset);
	}
}
