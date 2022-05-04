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
	public static <T> T deepClone(T obj) {
		if (obj instanceof DeepCloneable) {
			return (T) ((DeepCloneable) obj).deepClone();
		}

		if (obj instanceof List) {
			final List<@Nullable Object> clone;
			if (obj instanceof ArrayList) {
				final var source = (ArrayList<@Nullable Object>) obj;
				clone = (ArrayList<@Nullable Object>) source.clone();
			} else if (obj instanceof LinkedList) {
				final var source = (LinkedList<@Nullable Object>) obj;
				clone = (LinkedList<@Nullable Object>) source.clone();
			} else {
				final var source = (List<@Nullable Object>) obj;
				clone = new ArrayList<>(source);
			}
			clone.replaceAll(DeepCloneable::deepCloneNullable);
			return (T) clone;
		}

		if (obj instanceof Set) {
			final Set<@Nullable Object> source;
			final Set<@Nullable Object> clone;
			if (obj instanceof TreeSet) {
				final var cloneable = (TreeSet<@Nullable Object>) obj;
				source = cloneable;
				clone = (Set<@Nullable Object>) cloneable.clone();
			} else if (obj instanceof HashSet) {
				final var cloneable = (HashSet<@Nullable Object>) obj;
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
			if (obj instanceof HashMap) {
				final var source = (HashMap<Object, @Nullable Object>) obj;
				final var clone = (Map<Object, @Nullable Object>) source.clone();
				clone.replaceAll((k, v) -> deepCloneNullable(v));
				return (T) clone;
			}
			if (obj instanceof IdentityHashMap) {
				final var source = (IdentityHashMap<Object, @Nullable Object>) obj;
				final var clone = (Map<Object, @Nullable Object>) source.clone();
				clone.replaceAll((k, v) -> deepCloneNullable(v));
				return (T) clone;
			}
			if (obj instanceof TreeMap) {
				final var source = (TreeMap<Object, @Nullable Object>) obj;
				final var clone = (Map<Object, @Nullable Object>) source.clone();
				clone.replaceAll((k, v) -> deepCloneNullable(v));
				return (T) clone;
			}
		}

		// probably immutable
		return obj;
	}

	@Nullable
	public static <T> T deepCloneNullable(@Nullable T obj) {
		if (obj == null) {
			return null;
		}
		return deepClone(obj);
	}

	Object deepClone();
}
