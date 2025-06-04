/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.folding;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextEditorPrefs;
import org.eclipse.tm4e.ui.internal.model.DocumentHelper;

/**
 * Folding strategy that derives foldable regions from indentation levels.
 * <p>
 * A new folding range starts whenever the indentation of the next non-blank line increases compared to the current line.
 * The range ends one line <em>before</em> the indentation decreases back to the same or lower level.
 * Blank lines are ignored for indentation comparison.
 * </p>
 */
public final class IndentationFoldingStrategy extends AbstractFoldingStrategy {

	private static final class IndentationFoldingAnno extends ProjectionAnnotation {
		IndentationFoldingAnno() {
			super(false /* expanded by default */);
		}
	}

	private record FoldingRange(int startLineIndex, int endLineIndex) {
	}

	/** Represents an indentation block by recording its indentation level and the line index where that block began. */
	private record IndentationBlock(int startLineIndex, int indentLevel) {
	}

	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final @Nullable IRegion subRegion) {
		final var document = this.document;
		final var annoModel = projectionAnnotationModel;
		final var textViewer = this.textViewer;
		if (document == null || annoModel == null || textViewer == null)
			return;

		final int tabSize = TextEditorPrefs.getCursorConfiguration(null).indentSize;
		if (tabSize < 1)
			return; // widget disposed meanwhile

		try {
			/*
			 * Compute folding ranges
			 */
			final int startLineIndex = 0;
			final int endLineIndexExclusive = document.getNumberOfLines();
			final int endLineIndex = endLineIndexExclusive - 1;

			final var foldingRanges = new ArrayList<FoldingRange>();
			final var openRanges = new ArrayDeque<IndentationBlock>();

			int prevIndent = -1;
			int prevLineIdx = -1;

			for (int lineIndex = startLineIndex; lineIndex < endLineIndexExclusive; lineIndex++) {
				if (document != this.document)
					return; // abort on changed document

				final String lineText = DocumentHelper.getLineText(document, lineIndex, false);
				final boolean isBlank = lineText.isBlank();
				final int indentLevel = isBlank ? prevIndent : getIndentLevel(lineText, tabSize);

				if (!isBlank) {
					if (prevIndent >= 0 && prevLineIdx >= 0 && indentLevel > prevIndent) {
						// increased indent -> remember block starting at previous line
						openRanges.push(new IndentationBlock(prevLineIdx, prevIndent));
					} else if (indentLevel < prevIndent) {
						// decreased indent -> close blocks whose indent >= new indent
						while (!openRanges.isEmpty() && castNonNull(openRanges.peek()).indentLevel >= indentLevel) {
							final IndentationBlock block = openRanges.pop();
							final int startL = block.startLineIndex;
							final int endL = lineIndex - 1;
							if (endL > startL) {
								foldingRanges.add(new FoldingRange(startL, endL));
							}
						}
					}
					prevIndent = indentLevel;
					prevLineIdx = lineIndex;
				}
			}

			// close remaining open blocks at EOF
			while (!openRanges.isEmpty()) {
				final IndentationBlock block = openRanges.pop();
				final int startL = block.startLineIndex;
				final int endL = endLineIndex;
				if (endL > startL) {
					foldingRanges.add(new FoldingRange(startL, endL));
				}
			}

			// ignore single line foldingRanges
			foldingRanges.removeIf(r -> r.endLineIndex - r.startLineIndex == 0);

			/*
			 * Diff against existing annotations
			 */
			final var additions = new HashMap<IndentationFoldingAnno, Position>();
			final var deletions = new ArrayList<IndentationFoldingAnno>();
			final var newRanges = new HashSet<>(foldingRanges);

			// Iterate over existing annotations to find those that must be kept or removed
			final var scanOffset = document.getLineOffset(startLineIndex);
			final var scanLength = document.getLength() - scanOffset;
			for (final Iterator<Annotation> it = annoModel.getAnnotationIterator(scanOffset, scanLength, true, true); it.hasNext();) {
				if (document != this.document)
					return; // abort on changed document

				if (!(it.next() instanceof final IndentationFoldingAnno anno)) {
					continue; // ignore foreign annotations
				}

				final Position pos = annoModel.getPosition(anno);
				if (pos == null || pos.getLength() == 0) { // zero-length = bogus
					deletions.add(anno);
					continue;
				}

				try {
					final int annStartLine = document.getLineOfOffset(pos.getOffset());
					// + (length - 1) because last char belongs to the range
					final int annEndLine = document.getLineOfOffset(pos.getOffset() + pos.getLength() - 1);
					final var existing = new FoldingRange(annStartLine, annEndLine);
					if (newRanges.remove(existing)) {
						continue; // still valid -> keep
					}
					deletions.add(anno); // stale -> delete
				} catch (final BadLocationException ex) {
					// unable to map position -> recreate via deletions/additions
					deletions.add(anno);
				}
			}

			// Create Position + Annotation objects for new ranges
			for (final FoldingRange range : newRanges) {
				try {
					final int startOffset = document.getLineOffset(range.startLineIndex);
					final int endOffset = document.getLineOffset(range.endLineIndex) + document.getLineLength(range.endLineIndex);
					additions.put(new IndentationFoldingAnno(), new Position(startOffset, endOffset - startOffset));
				} catch (final BadLocationException ex) {
					LanguageConfigurationPlugin.logError(ex);
				}
			}

			/*
			 * Apply changes to the annotation model
			 */
			if (document != this.document)
				return; // abort on changed document
			modifyAnnotations(deletions, additions, List.of());
		} catch (final BadLocationException ex) {
			LanguageConfigurationPlugin.logError(ex);
		}
	}

	/**
	 * Computes the indentation level of a line based on leading whitespace.
	 */
	private static int getIndentLevel(final String lineText, final int tabSize) {
		int indent = 0;
		for (int i = 0, len = lineText.length(); i < len; i++) {
			final char ch = lineText.charAt(i);
			if (ch == ' ') {
				indent++;
			} else if (ch == '\t') {
				indent += tabSize;
			} else {
				break;
			}
		}
		return indent;
	}
}
