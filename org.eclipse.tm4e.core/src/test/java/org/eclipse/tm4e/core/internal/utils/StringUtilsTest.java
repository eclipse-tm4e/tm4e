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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

	@Test
	void testSplitToArray() {
		assertThat(StringUtils.splitToArray("", '.'))
				.containsExactly("");
		assertThat(StringUtils.splitToArray("abc", '.'))
				.containsExactly("abc");
		assertThat(StringUtils.splitToArray("abc.", '.'))
				.containsExactly("abc", "");
		assertThat(StringUtils.splitToArray(".abc.", '.'))
				.containsExactly("", "abc", "");
		assertThat(StringUtils.splitToArray(".", '.'))
				.containsExactly("", "");
		assertThat(StringUtils.splitToArray("...", '.'))
				.containsExactly("", "", "", "");
		assertThat(StringUtils.splitToArray("1.2.3.4.5.6.7.8", '.'))
				.containsExactly("1", "2", "3", "4", "5", "6", "7", "8");

		// test internal array resize
		assertThat(StringUtils.splitToArray("1.2.3.4.5.6.7.8.9", '.'))
				.containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9");
	}

	@Test
	void testSplitToList() {
		assertThat(StringUtils.splitToList("", '.'))
				.containsExactly("");
		assertThat(StringUtils.splitToList("abc", '.'))
				.containsExactly("abc");
		assertThat(StringUtils.splitToList("abc.", '.'))
				.containsExactly("abc", "");
		assertThat(StringUtils.splitToList(".abc.", '.'))
				.containsExactly("", "abc", "");
		assertThat(StringUtils.splitToList(".", '.'))
				.containsExactly("", "");
		assertThat(StringUtils.splitToList("...", '.'))
				.containsExactly("", "", "", "");
		assertThat(StringUtils.splitToList("1.2.3.4.5.6.7.8", '.'))
				.containsExactly("1", "2", "3", "4", "5", "6", "7", "8");

		// test internal list growth
		assertThat(StringUtils.splitToList("1.2.3.4.5.6.7.8.9", '.'))
				.containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9");
	}
}
