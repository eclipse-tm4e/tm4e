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

public class TMDocumentModel extends TMModel {

	private final IDocument document;

	public TMDocumentModel(IDocument document) {
		super(new DocumentLineList(document));
		this.document = document;
	}

	public IDocument getDocument() {
		return document;
	}

}
