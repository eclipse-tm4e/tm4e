/*******************************************************************************
 * Copyright (c) 2024 Sebastian Thomschke and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.utils;

import java.nio.charset.Charset;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.ui.TMUIPlugin;

public final class DocumentInputStream extends CharsInputStream {

	private static @Nullable Charset getCharset(final IDocument document) {
		final ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		if (bufferManager == null)
			return null;
		final ITextFileBuffer buffer = bufferManager.getTextFileBuffer(document);
		if (buffer == null)
			return null;
		try {
			final String charsetName = buffer.getEncoding();
			if (charsetName != null)
				return Charset.forName(charsetName);
		} catch (final Exception ex) {
			TMUIPlugin.logError(ex);
		}
		return null;
	}

	public DocumentInputStream(final IDocument doc) {
		super(doc::getChar, doc::getLength, getCharset(doc));
	}
}
