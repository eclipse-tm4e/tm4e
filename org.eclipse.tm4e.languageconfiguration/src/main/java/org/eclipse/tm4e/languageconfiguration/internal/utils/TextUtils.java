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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.internal.model.CursorConfiguration;

public final class TextUtils {

	/**
	 * @return true if text of the command is an enter and false otherwise.
	 */
	public static boolean isEnter(final IDocument doc, final DocumentCommand cmd) {
		return cmd.length == 0 && cmd.text != null && isLegalLineDelimiter(doc, cmd.text);
	}

	private static boolean isLegalLineDelimiter(final IDocument doc, final String delimiter) {
		if (delimiter.length() > 2)
			return false;
		for (final String d : doc.getLegalLineDelimiters()) {
			if (d.equals(delimiter))
				return true;
		}
		return false;
	}

	public static String getIndentationFromWhitespace(final String whitespace, final CursorConfiguration cursorCfg) {
		final var tab = "\t";
		int indentOffset = 0;
		boolean startsWithTab = true;
		boolean startsWithSpaces = true;
		final String spaces = cursorCfg.insertSpaces
				? " ".repeat(cursorCfg.indentSize)
				: "";
		while (startsWithTab || startsWithSpaces) {
			startsWithTab = whitespace.startsWith(tab, indentOffset);
			startsWithSpaces = cursorCfg.insertSpaces && whitespace.startsWith(spaces, indentOffset);
			if (startsWithTab) {
				indentOffset += tab.length();
			}
			if (startsWithSpaces) {
				indentOffset += spaces.length();
			}
		}
		return whitespace.substring(0, indentOffset);
	}

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/languageConfigurationRegistry.ts">
	 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfigurationRegistry.ts</a>
	 */
	public static String getIndentationAtPosition(final IDocument doc, final int offset) {
		try {
			// find start offset of current line
			final int lineStartOffset = doc.getLineInformationOfOffset(offset).getOffset();

			// find white spaces
			final int indentationEndOffset = findEndOfWhiteSpace(doc, lineStartOffset, offset);

			return doc.get(lineStartOffset, indentationEndOffset - lineStartOffset);
		} catch (final BadLocationException excp) {
			return "";
		}
	}

	/**
	 * Returns the first offset greater than <code>offset</code> and smaller than
	 * <code>end</code> whose character is not a space or tab character. If no such
	 * offset is found, <code>end</code> is returned.
	 *
	 * @param doc the document to search in
	 * @param startAt the offset at which searching start
	 * @param endAt the offset at which searching stops
	 *
	 * @return the offset in the specified range whose character is not a space or tab
	 *
	 * @exception BadLocationException if position is an invalid range in the given document
	 */
	private static int findEndOfWhiteSpace(final IDocument doc, final int startAt, final int endAt) throws BadLocationException {
		for (int i = startAt; i < endAt; i++) {
			final char ch = doc.getChar(i);
			if (ch != ' ' && ch != '\t') {
				return i;
			}
		}
		return endAt;
	}

	public static String getLeadingWhitespace(final IDocument doc, int lineIndex) throws BadLocationException {
		final int lineStartOffset = doc.getLineOffset(lineIndex);
		final int lineLength = doc.getLineLength(lineIndex);
		return doc.get(lineStartOffset, findEndOfWhiteSpace(doc, lineStartOffset, lineStartOffset + lineLength) - lineStartOffset);
	}

	/**
	 * Determines if all the characters at any offset of the specified document line are the whitespace characters.
	 *
	 * @param doc the document to search in
	 * @param lineIndex zero-based document line number
	 *
	 * @return <code>true</code> if all the characters of the specified document line are the whitespace
	 *         characters, otherwise returns <code>false</code>
	 */
	public static boolean isBlankLine(final IDocument doc, final int lineIndex) {
		try {
			int offset = doc.getLineOffset(lineIndex);
			final int lineEnd = offset + doc.getLineLength(lineIndex);
			while (offset < lineEnd) {
				if (!Character.isWhitespace(doc.getChar(offset))) {
					return false;
				}
				offset++;
			}
		} catch (final BadLocationException e) {
			// Ignore, forcing a positive result
		}
		return true;
	}

	public static boolean isEmptyLine(final IDocument doc, final int lineIndex) {
		try {
			final int lineLength = doc.getLineLength(lineIndex);
			if (lineLength > 2)
				return false;
			if (lineLength == 0)
				return true;

			final int lineOffset = doc.getLineOffset(lineIndex);
			return isLegalLineDelimiter(doc, doc.get(lineOffset, lineLength));
		} catch (final BadLocationException e) {
			// Ignore, forcing a positive result
			return true;
		}
	}

	private TextUtils() {
	}
}
