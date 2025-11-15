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

import org.eclipse.jface.text.IDocumentPartitioner;

/**
 * A coarse-grained partitioner that derives Eclipse document partitions from TM tokenization.
 *
 * Partitions are computed as contiguous regions grouped by their top-level TextMate language scope
 * (e.g., base scope like text.html.basic, or embedded scopes like source.js, source.css).
 */
public interface ITMPartitioner extends IDocumentPartitioner {

	@Override
	ITMPartitionRegion[] computePartitioning(final int offset, final int length);

	@Override
	ITMPartitionRegion getPartition(final int offset);
}
