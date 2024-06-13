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

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public final class NullSafetyHelper {

	public static <T> @NonNull T assertNonNull(final @Nullable T value) {
		if (value == null)
			throw new IllegalStateException("Null value!");
		return value;
	}

	/**
	 * Casts non-null value marked as {@link Nullable} to {@link NonNull}.
	 * <p>
	 * Only use if you are sure the value is non-null but annotation-based null analysis was not able to determine it.
	 * <p>
	 * This method is not meant for non-null input validation.
	 *
	 * @throws AssertionError if JVM assertions are enabled and the given value is null
	 */
	public static <T> @NonNull T castNonNull(final @Nullable T value) {
		assert value != null;
		return value;
	}

	@SuppressWarnings("null")
	private static <T> @NonNull T castNonNullUnsafe(final T value) {
		return value;
	}

	/**
	 * Casts a non-null value as {@link Nullable}.
	 */
	public static <T> @Nullable T castNullable(final T value) {
		return value;
	}

	public static <T> T defaultIfNull(final @Nullable T object, final T defaultValue) {
		if (object == null) {
			return defaultValue;
		}
		return object;
	}

	public static <T> T defaultIfNull(@Nullable final T object, final Supplier<T> defaultValue) {
		if (object == null) {
			return defaultValue.get();
		}
		return object;
	}

	/**
	 * Allows to initializes a @NonNull field with <code>null</code> that is lazy initialized
	 */
	@SuppressWarnings("unchecked")
	public static <T> @NonNull T lazyNonNull() {
		return (T) castNonNullUnsafe(null);
	}

	private NullSafetyHelper() {
	}
}
