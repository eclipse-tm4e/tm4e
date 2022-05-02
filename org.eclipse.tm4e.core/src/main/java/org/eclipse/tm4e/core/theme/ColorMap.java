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
package org.eclipse.tm4e.core.theme;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;

public class ColorMap {

	private boolean isFrozen;
	private int lastColorId = 0;
	private final List<String> id2color = new ArrayList<>();
	private final Map<String /* color */, @Nullable Integer /* ID color */ > color2id = new LinkedHashMap<>();

	public ColorMap() {
		this(null);
	}

	public ColorMap(@Nullable List<String> colorMap) {
		if (colorMap != null) {
			this.isFrozen = true;
			for (int i = 0, len = colorMap.size(); i < len; i++) {
				this.color2id.put(colorMap.get(i), i);
				this.id2color.add(colorMap.get(i));
			}
		} else {
			this.isFrozen = false;
		}
	}

	public int getId(@Nullable String color) {
		if (color == null) {
			return 0;
		}
		color = color.toUpperCase();
		Integer value = color2id.get(color);
		if (value != null) {
			return value;
		}
		if (this.isFrozen) {
			throw new TMException("Missing color in color map - " + color);
		}
		value = ++lastColorId;
		color2id.put(castNonNull(color), value);
		if (value >= id2color.size()) {
			id2color.add(castNonNull(color));
		} else {
			id2color.set(value, castNonNull(color));
		}
		return value;
	}

	@Nullable
	public String getColor(final int id) {
		for (final var entry : color2id.entrySet()) {
			if (id == entry.getValue()) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Set<String> getColorMap() {
		return color2id.keySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + color2id.hashCode();
		result = prime * result + lastColorId;
		return result;
	}

	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ColorMap other = (ColorMap) obj;
		return Objects.equals(color2id, other.color2id) && lastColorId == other.lastColorId;
	}
}
