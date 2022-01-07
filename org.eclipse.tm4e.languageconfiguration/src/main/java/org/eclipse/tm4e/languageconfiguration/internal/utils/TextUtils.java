/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TabsToSpacesConverter;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.tm4e.ui.utils.ClassHelper;

public class TextUtils {

	/**
	 * Returns true if text of the command is an enter and false otherwise.
	 * 
	 * @param d
	 * @param c
	 * @return true if text of the command is an enter and false otherwise.
	 */
	public static boolean isEnter(IDocument d, DocumentCommand c) {
		return (c.length == 0 && c.text != null && TextUtilities.equals(d.getLegalLineDelimiters(), c.text) != -1);
	}

	public static String normalizeIndentation(String str, int tabSize, boolean insertSpaces) {
		int firstNonWhitespaceIndex = TextUtils.firstNonWhitespaceIndex(str);
		if (firstNonWhitespaceIndex == -1) {
			firstNonWhitespaceIndex = str.length();
		}
		return TextUtils.normalizeIndentationFromWhitespace(str.substring(0, firstNonWhitespaceIndex), tabSize,
				insertSpaces) + str.substring(firstNonWhitespaceIndex);
	}

	private static String normalizeIndentationFromWhitespace(String str, int tabSize, boolean insertSpaces) {
		int spacesCnt = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\t') {
				spacesCnt += tabSize;
			} else {
				spacesCnt++;
			}
		}

		StringBuilder result = new StringBuilder();
		if (!insertSpaces) {
			long tabsCnt = Math.round(Math.floor(spacesCnt / tabSize));
			spacesCnt = spacesCnt % tabSize;
			for (int i = 0; i < tabsCnt; i++) {
				result.append('\t');
			}
		}

		for (int i = 0; i < spacesCnt; i++) {
			result.append(' ');
		}

		return result.toString();
	}

	/**
	 * Returns first index of the string that is not whitespace. If string is empty
	 * or contains only whitespaces, returns -1
	 */
	public static int firstNonWhitespaceIndex(String str) {
		for (int i = 0, len = str.length(); i < len; i++) {
			char c = str.charAt(i);
			if (c != ' ' && c != '\t') {
				return i;
			}
		}
		return -1;
	}

	public static String getIndentationAtPosition(IDocument d, int offset) {
		try {
			// find start of line
			int p = offset;
			IRegion info = d.getLineInformationOfOffset(p);
			int start = info.getOffset();

			// find white spaces
			int end = findEndOfWhiteSpace(d, start, offset);

			return d.get(start, end - start);
		} catch (BadLocationException excp) {
			// stop work
		}
		return "";
	}

	/**
	 * Returns the first offset greater than <code>offset</code> and smaller than
	 * <code>end</code> whose character is not a space or tab character. If no such
	 * offset is found, <code>end</code> is returned.
	 *
	 * @param document
	 *            the document to search in
	 * @param offset
	 *            the offset at which searching start
	 * @param end
	 *            the offset at which searching stops
	 * @return the offset in the specified range whose character is not a space or
	 *         tab
	 * @exception BadLocationException
	 *                if position is an invalid range in the given document
	 */
	private static int findEndOfWhiteSpace(IDocument document, int offset, int end) throws BadLocationException {
		while (offset < end) {
			char c = document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}

	public static TabSpacesInfo getTabSpaces(ITextViewer viewer) {
		TabsToSpacesConverter converter = ClassHelper.getFieldValue(viewer, "fTabsToSpacesConverter", TextViewer.class);
		if (converter != null) {
			int tabSize = ClassHelper.getFieldValue(converter, "fTabRatio", TabsToSpacesConverter.class);
			return new TabSpacesInfo(tabSize, true);
		}
		return new TabSpacesInfo(-1, false);
	}

}
