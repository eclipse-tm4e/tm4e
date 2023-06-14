/**
 * Copyright (c) 2023 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke - initial implementation
 */
package org.eclipse.tm4e.ui.model;

import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.model.ITMModel;

public interface ITMDocumentModel extends ITMModel {

	IDocument getDocument();
}
