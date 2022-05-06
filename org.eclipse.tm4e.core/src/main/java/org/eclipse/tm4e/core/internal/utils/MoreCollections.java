/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
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

import java.util.List;

import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.jdt.annotation.Nullable;

public final class MoreCollections {

	/**
	 * @return the last element or null if list is empty
	 */
	@Nullable
	public static <T> T findLastElement(@Nullable final List<T> list) {
		if (list == null || list.isEmpty())
			return null;
		return getLastElement(list);
	}

	/**
	 * @param list a non-empty list
	 * @param index the element to get. negative index counts from end of list, e.g. -1 = last element.
	 *
	 * @throws IndexOutOfBoundsException if the list is empty
	 */
	public static int getElementAt(final IntList list, int index) {
		if (index < 0)
			return list.get(list.size() - 1);
		return list.get(index);
	}

	/**
	 * @param list a non-empty list
	 * @param index the element to get. negative index counts from end of list, e.g. -1 = last element.
	 *
	 * @throws IndexOutOfBoundsException if the list is empty
	 */
	public static <T> T getElementAt(final List<T> list, int index) {
		if (index < 0)
			return list.get(list.size() - 1);
		return list.get(index);
	}

	/**
	 * @param list a non-empty list
	 */
	public static int getLastElement(final IntList list) {
		return list.get(list.size() - 1);
	}

	/**
	 * @param list a non-empty list
	 *
	 * @throws IndexOutOfBoundsException if the list is empty
	 */
	public static <T> T getLastElement(final List<T> list) {
		return list.get(list.size() - 1);
	}

	/**
	 * Removes the last element in this list.
	 *
	 * @return the element previously at the specified position
	 *
	 * @throws UnsupportedOperationException if the {@code remove} operation is not supported by this list
	 * @throws IndexOutOfBoundsException if the list is empty
	 */
	public static <T> T removeLastElement(final List<T> list) {
		return list.remove(list.size() - 1);
	}

	/**
	 * Removes the last element in this list.
	 *
	 * @return the element previously at the specified position
	 *
	 * @throws UnsupportedOperationException if the {@code remove} operation is not supported by this list
	 * @throws IndexOutOfBoundsException if the list is empty
	 */
	public static int removeLastElement(final MutableIntList list) {
		return list.removeAtIndex(list.size() - 1);
	}

	private MoreCollections() {
	}
}
