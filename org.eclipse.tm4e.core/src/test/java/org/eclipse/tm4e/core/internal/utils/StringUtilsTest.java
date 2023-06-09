/**
 * Copyright (c) 2023 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke - initial implementation
 */
package org.eclipse.tm4e.core.internal.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

	@Test
	void testSplitToArray() {
		assertArrayEquals(new String[] { "" }, StringUtils.splitToArray("", '.'));
		assertArrayEquals(new String[] { "abc" }, StringUtils.splitToArray("abc", '.'));
		assertArrayEquals(new String[] { "abc", "" }, StringUtils.splitToArray("abc.", '.'));
		assertArrayEquals(new String[] { "", "abc", "" }, StringUtils.splitToArray(".abc.", '.'));
		assertArrayEquals(new String[] { "", "" }, StringUtils.splitToArray(".", '.'));
		assertArrayEquals(new String[] { "", "", "", "" }, StringUtils.splitToArray("...", '.'));
		assertArrayEquals(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" },
				StringUtils.splitToArray("1.2.3.4.5.6.7.8", '.'));

		// test internal array resize
		assertArrayEquals(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" },
				StringUtils.splitToArray("1.2.3.4.5.6.7.8.9", '.'));
	}

	@Test
	void testSplitToList() {
		assertEquals(List.of(""), StringUtils.splitToList("", '.'));
		assertEquals(List.of("abc"), StringUtils.splitToList("abc", '.'));
		assertEquals(List.of("abc", ""), StringUtils.splitToList("abc.", '.'));
		assertEquals(List.of("", "abc", ""), StringUtils.splitToList(".abc.", '.'));
		assertEquals(List.of("", ""), StringUtils.splitToList(".", '.'));
		assertEquals(List.of("", "", "", ""), StringUtils.splitToList("...", '.'));
		assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8"),
				StringUtils.splitToList("1.2.3.4.5.6.7.8", '.'));

		// test internal array resize
		assertEquals(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"),
				StringUtils.splitToList("1.2.3.4.5.6.7.8.9", '.'));
	}
}
