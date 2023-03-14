/**
 * Copyright (c) 2015-2022 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TabsToSpacesConverter;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.tm4e.ui.internal.utils.ClassHelper;

public final class TextUtils {

	/**
	 * Returns true if text of the command is an enter and false otherwise.
	 *
	 * @param d
	 * @param c
	 *
	 * @return true if text of the command is an enter and false otherwise.
	 */
	public static boolean isEnter(final IDocument d, final DocumentCommand c) {
		return c.length == 0 && c.text != null && TextUtilities.equals(d.getLegalLineDelimiters(), c.text) != -1;
	}

	public static String normalizeIndentation(final String str, final int tabSize, final boolean insertSpaces) {
		int firstNonWhitespaceIndex = TextUtils.firstNonWhitespaceIndex(str);
		if (firstNonWhitespaceIndex == -1) {
			firstNonWhitespaceIndex = str.length();
		}
		return TextUtils.normalizeIndentationFromWhitespace(str.substring(0, firstNonWhitespaceIndex), tabSize,
				insertSpaces) + str.substring(firstNonWhitespaceIndex);
	}

	private static String normalizeIndentationFromWhitespace(final String str, final int tabSize,
			final boolean insertSpaces) {
		int spacesCnt = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\t') {
				spacesCnt += tabSize;
			} else {
				spacesCnt++;
			}
		}

		final var result = new StringBuilder();
		if (!insertSpaces) {
			final long tabsCnt = Math.round(Math.floor(spacesCnt / tabSize));
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
	 * Returns the start of the string at the offset in the text. If the string is
	 * not in the text at the offset, returns -1.</br>
	 * Ex: </br>
	 * text = "apple banana", offset=8, string="banana" returns=6
	 */
	public static int startIndexOfOffsetTouchingString(final String text, final int offset, final String string) {
		int start = offset - string.length();
		start = start < 0 ? 0 : start;
		int end = offset + string.length();
		end = end >= text.length() ? text.length() : end;
		try {
			final int indexInSubtext = text.substring(start, end).indexOf(string);
			return indexInSubtext == -1 ? -1 : start + indexInSubtext;
		} catch (final IndexOutOfBoundsException e) {
			return -1;
		}
	}

	/**
	 * Returns first index of the string that is not whitespace. If string is empty
	 * or contains only whitespaces, returns -1
	 */
	private static int firstNonWhitespaceIndex(final String str) {
		for (int i = 0, len = str.length(); i < len; i++) {
			final char c = str.charAt(i);
			if (c != ' ' && c != '\t') {
				return i;
			}
		}
		return -1;
	}

	public static String getIndentationFromWhitespace(final String whitespace, final TabSpacesInfo tabSpaces) {
		final var tab = "\t"; //$NON-NLS-1$
		int indentOffset = 0;
		boolean startsWithTab = true;
		boolean startsWithSpaces = true;
		final String spaces = tabSpaces.isInsertSpaces()
				? " ".repeat(tabSpaces.getTabSize())
				: "";
		while (startsWithTab || startsWithSpaces) {
			startsWithTab = whitespace.startsWith(tab, indentOffset);
			startsWithSpaces = tabSpaces.isInsertSpaces() && whitespace.startsWith(spaces, indentOffset);
			if (startsWithTab) {
				indentOffset += tab.length();
			}
			if (startsWithSpaces) {
				indentOffset += spaces.length();
			}
		}
		return whitespace.substring(0, indentOffset);
	}

	public static String getLinePrefixingWhitespaceAtPosition(final IDocument d, final int offset) {
		try {
			// find start of line
			final int p = offset;
			final IRegion info = d.getLineInformationOfOffset(p);
			final int start = info.getOffset();

			// find white spaces
			final int end = findEndOfWhiteSpace(d, start, offset);

			return d.get(start, end - start);
		} catch (final BadLocationException excp) {
			// stop work
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the first offset greater than <code>offset</code> and smaller than
	 * <code>end</code> whose character is not a space or tab character. If no such
	 * offset is found, <code>end</code> is returned.
	 *
	 * @param document the document to search in
	 * @param offset the offset at which searching start
	 * @param end the offset at which searching stops
	 *
	 * @return the offset in the specified range whose character is not a space or
	 *         tab
	 *
	 * @exception BadLocationException if position is an invalid range in the given document
	 */
	private static int findEndOfWhiteSpace(final IDocument document, int offset, final int end)
			throws BadLocationException {
		while (offset < end) {
			final char c = document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}

	public static TabSpacesInfo getTabSpaces(@Nullable final ITextViewer viewer) {
		if (viewer != null) {
			final TabsToSpacesConverter converter = ClassHelper.getFieldValue(viewer, "fTabsToSpacesConverter", //$NON-NLS-1$
					TextViewer.class);
			if (converter != null) {
				@SuppressWarnings("null")
				final int tabSize = ClassHelper.getFieldValue(converter, "fTabRatio", TabsToSpacesConverter.class); //$NON-NLS-1$
				return new TabSpacesInfo(tabSize, true);
			}
		}
		return new TabSpacesInfo(4, false);
	}

	/**
	 * Determines if all the characters at any offset of the specified document line are the whitespace characters.
	 *
	 * @param document the document to search in
	 * @param line zero-based document line number
	 *
	 * @return <code>true</code> if all the characters of the specified document line are the whitespace
	 *         characters, otherwise returns <code>false</code>
	 */
	public static boolean isBlankLine(final IDocument document, final int line) {
		try {
			int offset = document.getLineOffset(line);
			final int lineEnd = offset + document.getLineLength(line);
			while (offset < lineEnd) {
				if (!Character.isWhitespace(document.getChar(offset))) {
					return false;
				}
				offset++;
			}
		} catch (final BadLocationException e) {
			// Ignore, forcing a positive result
		}
		return true;
	}

	private TextUtils() {
	}
}
