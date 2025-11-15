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

import org.eclipse.jface.text.ITypedRegion;

public interface ITMPartitionRegion extends ITypedRegion {

	/**
	 * Returns the effective grammar scope for this region (e.g., "source.css", "source.js", "text.html").
	 * Never null; falls back to a best-effort value when the document grammar is not yet resolved.
	 */
	String getGrammarScope();
}
