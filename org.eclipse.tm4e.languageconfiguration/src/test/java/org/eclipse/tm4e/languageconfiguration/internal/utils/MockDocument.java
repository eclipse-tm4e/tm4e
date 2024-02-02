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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault({})
public class MockDocument implements IDocument {

	private String contentType;
	private String text;

	public MockDocument(final String contentType, final String text) {
		this.contentType = contentType;
		this.text = text;
	}

	@Override
	public void addDocumentListener(IDocumentListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDocumentPartitioningListener(IDocumentPartitioningListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPosition(Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPosition(String category, Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPositionCategory(String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPositionUpdater(IPositionUpdater updater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPrenotifiedDocumentListener(IDocumentListener documentAdapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int computeIndexInCategory(String category, int offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int computeNumberOfLines(String text) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsPosition(String category, int offset, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsPositionCategory(String category) {
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
		} catch (IndexOutOfBoundsException ex) {
			throw new BadLocationException(ex.getMessage());
		}
	}

	@Override
	public char getChar(final int offset) throws BadLocationException {
		try {
			return this.text.charAt(offset);
		} catch (IndexOutOfBoundsException ex) {
			throw new BadLocationException(ex.getMessage());
		}
	}

	@Override
	public String getContentType(int offset) {
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
	public String getLineDelimiter(final int lineIndex) throws BadLocationException {
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
		} catch (IOException ex) {
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
	public ITypedRegion getPartition(int offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getPositionCategories() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Position[] getPositions(String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPositionUpdater[] getPositionUpdaters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void insertPositionUpdater(IPositionUpdater updater, int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeDocumentListener(IDocumentListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeDocumentPartitioningListener(IDocumentPartitioningListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePosition(Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePosition(String category, Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePositionCategory(String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePositionUpdater(IPositionUpdater updater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePrenotifiedDocumentListener(IDocumentListener documentAdapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replace(final int offset, final int length, final String text) throws BadLocationException {
		try {
			this.text = new StringBuilder(this.text).replace(offset, offset + length, text).toString();
		} catch (IndexOutOfBoundsException ex) {
			throw new BadLocationException(ex.getMessage());
		}
	}

	@Override
	public int search(int startOffset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(final String text) {
		this.text = text;
	}

	@Override
	public void setDocumentPartitioner(IDocumentPartitioner partitioner) {
		throw new UnsupportedOperationException();
	}

}
