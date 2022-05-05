/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.model.ITMModel;
import org.eclipse.tm4e.ui.model.ITMModelManager;

/**
 * TextMate model manager which connect/disconnect a TextModel model
 * {@link ITMModel} with an Eclipse {@link IDocument}.
 */
public final class TMModelManager implements ITMModelManager {

	private static final ITMModelManager INSTANCE = new TMModelManager();

	public static ITMModelManager getInstance() {
		return INSTANCE;
	}

	private final Map<IDocument, ITMModel> models = new HashMap<>();

	private TMModelManager() {
	}

	@Override
	public ITMModel connect(final IDocument document) {
		ITMModel model = models.get(document);
		if (model != null) {
			return model;
		}
		model = new TMDocumentModel(document);
		models.put(document, model);
		return model;
	}

	@Override
	public void disconnect(final IDocument document) {
		final ITMModel model = models.remove(document);
		if (model != null) {
			model.dispose();
		}
	}
}
