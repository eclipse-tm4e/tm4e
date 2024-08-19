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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.IntSupplier;

import org.eclipse.jdt.annotation.Nullable;

class CharsInputStream extends InputStream {
	@FunctionalInterface
	public interface CharsSupplier {
		char charAt(int index) throws Exception;
	}

	private enum EncoderState {
		ENCODING,
		FLUSHING,
		DONE
	}

	/** 512 surrogate character pairs */
	private static final int DEFAULT_BUFFER_SIZE = 512;
	private static final int EOF = -1;

	private final int bufferSize;
	private final CharBuffer charBuffer;
	private final ByteBuffer byteBuffer;
	private final CharsetEncoder encoder;
	private EncoderState encoderState = EncoderState.ENCODING;

	private int charIndex = 0;
	private final CharsSupplier chars;
	private final IntSupplier charsLength;

	CharsInputStream(final CharSequence chars) {
		this(chars, null);
	}

	CharsInputStream(final CharSequence chars, final @Nullable Charset charset) {
		this(chars, charset, DEFAULT_BUFFER_SIZE);
	}

	CharsInputStream(final CharSequence chars, final @Nullable Charset charset, final int bufferSize) {
		this(chars::charAt, chars::length, charset, bufferSize);
	}

	CharsInputStream(final CharsSupplier chars, final IntSupplier charsLength) {
		this(chars, charsLength, null);
	}

	/**
	 * @param chars function to access indexed chars.
	 * @param charsLength function to get the number of indexed chars provided by the <code>chars</code> parameter.
	 */
	CharsInputStream(final CharsSupplier chars, final IntSupplier charsLength, final @Nullable Charset charset) {
		this(chars, charsLength, charset, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * @param chars function to access indexed chars.
	 * @param charsLength function to get the number of indexed chars provided by the <code>chars</code> parameter.
	 * @param bufferSize number of surrogate character pairs to encode at once.
	 */
	CharsInputStream(final CharsSupplier chars, final IntSupplier charsLength, final @Nullable Charset charset, final int bufferSize) {
		if (bufferSize < 1)
			throw new IllegalArgumentException("[bufferSize] must be 1 or larger");
		encoder = (charset == null ? StandardCharsets.UTF_8 : charset).newEncoder();

		this.bufferSize = bufferSize;
		charBuffer = CharBuffer.allocate(bufferSize * 2); // buffer for 2 chars (high/low surrogate)
		byteBuffer = ByteBuffer.allocate(bufferSize * 4); // buffer for one UTF character (up to 4 bytes)
		byteBuffer.flip();
		charBuffer.flip();

		this.chars = chars;
		this.charsLength = charsLength;
	}

	@Override
	public int available() {
		final int remaining = byteBuffer.remaining();
		return remaining == 0 ? charsLength.getAsInt() - charIndex : remaining;
	}

	public Charset getCharset() {
		return encoder.charset();
	}

	private boolean flushEncoder() throws IOException {
		if (encoderState == EncoderState.DONE)
			return false;

		if (encoderState == EncoderState.ENCODING) {
			encoderState = EncoderState.FLUSHING;
		}

		// flush
		byteBuffer.clear();
		final CoderResult result = encoder.flush(byteBuffer);
		byteBuffer.flip();

		if (result.isOverflow()) // byteBuffer too small
			return true;

		if (result.isError()) {
			result.throwException();
		}

		encoderState = EncoderState.DONE;
		return byteBuffer.hasRemaining();
	}

	@Override
	public int read() throws IOException {
		if (!byteBuffer.hasRemaining() && !refillBuffer())
			return EOF;
		return byteBuffer.get() & 0xFF; // next byte as an unsigned integer (0 to 255)
	}

	@Override
	public int read(final byte[] buf, final int off, final int bytesToRead) throws IOException {
		Objects.checkFromIndexSize(off, bytesToRead, buf.length);
		if (bytesToRead == 0)
			return 0;

		int bytesRead = 0;
		int bytesReadable = byteBuffer.remaining();

		while (bytesRead < bytesToRead) {
			if (bytesReadable == 0) {
				if (refillBuffer()) {
					bytesReadable = byteBuffer.remaining();
				} else
					return bytesRead == 0 ? EOF : bytesRead;
			}

			final int bytesToReadNow = Math.min(bytesToRead - bytesRead, bytesReadable);
			byteBuffer.get(buf, off + bytesRead, bytesToReadNow);
			bytesRead += bytesToReadNow;
			bytesReadable -= bytesToReadNow;
		}

		return bytesRead;
	}

	private boolean refillBuffer() throws IOException {
		if (encoderState == EncoderState.DONE)
			return false;

		if (encoderState == EncoderState.FLUSHING)
			return flushEncoder();

		final int charsLen = charsLength.getAsInt();

		// if EOF is reached transition to flushing
		if (charIndex >= charsLen) {
			// finalize encoding before switching to flushing
			byteBuffer.clear();
			final CoderResult result = encoder.encode(CharBuffer.allocate(0), byteBuffer, true /* signal EOF */);
			byteBuffer.flip();
			if (result.isError()) {
				result.throwException();
			}
			return flushEncoder();
		}

		try {
			charBuffer.clear();
			for (int i = 0; i < bufferSize && charIndex < charsLen; i++) {
				final char nextChar = chars.charAt(charIndex++);
				if (Character.isHighSurrogate(nextChar)) { // handle surrogate pairs
					if (charIndex < charsLen) {
						final char lowSurrogate = chars.charAt(charIndex);
						if (Character.isLowSurrogate(lowSurrogate)) {
							charIndex++;
							charBuffer.put(nextChar);
							charBuffer.put(lowSurrogate);
						} else {
							// missing low surrogate - fallback to replacement character
							charBuffer.put('\uFFFD');
						}
					} else {
						// missing low surrogate - fallback to replacement character
						charBuffer.put('\uFFFD');
						break;
					}
				} else {
					charBuffer.put(nextChar);
				}
			}
			charBuffer.flip();

			// encode chars into bytes
			byteBuffer.clear();
			final CoderResult result = encoder.encode(charBuffer, byteBuffer, false);
			byteBuffer.flip();
			if (result.isError()) {
				result.throwException();
			}
		} catch (final Exception ex) {
			throw new IOException(ex);
		}

		return true;
	}
}
