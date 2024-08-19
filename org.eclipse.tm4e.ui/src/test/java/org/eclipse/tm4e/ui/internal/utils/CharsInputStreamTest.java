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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

public class CharsInputStreamTest {
	private static final String TEST_ASCII = "Hello, World!";

	private static final String EMOJI = "😊";
	private static final int EMOJI_BYTES_LEN = EMOJI.getBytes(UTF_8).length;
	private static final String JAPANESE = "こんにちは";
	private static final String TEST_UNICODE = EMOJI + JAPANESE;
	private static final int TEST_UNICODE_BYTES_LEN = TEST_UNICODE.getBytes(UTF_8).length;

	@Test
	public void testAvailable() throws IOException {
		try (var is = new CharsInputStream(TEST_ASCII)) {
			assertEquals(TEST_ASCII.length(), is.available());
			final byte[] buffer = new byte[4];
			is.read(buffer);
			assertEquals(TEST_ASCII.length() - 4, is.available());
			is.readAllBytes();
			assertEquals(0, is.available());
		}

		try (var is = new CharsInputStream(TEST_UNICODE)) {
			assertTrue(is.available() > 0);
			is.read(new byte[10]);
			assertTrue(is.available() > 0);
			is.readAllBytes();
			assertEquals(0, is.available());
		}
	}

	@Test
	public void testEndOfStream() throws IOException {
		try (var is = new CharsInputStream(TEST_UNICODE)) {
			is.skip(Long.MAX_VALUE);
			assertEquals(-1, is.read());
		}
	}

	@Test
	public void testReadEachByte() throws IOException {
		try (var is = new CharsInputStream(TEST_UNICODE)) {
			final var bytesRead = new ArrayList<Byte>();
			int b;
			while ((b = is.read()) != -1) {
				bytesRead.add((byte) b);
			}

			final byte[] byteArray = new byte[bytesRead.size()];
			for (int i = 0; i < bytesRead.size(); i++) {
				byteArray[i] = bytesRead.get(i);
			}
			assertEquals(TEST_UNICODE, new String(byteArray, UTF_8));
		}
	}

	@Test
	public void testReadIntoByteArray() throws IOException {
		final byte[] buffer = new byte[1024]; // Buffer to read a portion of the text

		try (var is = new CharsInputStream(TEST_UNICODE)) {
			final int bytesRead = is.read(buffer, 0, buffer.length);

			assertEquals(TEST_UNICODE_BYTES_LEN, bytesRead);
			assertEquals(TEST_UNICODE, new String(buffer, 0, bytesRead, UTF_8));
		}
	}

	@Test
	public void testSkip() throws IOException {
		try (var is = new CharsInputStream(TEST_UNICODE)) {
			// skip emoji
			final long skipped = is.skip(EMOJI_BYTES_LEN);
			assertEquals(EMOJI_BYTES_LEN, skipped);

			final byte[] japanese = new byte[TEST_UNICODE_BYTES_LEN];
			final int bytesRead = is.read(japanese);

			assertEquals(JAPANESE, new String(japanese, 0, bytesRead, UTF_8));
		}
	}

	@Test
	public void testHighSurrogateAtEndOfInput() throws IOException {
		final char[] invalidSequence = { 'A', '\uD800' }; // valid char followed by an isolated high surrogate
		try (var is = new CharsInputStream(new String(invalidSequence), UTF_8)) {
			final byte[] result = is.readAllBytes();
			final String output = new String(result, UTF_8);

			// the high surrogate at the end should be replaced by the Unicode replacement char
			assertEquals("A\uFFFD", output);
		}
	}

	@Test
	public void testHighSurrogateWithoutLowSurrogate() throws IOException {
		final char[] invalidSequence = { '\uD800', 'A' }; // \uD800 is a high surrogate, followed by 'A'
		try (var is = new CharsInputStream(new String(invalidSequence), UTF_8)) {
			final byte[] result = is.readAllBytes();
			final String output = new String(result, UTF_8);

			// the invalid surrogate pair should be replaced by the Unicode replacement char
			assertEquals("\uFFFD" + "A", output);
		}
	}
}
