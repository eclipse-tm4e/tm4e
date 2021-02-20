package org.eclipse.tm4e.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.tm4e.core.internal.oniguruma.OnigString;
import org.junit.jupiter.api.Test;

public class OnigStringTest {

	@Test
	public void testUtf8Utf16Conversions() {
		OnigString onigString = new OnigString("áé");
		assertEquals(onigString.utf8_value.length, 4);
		assertEquals(onigString.string.length(), 2);
		assertEquals(onigString.convertUtf8OffsetToUtf16(0), 0);
	}

	@Test
	public void testUtf8Utf16Conversions2() {

        String string = "myááçóúôõaab";
        OnigString utf8WithCharLen = new OnigString(string);

        assertEquals(0, utf8WithCharLen.convertUtf16OffsetToUtf8(0));
        assertEquals(1, utf8WithCharLen.convertUtf16OffsetToUtf8(1));
        assertEquals(2, utf8WithCharLen.convertUtf16OffsetToUtf8(2));
        assertEquals(4, utf8WithCharLen.convertUtf16OffsetToUtf8(3));
        assertEquals(6, utf8WithCharLen.convertUtf16OffsetToUtf8(4));
        assertEquals(8, utf8WithCharLen.convertUtf16OffsetToUtf8(5));
        assertEquals(10, utf8WithCharLen.convertUtf16OffsetToUtf8(6));
        assertEquals(12, utf8WithCharLen.convertUtf16OffsetToUtf8(7));
        try {
            utf8WithCharLen.convertUtf16OffsetToUtf8(55);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        assertEquals(0, utf8WithCharLen.convertUtf8OffsetToUtf16(0));
        assertEquals(1, utf8WithCharLen.convertUtf8OffsetToUtf16(1));
        assertEquals(2, utf8WithCharLen.convertUtf8OffsetToUtf16(2));
        assertEquals(2, utf8WithCharLen.convertUtf8OffsetToUtf16(3));
        assertEquals(3, utf8WithCharLen.convertUtf8OffsetToUtf16(4));
        assertEquals(3, utf8WithCharLen.convertUtf8OffsetToUtf16(5));
        assertEquals(4, utf8WithCharLen.convertUtf8OffsetToUtf16(6));
        assertEquals(4, utf8WithCharLen.convertUtf8OffsetToUtf16(7));
        assertEquals(5, utf8WithCharLen.convertUtf8OffsetToUtf16(8));
        assertEquals(6, utf8WithCharLen.convertUtf8OffsetToUtf16(10));
        assertEquals(7, utf8WithCharLen.convertUtf8OffsetToUtf16(12));
        try {
            utf8WithCharLen.convertUtf8OffsetToUtf16(55);
            fail("Expected error");
        } catch (ArrayIndexOutOfBoundsException e) {
        }

	}
}
