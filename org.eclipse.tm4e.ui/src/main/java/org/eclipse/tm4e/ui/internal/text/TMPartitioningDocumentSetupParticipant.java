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
package org.eclipse.tm4e.ui.internal.text;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.text.TMPartitions;

/**
 * Auto-installs {@link TMPartitioner} under the tm4e.partitioning ID when a TextMate grammar can be resolved for the document.
 *
 * The default (editor) partitioner is not replaced; this adds a secondary partitioning that consumers can opt-in to use.
 */
public final class TMPartitioningDocumentSetupParticipant implements IDocumentSetupParticipant {

	@Override
	public void setup(final IDocument doc) {
		// avoid impacting non-ext3 documents
		if (!(doc instanceof final IDocumentExtension3 ext3)) //
			return;

		try {
			if (ext3.getDocumentPartitioner(TMPartitions.TM_PARTITIONING) != null)
				return; // already installed

			final var partitioner = new TMPartitioner();
			ext3.setDocumentPartitioner(TMPartitions.TM_PARTITIONING, partitioner);
			partitioner.connect(doc);
		} catch (final Exception ex) {
			TMUIPlugin.logTrace(ex);
		}
	}
}
