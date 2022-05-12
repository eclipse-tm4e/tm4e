package org.eclipse.tm4e.core.internal.oniguruma;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnigStringTest {

	private OnigString verifyBasics(final String string, final Class<? extends OnigString> expectedType) {
		final OnigString onigString = OnigString.of(string);
		assertInstanceOf(expectedType, onigString);
		assertEquals(string, onigString.string);
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
		final String string = "";
		final OnigString onigString = verifyBasics(string, OnigString.SingleByteString.class);

		assertEquals(0, onigString.bytesCount);
	}

	@Test
	void testSingleBytesStrings() {
		final String string = "ab";
		final OnigString onigString = verifyBasics(string, OnigString.SingleByteString.class);

		assertEquals(2, onigString.bytesCount);

		/*
		 * getByteIndexOfChar tests
		 */
		assertEquals(1, onigString.getByteIndexOfChar(1));
		assertEquals(string.length() - 1, onigString.getByteIndexOfChar(string.length() - 1));

		/*
		 * getCharIndexOfByte tests
		 */
		assertEquals(1, onigString.getCharIndexOfByte(1)); // b
		assertEquals(2, onigString.getCharIndexOfByte(2)); // does not throws exception, because of internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getCharIndexOfByte(3));
	}

	@Test
	void testMultiByteString() {
		final String string = "áé";
		final OnigString onigString = verifyBasics(string, OnigString.MultiByteString.class);

		assertEquals(4, onigString.bytesCount);

		/*
		 * getByteIndexOfChar tests
		 */
		assertEquals(2, onigString.getByteIndexOfChar(1));

		/*
		 * getCharIndexOfByte tests
		 */
		assertEquals(0, onigString.getCharIndexOfByte(1)); // á
		assertEquals(1, onigString.getCharIndexOfByte(2)); // é
		assertEquals(1, onigString.getCharIndexOfByte(3)); // é
		assertEquals(2, onigString.getCharIndexOfByte(4)); // this is an internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getCharIndexOfByte(5));

	}

	@Test
	void testMixedMultiByteString() {
		final String string = "myááçóúôõaab";
		final OnigString onigString = verifyBasics(string, OnigString.MultiByteString.class);

		assertEquals(19, onigString.bytesCount);

		/*
		 * getByteIndexOfChar tests
		 */
		assertEquals(1, onigString.getByteIndexOfChar(1));
		assertEquals(2, onigString.getByteIndexOfChar(2));
		assertEquals(4, onigString.getByteIndexOfChar(3));
		assertEquals(6, onigString.getByteIndexOfChar(4));
		assertEquals(8, onigString.getByteIndexOfChar(5));
		assertEquals(10, onigString.getByteIndexOfChar(6));
		assertEquals(12, onigString.getByteIndexOfChar(7));

		/*
		 * getCharIndexOfByte tests
		 */
		assertEquals(1, onigString.getCharIndexOfByte(1));
		assertEquals(2, onigString.getCharIndexOfByte(2));
		assertEquals(2, onigString.getCharIndexOfByte(3));
		assertEquals(3, onigString.getCharIndexOfByte(4));
		assertEquals(3, onigString.getCharIndexOfByte(5));
		assertEquals(4, onigString.getCharIndexOfByte(6));
		assertEquals(4, onigString.getCharIndexOfByte(7));
		assertEquals(5, onigString.getCharIndexOfByte(8));
		assertEquals(6, onigString.getCharIndexOfByte(10));
		assertEquals(7, onigString.getCharIndexOfByte(12));
		assertEquals(10, onigString.getCharIndexOfByte(17)); // a
		assertEquals(11, onigString.getCharIndexOfByte(18)); // b

		assertEquals(12, onigString.getCharIndexOfByte(19)); // this is an internal workaround
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> onigString.getCharIndexOfByte(20));
	}
}
