/**
 * Copyright (c) 2023 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.utils;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class MoreCollectionsTest {

	@Test
	void testGetElementAt() {
		assertThat(MoreCollections.getElementAt(List.of(1, 2, 3), 0)).isEqualTo(1);
		assertThat(MoreCollections.getElementAt(List.of(1, 2, 3), -1)).isEqualTo(3);
		assertThat(MoreCollections.getElementAt(List.of(1, 2, 3), -2)).isEqualTo(2);
		assertThat(MoreCollections.getElementAt(List.of(1, 2, 3), -3)).isEqualTo(1);

		assertThatThrownBy(() -> MoreCollections.getElementAt(List.of(1, 2, 3), -4))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);
		assertThatThrownBy(() -> MoreCollections.getElementAt(List.of(1, 2, 3), 4))
				.isInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void testGetLastElement() {
		assertThat(MoreCollections.getLastElement(List.of(1, 2, 3))).isEqualTo(3);

		assertThatThrownBy(() -> MoreCollections.getLastElement(Collections.emptyList()))
				.isInstanceOf(IndexOutOfBoundsException.class);
	}

	@Test
	void testFindLastElement() {
		assertThat(MoreCollections.findLastElement(List.of(1, 2, 3))).isEqualTo(3);
		final Object lastElement = MoreCollections.findLastElement(Collections.emptyList());
		assertThat(lastElement).isNull();
	}
}
