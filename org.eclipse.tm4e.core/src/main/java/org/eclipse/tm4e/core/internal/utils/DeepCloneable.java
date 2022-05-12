/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.Nullable;

public interface DeepCloneable {

	@SuppressWarnings("unchecked")
	static <T> T deepClone(final T obj) {
		if (obj instanceof final DeepCloneable deepCloneable) {
			return (T) deepCloneable.deepClone();
		}

		if (obj instanceof List<?>) {
			List<@Nullable ?> clone;
			if (obj instanceof final ArrayList<?> source) {
				clone = (ArrayList<@Nullable Object>) source.clone();
			} else if (obj instanceof final LinkedList<?> source) {
				clone = (LinkedList<@Nullable Object>) source.clone();
			} else {
				final var source = (List<@Nullable Object>) obj;
				clone = new ArrayList<>(source);
			}
			clone.replaceAll(DeepCloneable::deepCloneNullable);
			return (T) clone;
		}

		if (obj instanceof Set) {
			final Set<@Nullable ?> source;
			final Set<@Nullable Object> clone;
			if (obj instanceof final TreeSet<?> cloneable) {
				source = cloneable;
				clone = (Set<@Nullable Object>) cloneable.clone();
			} else if (obj instanceof final HashSet<?> cloneable) {
				source = cloneable;
				clone = (Set<@Nullable Object>) cloneable.clone();
			} else {
				source = (Set<@Nullable Object>) obj;
				clone = new HashSet<>();
			}
			clone.clear();
			for (final var e : source) {
				clone.add(deepCloneNullable(e));
			}
			return (T) clone;
		}

		if (obj instanceof Map) {
			if (obj instanceof final HashMap<?, ?> source) {
				final var clone = (Map<Object, @Nullable Object>) source.clone();
				clone.replaceAll((k, v) -> deepCloneNullable(v));
				return (T) clone;
			}
			if (obj instanceof final IdentityHashMap<?, ?> source) {
				final var clone = (Map<Object, @Nullable Object>) source.clone();
				clone.replaceAll((k, v) -> deepCloneNullable(v));
				return (T) clone;
			}
			if (obj instanceof final TreeMap<?, ?> source) {
				final var clone = (Map<Object, @Nullable Object>) source.clone();
				clone.replaceAll((k, v) -> deepCloneNullable(v));
				return (T) clone;
			}
		}

		// probably immutable
		return obj;
	}

	@Nullable
	static <T> T deepCloneNullable(@Nullable final T obj) {
		if (obj == null) {
			return null;
		}
		return deepClone(obj);
	}

	Object deepClone();
}
