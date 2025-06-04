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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.tm4e.core.internal.utils.MoreCollections;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.ui.internal.model.DocumentHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;

/**
 * Folding strategy based on folding markers in TextMate grammars or Language Configurations.
 */
public final class TMFoldingStrategy extends AbstractFoldingStrategy {

	private static final class TMFoldingAnno extends ProjectionAnnotation {
		TMFoldingAnno() {
			super(false /* expanded by default */);
		}
	}

	private record FoldingRange(int startLineIndex, int endLineIndex) {
	}

	private @Nullable ContentTypeInfo contentTypeInfo;

	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final @Nullable IRegion subRegion) {
		final var document = this.document;
		final var annoModel = projectionAnnotationModel;
		final var contentTypeInfo = this.contentTypeInfo;
		if (document == null || annoModel == null || contentTypeInfo == null)
			return;

		final var folding = FoldingSupport.getFoldingRules(contentTypeInfo);
		if (folding == null)
			return;

		try {
			/*
			 * Compute folding ranges
			 */
			final int startLineIndex = 0; // always start scanning from line 0 (not just the dirty region) so that an end‐marker
											// in the changed area can still match a start‐marker that appears earlier in the document
			final int endLineIndexExclusive = document.getNumberOfLines();

			final var foldingRanges = new ArrayList<FoldingRange>();
			final var openRanges = new ArrayList<Integer>();

			for (int lineIndex = startLineIndex; lineIndex < endLineIndexExclusive; lineIndex++) {
				if (document != this.document)
					return; // abort on changed document

				final String lineText = DocumentHelper.getLineText(document, lineIndex, false);
				if (folding.markers.start.matchesPartially(lineText)) {
					openRanges.add(lineIndex);
				} else if (folding.markers.end.matchesPartially(lineText) && !openRanges.isEmpty()) {
					foldingRanges.add(new FoldingRange(MoreCollections.removeLastElement(openRanges), lineIndex));
				}
			}

			// ignore single line foldingRanges
			foldingRanges.removeIf(r -> r.endLineIndex - r.startLineIndex == 0);

			/*
			 * Diff against existing annotations
			 */
			final var additions = new HashMap<TMFoldingAnno, Position>();
			final var deletions = new ArrayList<TMFoldingAnno>();
			final var newRanges = new HashSet<>(foldingRanges);

			// Iterate over existing annotations to find those that must be kept or removed
			final var scanOffset = document.getLineOffset(startLineIndex);
			final var scanLength = document.getLength() - scanOffset;
			for (final Iterator<Annotation> it = annoModel.getAnnotationIterator(scanOffset, scanLength, true, true); it.hasNext();) {
				if (document != this.document)
					return; // abort on changed document

				if (!(it.next() instanceof final TMFoldingAnno anno)) {
					continue; // ignore foreign annotations
				}

				final Position pos = annoModel.getPosition(anno);
				if (pos == null || pos.getLength() == 0) { // zero-length = bogus
					deletions.add(anno);
					continue;
				}

				try {
					final int annStartLineIndex = document.getLineOfOffset(pos.getOffset());
					// + (length - 1) because last char belongs to the range
					final int annEndLineIndex = document.getLineOfOffset(pos.getOffset() + pos.getLength() - 1);
					final var existing = new FoldingRange(annStartLineIndex, annEndLineIndex);
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
					additions.put(new TMFoldingAnno(), new Position(startOffset, endOffset - startOffset));
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

	@Override
	public void setDocument(final @Nullable IDocument doc) {
		super.setDocument(doc);
		contentTypeInfo = doc == null ? null : ContentTypeHelper.findContentTypes(doc);
	}
}
