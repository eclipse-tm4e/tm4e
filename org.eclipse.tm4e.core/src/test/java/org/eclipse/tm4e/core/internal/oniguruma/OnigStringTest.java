package org.eclipse.tm4e.core.internal.oniguruma;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnigStringTest {

	private OnigString verifyBasics(final String string, final Class<? extends OnigString> expectedType) {
		final OnigString onigString = OnigString.of(string);
		assertInstanceOf(expectedType, onigString);
		assertEquals(string, onigString.content);
		assertTrue(onigString.toString().contains(string));

		assertEquals(onigString.bytesCount, onigString.bytesUTF8.length);

		/*
		 * getByteIndexOfChar tests
		 */
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getByteIndexOfChar(-1));
		assertEquals(0, onigString.getByteIndexOfChar(0));
		if (!string.isEmpty())
			onigString.getByteIndexOfChar(string.length() - 1); // does not throws exception, because in range
		onigString.getByteIndexOfChar(string.length()); // does not throws exception, because of internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getByteIndexOfChar(string.length() + 1));

		/*
		 * getCharIndexOfByte tests
		 */
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getCharIndexOfByte(-1));
		assertEquals(0, onigString.getCharIndexOfByte(0));
		if (!string.isEmpty()) {
			// does not throws exception, because in range
			assertEquals(string.length() - 1, onigString.getCharIndexOfByte(onigString.bytesCount - 1));
		}
		// does not throws exception, because of internal workaround
		assertEquals(string.length(), onigString.getCharIndexOfByte(onigString.bytesCount));

		assertThrows(ArrayIndexOutOfBoundsException.class,
			() -> onigString.getCharIndexOfByte(onigString.bytesCount + 1));

		return onigString;
	}

	@Test
	void testEmptyStrings() {
		final var string = "";
		final OnigString onigString = verifyBasics(string, OnigString.SingleByteString.class);

		assertEquals(0, onigString.bytesCount);
	}

	@Test
	void testSingleBytesStrings() {
		final var string = "ab";
		final OnigString onigString = verifyBasics(string, OnigString.SingleByteString.class);

		assertEquals(2, onigString.bytesCount);

		/*
		 * getByteIndexOfChar tests
		 */
		assertEquals(0, onigString.getByteIndexOfChar(0));
		assertEquals(1, onigString.getByteIndexOfChar(1));
		assertEquals(2, onigString.getByteIndexOfChar(2)); // does not throw exception, because of internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getByteIndexOfChar(3));

		/*
		 * getCharIndexOfByte tests
		 */
		assertEquals(0, onigString.getCharIndexOfByte(0)); // a
		assertEquals(1, onigString.getCharIndexOfByte(1)); // b
		assertEquals(2, onigString.getCharIndexOfByte(2)); // does not throw exception, because of internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getCharIndexOfByte(3));
	}

	@Test
	void testMultiByteString() {
		final var string = "áé";
		final OnigString onigString = verifyBasics(string, OnigString.MultiByteString.class);

		assertEquals(4, onigString.bytesCount);

		/*
		 * getByteIndexOfChar tests
		 */
		assertEquals(0, onigString.getByteIndexOfChar(0)); // á
		assertEquals(2, onigString.getByteIndexOfChar(1)); // é
		assertEquals(4, onigString.getByteIndexOfChar(2)); // does not throw exception, because of internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getByteIndexOfChar(3));

		/*
		 * getCharIndexOfByte tests
		 */
		assertEquals(0, onigString.getCharIndexOfByte(1)); // á
		assertEquals(1, onigString.getCharIndexOfByte(2)); // é
		assertEquals(1, onigString.getCharIndexOfByte(3)); // é
		assertEquals(2, onigString.getCharIndexOfByte(4)); // does not throw exception, because of internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getCharIndexOfByte(5));

	}

	@Test
	void testMixedMultiByteString() {
		final var string = "myáçóúôõab";
		final OnigString onigString = verifyBasics(string, OnigString.MultiByteString.class);

		assertEquals(16, onigString.bytesCount);

		/*
		 * getByteIndexOfChar tests
		 */
		assertEquals(0, onigString.getByteIndexOfChar(0)); // m
		assertEquals(1, onigString.getByteIndexOfChar(1)); // y
		assertEquals(2, onigString.getByteIndexOfChar(2)); // á
		assertEquals(4, onigString.getByteIndexOfChar(3)); // ç
		assertEquals(6, onigString.getByteIndexOfChar(4)); // ó
		assertEquals(8, onigString.getByteIndexOfChar(5)); // ú
		assertEquals(10, onigString.getByteIndexOfChar(6)); // ô
		assertEquals(12, onigString.getByteIndexOfChar(7)); // õ
		assertEquals(14, onigString.getByteIndexOfChar(8)); // a
		assertEquals(15, onigString.getByteIndexOfChar(9)); // b
		assertEquals(16, onigString.getByteIndexOfChar(10)); // does not throw exception, because of internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getByteIndexOfChar(string.length() + 1));

		/*
		 * getCharIndexOfByte tests
		 */
		assertEquals(0, onigString.getCharIndexOfByte(0)); // m
		assertEquals(1, onigString.getCharIndexOfByte(1)); // y
		assertEquals(2, onigString.getCharIndexOfByte(2)); // á
		assertEquals(2, onigString.getCharIndexOfByte(3)); // á
		assertEquals(3, onigString.getCharIndexOfByte(4)); // ç
		assertEquals(3, onigString.getCharIndexOfByte(5)); // ç
		assertEquals(4, onigString.getCharIndexOfByte(6)); // ó
		assertEquals(4, onigString.getCharIndexOfByte(7)); // ó
		assertEquals(5, onigString.getCharIndexOfByte(8)); // ú
		assertEquals(5, onigString.getCharIndexOfByte(9)); // ú
		assertEquals(6, onigString.getCharIndexOfByte(10)); // ô
		assertEquals(6, onigString.getCharIndexOfByte(11)); // ô
		assertEquals(7, onigString.getCharIndexOfByte(12)); // õ
		assertEquals(7, onigString.getCharIndexOfByte(13)); // õ
		assertEquals(8, onigString.getCharIndexOfByte(14)); // a
		assertEquals(9, onigString.getCharIndexOfByte(15)); // b
		assertEquals(10, onigString.getCharIndexOfByte(16)); // does not throw exception, because of internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getCharIndexOfByte(17));
	}
}
