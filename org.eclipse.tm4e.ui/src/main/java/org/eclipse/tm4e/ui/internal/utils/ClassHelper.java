/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.utils;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Class Reflection utilities.
 *
 */
public final class ClassHelper {

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(final Object object, final String name, final Class<?> clazz) {
		final Field f = getDeclaredField(clazz, name);
		if (f != null) {
			try {
				return (T) f.get(object);
			} catch (final Exception e) {
				return null;
			}
		}
		return null;
	}

	@Nullable
	public static <T> T getFieldValue(final Object object, final String name) {
		return getFieldValue(object, name, object.getClass());
	}

	@Nullable
	public static Field getDeclaredField(@Nullable final Class<?> clazz, final String name) {
		if (clazz == null) {
			return null;
		}
		try {
			final Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return f;
		} catch (final NoSuchFieldException e) {
			return getDeclaredField(clazz.getSuperclass(), name);
		} catch (final SecurityException e) {
			return null;
		}
	}

	private ClassHelper() {
	}
}
