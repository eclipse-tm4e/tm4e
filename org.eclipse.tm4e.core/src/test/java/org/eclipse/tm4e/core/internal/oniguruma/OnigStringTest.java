/**
 * Copyright (c) 2017 Fabio Zadrozny.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Fabio Zadrozny - initial API and implementation
 * Sebastian Thomschke - refactoring and extended test cases
 */
package org.eclipse.tm4e.core.internal.oniguruma;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnigStringTest {

	private OnigString verifyBasics(final String string, final Class<? extends OnigString> expectedType) {
		final OnigString onigString = OnigString.of(string);
		assertThat(onigString).isInstanceOf(expectedType);
		assertThat(onigString.content).isEqualTo(string);
		assertThat(onigString.toString()).contains(string);

		assertThat(onigString.bytesCount).isEqualTo(onigString.bytesUTF8.length);

		/*
		 * getByteIndexOfChar tests
		 */
		assertThatThrownBy(() -> onigString.getByteIndexOfChar(-1))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);
		assertThat(onigString.getByteIndexOfChar(0)).isZero();
		if (!string.isEmpty()) {
			onigString.getByteIndexOfChar(string.length() - 1); // does not throw exception, because in range
		}
		onigString.getByteIndexOfChar(string.length()); // does not throw exception, because of internal workaround
		assertThatThrownBy(() -> onigString.getByteIndexOfChar(string.length() + 1))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);

		/*
		 * getCharIndexOfByte tests
		 */
		assertThatThrownBy(() -> onigString.getCharIndexOfByte(-1))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);
		assertThat(onigString.getCharIndexOfByte(0)).isZero();
		if (!string.isEmpty()) {
			// does not throw exception, because in range
			assertThat(onigString.getCharIndexOfByte(onigString.bytesCount - 1))
					.isEqualTo(string.length() - 1);
		}
		// does not throw exception, because of internal workaround
		assertThat(onigString.getCharIndexOfByte(onigString.bytesCount))
				.isEqualTo(string.length());

		assertThatThrownBy(() -> onigString.getCharIndexOfByte(onigString.bytesCount + 1))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);

		return onigString;
	}

	@Test
	void testEmptyStrings() {
		final var string = "";
		final OnigString onigString = verifyBasics(string, OnigString.SingleByteString.class);

		assertThat(onigString.bytesCount).isZero();
	}

	@Test
	void testSingleBytesStrings() {
		final var string = "ab";
		final OnigString onigString = verifyBasics(string, OnigString.SingleByteString.class);

		assertThat(onigString.bytesCount).isEqualTo(2);

		/*
		 * getByteIndexOfChar tests
		 */
		assertThat(onigString.getByteIndexOfChar(0)).isZero();
		assertThat(onigString.getByteIndexOfChar(1)).isEqualTo(1);
		assertThat(onigString.getByteIndexOfChar(2)).isEqualTo(2); // does not throw exception, because of internal workaround
		assertThatThrownBy(() -> onigString.getByteIndexOfChar(3))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);

		/*
		 * getCharIndexOfByte tests
		 */
		assertThat(onigString.getCharIndexOfByte(0)).isZero(); // a
		assertThat(onigString.getCharIndexOfByte(1)).isEqualTo(1); // b
		assertThat(onigString.getCharIndexOfByte(2)).isEqualTo(2); // does not throw exception, because of internal workaround
		assertThatThrownBy(() -> onigString.getCharIndexOfByte(3))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void testMultiByteString() {
		final var string = "áé";
		final OnigString onigString = verifyBasics(string, OnigString.MultiByteString.class);

		assertThat(onigString.bytesCount).isEqualTo(4);

		/*
		 * getByteIndexOfChar tests
		 */
		assertThat(onigString.getByteIndexOfChar(0)).isZero(); // á
		assertThat(onigString.getByteIndexOfChar(1)).isEqualTo(2); // é
		assertThat(onigString.getByteIndexOfChar(2)).isEqualTo(4); // does not throw exception, because of internal workaround
		assertThatThrownBy(() -> onigString.getByteIndexOfChar(3))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);

		/*
		 * getCharIndexOfByte tests
		 */
		assertThat(onigString.getCharIndexOfByte(1)).isZero(); // á
		assertThat(onigString.getCharIndexOfByte(2)).isEqualTo(1); // é
		assertThat(onigString.getCharIndexOfByte(3)).isEqualTo(1); // é
		assertThat(onigString.getCharIndexOfByte(4)).isEqualTo(2); // does not throw exception, because of internal workaround
		assertThatThrownBy(() -> onigString.getCharIndexOfByte(5))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void testMixedMultiByteString() {
		final var string = "myáçóúôõab";
		final OnigString onigString = verifyBasics(string, OnigString.MultiByteString.class);

		assertThat(onigString.bytesCount).isEqualTo(16);

		/*
		 * getByteIndexOfChar tests
		 */
		assertThat(onigString.getByteIndexOfChar(0)).isZero(); // m
		assertThat(onigString.getByteIndexOfChar(1)).isEqualTo(1); // y
		assertThat(onigString.getByteIndexOfChar(2)).isEqualTo(2); // á
		assertThat(onigString.getByteIndexOfChar(3)).isEqualTo(4); // ç
		assertThat(onigString.getByteIndexOfChar(4)).isEqualTo(6); // ó
		assertThat(onigString.getByteIndexOfChar(5)).isEqualTo(8); // ú
		assertThat(onigString.getByteIndexOfChar(6)).isEqualTo(10); // ô
		assertThat(onigString.getByteIndexOfChar(7)).isEqualTo(12); // õ
		assertThat(onigString.getByteIndexOfChar(8)).isEqualTo(14); // a
		assertThat(onigString.getByteIndexOfChar(9)).isEqualTo(15); // b
		assertThat(onigString.getByteIndexOfChar(10)).isEqualTo(16); // does not throw exception, because of internal workaround
		assertThatThrownBy(() -> onigString.getByteIndexOfChar(string.length() + 1))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);

		/*
		 * getCharIndexOfByte tests
		 */
		assertThat(onigString.getCharIndexOfByte(0)).isZero(); // m
		assertThat(onigString.getCharIndexOfByte(1)).isEqualTo(1); // y
		assertThat(onigString.getCharIndexOfByte(2)).isEqualTo(2); // á
		assertThat(onigString.getCharIndexOfByte(3)).isEqualTo(2); // á
		assertThat(onigString.getCharIndexOfByte(4)).isEqualTo(3); // ç
		assertThat(onigString.getCharIndexOfByte(5)).isEqualTo(3); // ç
		assertThat(onigString.getCharIndexOfByte(6)).isEqualTo(4); // ó
		assertThat(onigString.getCharIndexOfByte(7)).isEqualTo(4); // ó
		assertThat(onigString.getCharIndexOfByte(8)).isEqualTo(5); // ú
		assertThat(onigString.getCharIndexOfByte(9)).isEqualTo(5); // ú
		assertThat(onigString.getCharIndexOfByte(10)).isEqualTo(6); // ô
		assertThat(onigString.getCharIndexOfByte(11)).isEqualTo(6); // ô
		assertThat(onigString.getCharIndexOfByte(12)).isEqualTo(7); // õ
		assertThat(onigString.getCharIndexOfByte(13)).isEqualTo(7); // õ
		assertThat(onigString.getCharIndexOfByte(14)).isEqualTo(8); // a
		assertThat(onigString.getCharIndexOfByte(15)).isEqualTo(9); // b
		assertThat(onigString.getCharIndexOfByte(16)).isEqualTo(10); // does not throw exception, because of internal workaround
		assertThatThrownBy(() -> onigString.getCharIndexOfByte(17))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);
	}
}
