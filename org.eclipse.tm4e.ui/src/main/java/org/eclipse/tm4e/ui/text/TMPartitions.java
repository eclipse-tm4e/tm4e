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
package org.eclipse.tm4e.ui.text;

import java.util.Collection;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.registry.internal.GrammarRegistryManager;
import org.eclipse.tm4e.ui.internal.text.TMPartitioner;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.tm4e.ui.internal.utils.FileLanguageSelection;

/**
 * Defines TM partition IDs and finds the content types used to apply editing rules within each partition.
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
		final ITMPartitionRegion part = getPartition(doc, offset);
		if (part == null)
			return NO_CONTENT_TYPES;

		final ContentTypeInfo info = ContentTypeHelper.findContentTypes(doc);
		return getContentTypes(doc, part, info == null ? NO_CONTENT_TYPES : info.getContentTypes());
	}

	/**
	 * Resolves partition content types without repeating document content detection.
	 * Returns an empty array when no mapping exists or no TM partitioner is installed.
	 *
	 * @param documentContentTypes the content types selected for the document, including any user binding
	 */
	public static IContentType[] getContentTypesForOffset(final IDocument doc, final int offset,
			final IContentType[] documentContentTypes) {
		final ITMPartitionRegion part = getPartition(doc, offset);
		return part == null ? NO_CONTENT_TYPES : getContentTypes(doc, part, documentContentTypes);
	}

	private static IContentType[] getContentTypes(final IDocument doc, final ITMPartitionRegion part,
			final IContentType[] documentContentTypes) {
		final String scopeName = part.getGrammarScope();
		final ContentTypeInfo fileSelection = FileLanguageSelection.getForDocument(doc);
		final IGrammar fileGrammar = fileSelection == null ? null : fileSelection.getExplicitGrammar();
		// A file choice takes priority over workspace bindings, including for its editing rules.
		// Partition types drop scope suffixes such as .jsx. Use the partitioner's mapping for both file and workspace choices.
		// Other partition types keep their own bindings so embedded languages retain their editing rules.
		if (fileSelection != null && fileGrammar != null
				&& part.getType().equals(TMPartitioner.scopeToPartitionType(fileGrammar.getScopeName())))
			return fileSelection.getContentTypes();
		final var mgr = TMEclipseRegistryPlugin.getGrammarRegistryManager();
		// Several content types can bind to the same scope. Only use the binding selected for this document.
		if (documentContentTypes.length == 1) {
			final IGrammarDefinition binding = mgr.getUserGrammarBinding(documentContentTypes[0]);
			if (binding != null && part.getType().equals(TMPartitioner.scopeToPartitionType(binding.getScope().getName())))
				return documentContentTypes;
		}

		// Partition scopes have no plugin IDs. Find plugin bindings for the document and its embedded languages,
		// including bindings contributed by a bundle other than the grammar's.
		// Ignore user bindings for other documents, even when they use the same scope.
		final Collection<IContentType> result = GrammarRegistryManager.getInstance().getContributedContentTypesForScope(scopeName);
		return result.isEmpty() //
				? NO_CONTENT_TYPES
				: result.toArray(IContentType[]::new);
	}

	private TMPartitions() {
	}
}
