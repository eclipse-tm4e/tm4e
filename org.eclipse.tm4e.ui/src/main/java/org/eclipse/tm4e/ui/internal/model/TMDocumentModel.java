/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.model;

import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.model.TMModel;
import org.eclipse.tm4e.ui.model.ITMDocumentModel;

public final class TMDocumentModel extends TMModel implements ITMDocumentModel {

	private final IDocument document;

	public TMDocumentModel(final IDocument document) {
		super(new DocumentModelLines(document));
		this.document = document;
	}

	@Override
	public IDocument getDocument() {
		return document;
	}

}
