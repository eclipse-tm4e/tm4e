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
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class CharsInputStreamTest {

	private static final String TEST_ASCII = "Hello, World!";

	private static final String EMOJI = "😊";
	private static final int EMOJI_BYTES_LEN = EMOJI.getBytes(UTF_8).length;
	private static final String JAPANESE = "こんにちは";
	private static final String TEST_UNICODE = EMOJI + JAPANESE;
	private static final int TEST_UNICODE_BYTES_LEN = TEST_UNICODE.getBytes(UTF_8).length;

	@Test
	void testAvailable() throws IOException {
		try (var is = new CharsInputStream(TEST_ASCII)) {
			assertThat(is.available()).isEqualTo(TEST_ASCII.length());
			final var buffer = new byte[4];
			is.read(buffer);
			assertThat(is.available()).isEqualTo(TEST_ASCII.length() - 4);
			is.readAllBytes();
			assertThat(is.available()).isZero();
		}

		try (var is = new CharsInputStream(TEST_UNICODE)) {
			assertThat(is.available()).isPositive();
			is.read(new byte[10]);
			assertThat(is.available()).isPositive();
			is.readAllBytes();
			assertThat(is.available()).isZero();
		}
	}

	@Test
	void testEndOfStream() throws IOException {
		try (var is = new CharsInputStream(TEST_UNICODE)) {
			is.skip(Long.MAX_VALUE);
			assertThat(is.read()).isEqualTo(-1);
		}
	}

	@Test
	void testReadEachByte() throws IOException {
		try (var is = new CharsInputStream(TEST_UNICODE)) {
			final var bytesRead = new ArrayList<Byte>();
			int b;
			while ((b = is.read()) != -1) {
				bytesRead.add((byte) b);
			}

			final var byteArray = new byte[bytesRead.size()];
			for (int i = 0; i < bytesRead.size(); i++) {
				byteArray[i] = bytesRead.get(i);
			}
			assertThat(new String(byteArray, UTF_8)).isEqualTo(TEST_UNICODE);
		}
	}

	@Test
	void testReadIntoByteArray() throws IOException {
		final var buffer = new byte[1024]; // Buffer to read a portion of the text

		try (var is = new CharsInputStream(TEST_UNICODE)) {
			final int bytesRead = is.read(buffer, 0, buffer.length);

			assertThat(bytesRead).isEqualTo(TEST_UNICODE_BYTES_LEN);
			assertThat(new String(buffer, 0, bytesRead, UTF_8)).isEqualTo(TEST_UNICODE);
		}
	}

	@Test
	void testSkip() throws IOException {
		try (var is = new CharsInputStream(TEST_UNICODE)) {
			// skip emoji
			final long skipped = is.skip(EMOJI_BYTES_LEN);
			assertThat(skipped).isEqualTo(EMOJI_BYTES_LEN);

			final var japanese = new byte[TEST_UNICODE_BYTES_LEN];
			final int bytesRead = is.read(japanese);

			assertThat(new String(japanese, 0, bytesRead, UTF_8)).isEqualTo(JAPANESE);
		}
	}

	@Test
	void testHighSurrogateAtEndOfInput() throws IOException {
		final char[] invalidSequence = { 'A', '\uD800' }; // valid char followed by an isolated high surrogate
		try (var is = new CharsInputStream(new String(invalidSequence), UTF_8)) {
			final byte[] result = is.readAllBytes();
			final var output = new String(result, UTF_8);

			// the high surrogate at the end should be replaced by the Unicode replacement char
			assertThat(output).isEqualTo("A" + CharsInputStream.UNICODE_REPLACEMENT_CHAR);
		}
	}

	@Test
	void testHighSurrogateWithoutLowSurrogate() throws IOException {
		final char[] invalidSequence = { '\uD800', 'A' }; // \uD800 is a high surrogate, followed by 'A'
		try (var is = new CharsInputStream(new String(invalidSequence), UTF_8)) {
			final byte[] result = is.readAllBytes();
			final var output = new String(result, UTF_8);

			// the invalid surrogate pair should be replaced by the Unicode replacement char
			assertThat(output).isEqualTo(CharsInputStream.UNICODE_REPLACEMENT_CHAR + "A");
		}
	}
}
