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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public final class NullSafetyHelper {

	public static <T> Iterator<@NonNull T> castNonNull(@Nullable Iterator<T> value) {
		assert value != null;
		return value;
	}

	@NonNull
	public static <T> T castNonNull(@Nullable T value) {
		assert value != null;
		return value;
	}

	/**
	 * @param list a non-empty list with non-nullable elements
	 */
	@NonNull
	public static <T> T getLastElement(List<T> list) {
		final var value = list.get(list.size() - 1);
		assert value != null;
		return value;
	}

	private NullSafetyHelper() {
	}
}
