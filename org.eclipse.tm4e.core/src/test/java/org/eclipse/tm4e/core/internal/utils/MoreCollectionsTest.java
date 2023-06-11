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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class MoreCollectionsTest {

	@Test
	void testGetElementAt() {
		assertEquals(1, MoreCollections.getElementAt(List.of(1, 2, 3), 0));
		assertEquals(3, MoreCollections.getElementAt(List.of(1, 2, 3), -1));
		assertEquals(2, MoreCollections.getElementAt(List.of(1, 2, 3), -2));
		assertEquals(1, MoreCollections.getElementAt(List.of(1, 2, 3), -3));
		try {
			MoreCollections.getElementAt(List.of(1, 2, 3), -4);
			fail("Expected ArrayIndexOutOfBoundsException");
		} catch (final ArrayIndexOutOfBoundsException ex) {
			//expected
		}
		try {
			MoreCollections.getElementAt(List.of(1, 2, 3), 4);
			fail("Expected ArrayIndexOutOfBoundsException");
		} catch (final ArrayIndexOutOfBoundsException ex) {
			//expected
		}
	}

	@Test
	void testGetLastElement() {
		assertEquals(3, MoreCollections.getLastElement(List.of(1, 2, 3)));
		try {
			MoreCollections.getLastElement(Collections.emptyList());
			fail("Expected IndexOutOfBoundsException");
		} catch (final IndexOutOfBoundsException ex) {
			//expected
		}
	}

	@Test
	void testFindLastElement() {
		assertEquals(3, MoreCollections.findLastElement(List.of(1, 2, 3)));
		assertEquals((Object) null, MoreCollections.findLastElement(Collections.emptyList()));
	}
}
