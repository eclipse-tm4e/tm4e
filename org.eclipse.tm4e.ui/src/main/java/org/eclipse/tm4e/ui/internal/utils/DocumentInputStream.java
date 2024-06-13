/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * QNX Software System
 * Sebastian Thomschke - implement read(byte[], int, int)
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Input stream which reads from a document
 */
final class DocumentInputStream extends InputStream {

	private final IDocument doc;
	private int pos = 0;

	DocumentInputStream(final IDocument document) {
		doc = document;
	}

	@Override
	public int read(final byte[] buff, final int buffOffset, final int len) throws IOException {
		Objects.checkFromIndexSize(buffOffset, len, buff.length);

		if (len == 0)
			return 0;

		final var docLen = doc.getLength();
		if (pos >= docLen)
			return -1;

		var bytesRead = -1;
		try {
			buff[buffOffset] = (byte) doc.getChar(pos++);
			bytesRead = 1;
			while (bytesRead < len) {
				if (pos >= docLen) {
					break;
				}
				buff[buffOffset + bytesRead++] = (byte) doc.getChar(pos++);
			}
		} catch (final BadLocationException ex) {
			// ignore
		}
		return bytesRead;
	}

	@Override
	public int read() throws IOException {
		try {
			if (pos < doc.getLength())
				return doc.getChar(pos++) & 0xFF;
		} catch (final BadLocationException ex) {
			// ignore
		}
		return -1;
	}
}
