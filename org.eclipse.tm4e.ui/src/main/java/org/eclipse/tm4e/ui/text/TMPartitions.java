/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;

/**
 * Constants for the TM partitioning and partition types.
 */
public final class TMPartitions {

	private static final IContentType[] NO_CONTENT_TYPES = new IContentType[0];

	/** The identifier for TM partitioning to be used with e.g. {@link IDocumentExtension3#getDocumentPartitioner(String)} */
	public static final String TM_PARTITIONING = "tm4e.partitioning";

	public static final String PARTITION_TYPE_PREFIX = "tm4e:";

	/** Fallback/base partition type for the document's main language scope. */
	public static final String BASE_PARTITION_TYPE = PARTITION_TYPE_PREFIX + "base";

	/**
	 * @return true if the given document has a TM4E partitioner installed.
	 */
	public static boolean hasPartitioning(final @Nullable IDocument doc) {
		return doc instanceof final IDocumentExtension3 ext3 //
				&& ext3.getDocumentPartitioner(TMPartitions.TM_PARTITIONING) != null;
	}

	/**
	 * @return the TM4E partition at the given offset or <code>null</code> when no TM partitioner is installed.
	 */
	public static @Nullable ITMPartitionRegion getPartition(final IDocument doc, final int offset) {
		if (doc instanceof final IDocumentExtension3 ext3
				&& ext3.getDocumentPartitioner(TMPartitions.TM_PARTITIONING) instanceof final ITMPartitioner tmPartitioner)
			return tmPartitioner.getPartition(offset);
		return null;
	}

	/**
	 * Resolves content types associated with the TM4E partition at the given offset.
	 * Returns an empty array when no mapping exists or no TM partitioner is installed.
	 */
	public static IContentType[] getContentTypesForOffset(final IDocument doc, final int offset) {
		final ITypedRegion part = getPartition(doc, offset);
		if (part == null)
			return NO_CONTENT_TYPES;

		if (!(part instanceof final ITMPartitionRegion tmPart))
			return NO_CONTENT_TYPES;

		final String scopeName = tmPart.getGrammarScope();
		final var mgr = TMEclipseRegistryPlugin.getGrammarRegistryManager();
		// Try direct lookup (works when scope was contributed unqualified)
		final Collection<IContentType> cts = mgr.getContentTypesForScope(ITMScope.parse(scopeName));
		if (cts != null && !cts.isEmpty())
			return cts.toArray(IContentType[]::new);

		// Fallback: match by unqualified scope name across all definitions and union their content types
		final List<IContentType> result = new ArrayList<>();
		for (final IGrammarDefinition def : mgr.getDefinitions()) {
			final var defScope = def.getScope();
			if (scopeName.equals(defScope.getName())) {
				final Collection<IContentType> mapped = mgr.getContentTypesForScope(defScope);
				if (mapped != null && !mapped.isEmpty()) {
					result.addAll(mapped);
				}
			}
		}
		return result.isEmpty() ? NO_CONTENT_TYPES : result.toArray(IContentType[]::new);
	}

	private TMPartitions() {
	}
}
