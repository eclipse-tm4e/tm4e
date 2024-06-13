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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.ui.model.ITMModelManager;

/**
 * TextMate model manager which connect/disconnect a {@link TMDocumentModel} with an Eclipse {@link IDocument}.
 */
public final class TMModelManager implements ITMModelManager {

	public static final TMModelManager INSTANCE = new TMModelManager();

	private final Map<IDocument, TMDocumentModel> models = new ConcurrentHashMap<>();

	private TMModelManager() {
	}

	@Override
	public TMDocumentModel connect(final IDocument document) {
		return models.computeIfAbsent(document, TMDocumentModel::new);
	}

	@Override
	public void disconnect(final IDocument document) {
		final var model = models.remove(document);
		if (model != null) {
			model.dispose();
		}
	}

	public @Nullable TMDocumentModel getConnectedModel(final IDocument document) {
		return models.get(document);
	}

	@Override
	public boolean isConnected(final IDocument document) {
		return models.containsKey(document);
	}
}
