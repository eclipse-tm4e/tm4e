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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.tm4e.core.model.AbstractModelLines;
import org.eclipse.tm4e.ui.TMUIPlugin;

/**
 * TextMate {@link AbstractModelLines} implementation with Eclipse {@link IDocument}.
 *
 * Goal of this class is to synchronize Eclipse {@link DocumentEvent} with TextMate model lines.
 */
final class DocumentModelLines extends AbstractModelLines implements IDocumentListener {

	private final IDocument document;
	private final Map<DocumentEvent, Integer> removeEndLineIndexes = new ConcurrentHashMap<>();

	DocumentModelLines(final IDocument document) {
		this.document = document;
		document.addDocumentListener(this);
		addLines(0, document.getNumberOfLines());
	}

	@Override
	public void documentAboutToBeChanged(@Nullable final DocumentEvent event) {
		if (event == null)
			return;
		try {
			switch (DocumentHelper.getEventType(event)) {
			case REMOVE, REPLACE /*= Remove + Insert */:
				removeEndLineIndexes.put(event, DocumentHelper.getEndLineIndexOfRemovedText(event));
				// => cannot be calculated in documentChanged() where it will result in BadLocationException
				break;
			default:
			}
		} catch (final BadLocationException ex) {
			TMUIPlugin.log(new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID, ex.getMessage(), ex));
		}
	}

	@Override
	public void documentChanged(@Nullable final DocumentEvent event) {
		if (event == null)
			return;
		try {
			final int startLineIndex = DocumentHelper.getStartLineIndex(event);
			switch (DocumentHelper.getEventType(event)) {
			case INSERT: {
				final var endLineIndex = DocumentHelper.getEndLineIndexOfAddedText(event);
				replaceLines(startLineIndex, 1, 1 + (endLineIndex - startLineIndex));
				break;
			}
			case REMOVE: {
				final var endLineIndex = removeEndLineIndexes.remove(event).intValue();
				replaceLines(startLineIndex, 1 + (endLineIndex - startLineIndex), 1);
				break;
			}
			case REPLACE: {
				final var endLineRemovedIndex = removeEndLineIndexes.remove(event).intValue();
				final var endLineAddedIndex = DocumentHelper.getEndLineIndexOfAddedText(event);
				replaceLines(startLineIndex,
						1 + (endLineRemovedIndex - startLineIndex),
						1 + (endLineAddedIndex - startLineIndex));
				break;
			}
			}
		} catch (final BadLocationException ex) {
			TMUIPlugin.log(new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID, ex.getMessage(), ex));
		}
	}

	@Override
	public String getLineText(final int line) throws Exception {
		return DocumentHelper.getLineText(document, line, false);
	}

	@Override
	public void dispose() {
		document.removeDocumentListener(this);
	}
}
