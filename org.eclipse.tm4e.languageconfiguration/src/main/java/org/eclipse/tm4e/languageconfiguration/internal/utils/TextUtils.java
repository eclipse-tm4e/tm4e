/**
 * Copyright (c) 2015-2022 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Sebastian Thomschke (Vegard IT) - added methods replaceIndent, and isEmptyLine
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

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

	public static String getLeadingWhitespace(final IDocument doc, final int lineIndex) throws BadLocationException {
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

	public static CharSequence replaceIndent(final CharSequence multiLineString, final int tabSize, final String newIndent,
			final boolean indentEmptyLines) {
		final int effectiveTabSize = Math.max(1, tabSize);

		abstract class CharConsumer implements IntConsumer {

			char prevChar = 0;

			@Override
			public void accept(final int value) {
				final char ch = (char) value;
				onChar(ch);
				prevChar = ch;
			}

			abstract void onChar(char ch);
		}

		/*
		 * determine common indentation of all lines
		 */
		final class IndentDetector extends CharConsumer implements IntPredicate {
			int existingIndent = Integer.MAX_VALUE;
			int indentOfLine = 0;
			boolean isEmptyLine = true;
			boolean skipToLineEnd = false;
			int lineCount = 1;

			@Override
			void onChar(final char ch) {
				// handle new line chars
				if (ch == '\n' || ch == '\r') {
					if (ch == '\n' && prevChar == '\r'
							|| ch == '\r' && prevChar == '\n') {
						return;
					}
					lineCount++;
					skipToLineEnd = false;
					if (!isEmptyLine && indentOfLine < existingIndent)
						existingIndent = indentOfLine;
					indentOfLine = 0;
					isEmptyLine = true;
					return;
				}

				// handle other chars
				isEmptyLine = false;
				if (!skipToLineEnd) {
					if (ch == '\t') {
						indentOfLine += effectiveTabSize;
					} else if (Character.isWhitespace(ch)) {
						indentOfLine++;
					} else {
						skipToLineEnd = true;
					}
				}
			}

			@Override
			public boolean test(final int value) {
				return existingIndent > 0;
			}
		}

		final var indentDetector = new IndentDetector();
		multiLineString.chars().takeWhile(indentDetector).forEach(indentDetector);

		final var existingIndent = indentDetector.isEmptyLine
				? indentDetector.existingIndent
				: Math.min(indentDetector.indentOfLine, indentDetector.existingIndent);
		if (existingIndent == 0 && newIndent.isEmpty())
			return multiLineString;

		/*
		 * replace common indentation of all lines
		 */
		final var sb = new StringBuilder(Math.max(0, multiLineString.length() - indentDetector.lineCount * existingIndent));
		final class IdentReplacer extends CharConsumer {
			int skippedIndentOfLine = 0;
			boolean isEmptyLine = true;

			@Override
			public void onChar(final char ch) {
				if (ch == '\r')
					return;

				if (ch == '\n') {
					if (isEmptyLine && indentEmptyLines) {
						sb.append(newIndent);
					}
					if (prevChar == '\r') {
						sb.append('\r');
					}
					sb.append(ch);
					skippedIndentOfLine = 0;
					isEmptyLine = true;
					return;
				}

				if (skippedIndentOfLine >= existingIndent) {
					if (isEmptyLine) {
						sb.append(newIndent);
						isEmptyLine = false;
					}
					sb.append(ch);
				} else {
					if (ch == '\t') {
						skippedIndentOfLine += effectiveTabSize;
					} else {
						skippedIndentOfLine++;
					}
				}
			}
		}

		final var indentReplacer = new IdentReplacer();
		multiLineString.chars().forEach(indentReplacer);

		// special case
		if (indentEmptyLines && sb.isEmpty() && !multiLineString.isEmpty()) {
			sb.append(newIndent);
		}
		return sb;
	}

	private TextUtils() {
	}
}
