/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.core.theme;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RGBTest {

	// ========== Valid 6-digit hex parsing tests ==========

	@Test
	void testFromHex_sixDigitsWithHash() {
		final RGB rgb = RGB.fromHex("#FF0000");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isEqualTo(255);
		assertThat(rgb.green).isZero();
		assertThat(rgb.blue).isZero();
	}

	@Test
	void testFromHex_sixDigitsWithoutHash() {
		final RGB rgb = RGB.fromHex("00FF00");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isZero();
		assertThat(rgb.green).isEqualTo(255);
		assertThat(rgb.blue).isZero();
	}

	@Test
	void testFromHex_sixDigitsLowercase() {
		final RGB rgb = RGB.fromHex("#aabbcc");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isEqualTo(170);
		assertThat(rgb.green).isEqualTo(187);
		assertThat(rgb.blue).isEqualTo(204);
	}

	@Test
	void testFromHex_sixDigitsMixedCase() {
		final RGB rgb = RGB.fromHex("#AaBbCc");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isEqualTo(170);
		assertThat(rgb.green).isEqualTo(187);
		assertThat(rgb.blue).isEqualTo(204);
	}

	// ========== Valid 3-digit hex parsing tests ==========

	@Test
	void testFromHex_threeDigitsWithHash() {
		final RGB rgb = RGB.fromHex("#F00");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isEqualTo(255);
		assertThat(rgb.green).isZero();
		assertThat(rgb.blue).isZero();
	}

	@Test
	void testFromHex_threeDigitsWithoutHash() {
		final RGB rgb = RGB.fromHex("0F0");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isZero();
		assertThat(rgb.green).isEqualTo(255);
		assertThat(rgb.blue).isZero();
	}

	@Test
	void testFromHex_threeDigitsExpansion() {
		// Test that #ABC expands to #AABBCC
		final RGB rgb = RGB.fromHex("#ABC");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isEqualTo(170);   // AA in hex = 170
		assertThat(rgb.green).isEqualTo(187); // BB in hex = 187
		assertThat(rgb.blue).isEqualTo(204);  // CC in hex = 204
	}

	@Test
	void testFromHex_threeDigitsWhite() {
		final RGB rgb = RGB.fromHex("#FFF");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isEqualTo(255);
		assertThat(rgb.green).isEqualTo(255);
		assertThat(rgb.blue).isEqualTo(255);
	}

	@Test
	void testFromHex_threeDigitsBlack() {
		final RGB rgb = RGB.fromHex("#000");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isZero();
		assertThat(rgb.green).isZero();
		assertThat(rgb.blue).isZero();
	}

	// ========== Invalid input tests ==========

	@Test
	void testFromHex_null() {
		assertThat(RGB.fromHex(null)).isNull();
	}

	@Test
	void testFromHex_empty() {
		assertThat(RGB.fromHex("")).isNull();
	}

	@Test
	void testFromHex_blank() {
		assertThat(RGB.fromHex("   ")).isNull();
	}

	@Test
	void testFromHex_tooShort_oneDigit() {
		assertThat(RGB.fromHex("#F")).isNull();
	}

	@Test
	void testFromHex_tooShort_twoDigits() {
		assertThat(RGB.fromHex("#FF")).isNull();
		assertThat(RGB.fromHex("AB")).isNull();
	}

	@Test
	void testFromHex_tooShort_fourDigits() {
		assertThat(RGB.fromHex("#FFFF")).isNull();
		assertThat(RGB.fromHex("1234")).isNull();
	}

	@Test
	void testFromHex_tooShort_fiveDigits() {
		assertThat(RGB.fromHex("#FFFFF")).isNull();
		assertThat(RGB.fromHex("12345")).isNull();
	}

	@Test
	void testFromHex_tooLong_sevenDigits() {
		assertThat(RGB.fromHex("#FFFFFFF")).isNull();
		assertThat(RGB.fromHex("1234567")).isNull();
	}

	@Test
	void testFromHex_tooLong_eightDigits() {
		assertThat(RGB.fromHex("#FFFFFFFF")).isNull();
	}

	@Test
	void testFromHex_invalidPrefix() {
		assertThat(RGB.fromHex("##FFFFFF")).isNull();
	}

	@Test
	void testFromHex_invalidSuffix() {
		assertThat(RGB.fromHex("FFFFFF#")).isNull();
	}

	// ========== RGB object behavior tests ==========

	@Test
	void testConstructor() {
		final RGB rgb = new RGB(100, 150, 200);
		assertThat(rgb.red).isEqualTo(100);
		assertThat(rgb.green).isEqualTo(150);
		assertThat(rgb.blue).isEqualTo(200);
	}

	@Test
	void testToString() {
		final RGB rgb = new RGB(255, 128, 64);
		assertThat(rgb).hasToString("RGB(255,128,64)");
	}

	@Test
	void testEquals_sameValues() {
		final RGB rgb1 = new RGB(100, 150, 200);
		final RGB rgb2 = new RGB(100, 150, 200);
		assertThat(rgb1).isEqualTo(rgb2);
	}

	@Test
	void testEquals_differentValues() {
		final RGB rgb1 = new RGB(100, 150, 200);
		final RGB rgb2 = new RGB(100, 150, 201);
		assertThat(rgb1).isNotEqualTo(rgb2);
	}

	@Test
	void testEquals_sameInstance() {
		final RGB rgb = new RGB(100, 150, 200);
		assertThat(rgb).isEqualTo(rgb);
	}

	@Test
	void testEquals_null() {
		final RGB rgb = new RGB(100, 150, 200);
		assertThat(rgb).isNotEqualTo(null);
	}

	@Test
	void testEquals_differentClass() {
		final RGB rgb = new RGB(100, 150, 200);
		assertThat(rgb).isNotEqualTo("not an RGB");
	}

	@Test
	void testHashCode_sameValues() {
		final RGB rgb1 = new RGB(100, 150, 200);
		final RGB rgb2 = new RGB(100, 150, 200);
		assertThat(rgb1).hasSameHashCodeAs(rgb2);
	}

	@Test
	void testHashCode_differentValues() {
		final RGB rgb1 = new RGB(100, 150, 200);
		final RGB rgb2 = new RGB(100, 150, 201);
		assertThat(rgb1.hashCode()).isNotEqualTo(rgb2.hashCode());
	}

	// ========== Edge cases and special values ==========

	@Test
	void testFromHex_maxValues() {
		final RGB rgb = RGB.fromHex("#FFFFFF");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isEqualTo(255);
		assertThat(rgb.green).isEqualTo(255);
		assertThat(rgb.blue).isEqualTo(255);
	}

	@Test
	void testFromHex_minValues() {
		final RGB rgb = RGB.fromHex("#000000");
		assertThat(rgb).isNotNull();
		assert rgb != null;
		assertThat(rgb.red).isZero();
		assertThat(rgb.green).isZero();
		assertThat(rgb.blue).isZero();
	}

	@Test
	void testFromHex_commonColors() {
		// Test some common web colors
		final RGB red = RGB.fromHex("#FF0000");
		assertThat(red).isNotNull();
		assert red != null;
		assertThat(red.red).isEqualTo(255);
		assertThat(red.green).isZero();
		assertThat(red.blue).isZero();

		final RGB green = RGB.fromHex("#00FF00");
		assertThat(green).isNotNull();
		assert green != null;
		assertThat(green.red).isZero();
		assertThat(green.green).isEqualTo(255);
		assertThat(green.blue).isZero();

		final RGB blue = RGB.fromHex("#0000FF");
		assertThat(blue).isNotNull();
		assert blue != null;
		assertThat(blue.red).isZero();
		assertThat(blue.green).isZero();
		assertThat(blue.blue).isEqualTo(255);

		final RGB yellow = RGB.fromHex("#FFFF00");
		assertThat(yellow).isNotNull();
		assert yellow != null;
		assertThat(yellow.red).isEqualTo(255);
		assertThat(yellow.green).isEqualTo(255);
		assertThat(yellow.blue).isZero();

		final RGB cyan = RGB.fromHex("#00FFFF");
		assertThat(cyan).isNotNull();
		assert cyan != null;
		assertThat(cyan.red).isZero();
		assertThat(cyan.green).isEqualTo(255);
		assertThat(cyan.blue).isEqualTo(255);

		final RGB magenta = RGB.fromHex("#FF00FF");
		assertThat(magenta).isNotNull();
		assert magenta != null;
		assertThat(magenta.red).isEqualTo(255);
		assertThat(magenta.green).isZero();
		assertThat(magenta.blue).isEqualTo(255);
	}
}
