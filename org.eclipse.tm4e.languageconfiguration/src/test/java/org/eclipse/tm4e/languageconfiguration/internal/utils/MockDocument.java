/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;

/**
 * Partially implemented {@link IDocument}. Especially all methods related to {@link IDocumentListener}, {@link IDocumentPartitioner},
 * {@link Position} are not implemented.
 */
public class MockDocument implements IDocument {

	private final String contentType;
	private String text;

	public MockDocument(final String contentType, final String text) {
		this.contentType = contentType;
		this.text = text;
	}

	public int getOffsetAfter(final String searchFor) {
		final var idx = text.indexOf(searchFor);
		if (idx > -1)
			return idx + searchFor.length();
		return idx;
	}

	public int getOffsetOf(final String searchFor) {
		return text.indexOf(searchFor);
	}

	@Override
	public void addDocumentListener(final IDocumentListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDocumentPartitioningListener(final IDocumentPartitioningListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPosition(final Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPosition(final String category, final Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPositionCategory(final String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPositionUpdater(final IPositionUpdater updater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPrenotifiedDocumentListener(final IDocumentListener documentAdapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int computeIndexInCategory(final String category, final int offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int computeNumberOfLines(final String text) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ITypedRegion[] computePartitioning(final int offset, final int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsPosition(final String category, final int offset, final int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsPositionCategory(final String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String get() {
		return this.text;
	}

	@Override
	public String get(final int offset, final int length) throws BadLocationException {
		try {
			return this.text.substring(offset, offset + length);
		} catch (final IndexOutOfBoundsException ex) {
			throw new BadLocationException(ex.getMessage());
		}
	}

	@Override
	public char getChar(final int offset) throws BadLocationException {
		try {
			return this.text.charAt(offset);
		} catch (final IndexOutOfBoundsException ex) {
			throw new BadLocationException(ex.getMessage());
		}
	}

	@Override
	public String getContentType(final int offset) {
		return contentType;
	}

	@Override
	public IDocumentPartitioner getDocumentPartitioner() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getLegalContentTypes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getLegalLineDelimiters() {
		return DefaultLineTracker.DELIMITERS;
	}

	@Override
	public int getLength() {
		return this.text.length();
	}

	@Override
	public @Nullable String getLineDelimiter(final int lineIndex) throws BadLocationException {
		final String[] lines = text.split("\n");
		if (lines.length <= lineIndex)
			throw new BadLocationException("Line " + lineIndex + " not present.");
		return lines[lineIndex].endsWith("\r") ? "\r\n" : "\n";
	}

	@Override
	public IRegion getLineInformation(final int lineIndex) throws BadLocationException {
		return new Region(getLineOffset(lineIndex), getLineLength(lineIndex));
	}

	@Override
	public IRegion getLineInformationOfOffset(final int offset) throws BadLocationException {
		final var lineIndex = getLineOfOffset(offset);
		return new Region(getLineOffset(lineIndex), getLineLength(lineIndex));
	}

	@Override
	public int getLineLength(final int lineIndex) throws BadLocationException {
		final String[] lines = text.split("\n");
		if (lines.length <= lineIndex)
			throw new BadLocationException("Line " + lineIndex + " not present.");
		return lineIndex == lines.length - 1
				? lines[lineIndex].length()
				: lines[lineIndex].length() + 1;
	}

	@Override
	public int getLineOffset(final int lineIndex) throws BadLocationException {
		final String[] lines = text.split("\n");
		if (lines.length <= lineIndex)
			throw new BadLocationException("Line " + lineIndex + " not present.");

		int offset = 0;
		for (int i = 0; i < lineIndex; i++) {
			offset += lines[i].length() + 1;
		}
		return offset;
	}

	@Override
	public int getLineOfOffset(final int offset) throws BadLocationException {
		try (var lineIndexReader = new LineNumberReader(new StringReader(text))) {
			lineIndexReader.skip(offset);
			return lineIndexReader.getLineNumber();
		} catch (final IOException ex) {
			throw new BadLocationException(ex.getMessage());
		}
	}

	@Override
	public int getNumberOfLines() {
		return (int) text.lines().count();
	}

	@Override
	public int getNumberOfLines(final int offset, final int length) throws BadLocationException {
		return (int) get(offset, length).lines().count();
	}

	@Override
	public ITypedRegion getPartition(final int offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getPositionCategories() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Position[] getPositions(final String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPositionUpdater[] getPositionUpdaters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void insertPositionUpdater(final IPositionUpdater updater, final int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeDocumentListener(final IDocumentListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeDocumentPartitioningListener(final IDocumentPartitioningListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePosition(final Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePosition(final String category, final Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePositionCategory(final String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePositionUpdater(final IPositionUpdater updater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePrenotifiedDocumentListener(final IDocumentListener documentAdapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replace(final int offset, final int length, final String text) throws BadLocationException {
		try {
			this.text = new StringBuilder(this.text).replace(offset, offset + length, text).toString();
		} catch (final IndexOutOfBoundsException ex) {
			throw new BadLocationException(ex.getMessage());
		}
	}

	@Override
	public int search(final int startOffset, final String findString, final boolean forwardSearch, final boolean caseSensitive,
			final boolean wholeWord) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(final String text) {
		this.text = text;
	}

	@Override
	public void setDocumentPartitioner(@Nullable final IDocumentPartitioner partitioner) {
		throw new UnsupportedOperationException();
	}

}
