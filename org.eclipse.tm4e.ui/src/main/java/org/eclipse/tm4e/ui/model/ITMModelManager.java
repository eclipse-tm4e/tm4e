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
package org.eclipse.tm4e.ui.model;

import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.model.ITMModel;

/**
 * TextMate model manager API.
 *
 */
public interface ITMModelManager {

	/**
	 * Connect the given document to a TextMate model.
	 *
	 * @param document
	 *
	 * @return the TextMate model connected to the document.
	 */
	ITMModel connect(IDocument document);

	/**
	 * Disconnect the TextMate model of the given document.
	 *
	 * @param document
	 */
	void disconnect(IDocument document);
}
