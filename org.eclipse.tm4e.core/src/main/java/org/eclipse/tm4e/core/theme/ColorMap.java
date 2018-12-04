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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ColorMap {

	private int _lastColorId;
	private final Map<String /* color */, Integer /* ID color */ > _color2id;

	public ColorMap() {
		this._lastColorId = 0;
		this._color2id = new HashMap<>();
	}

	public int getId(String color) {
		if (color == null) {
			return 0;
		}
		color = color.toUpperCase();
		Integer value = this._color2id.get(color);
		if (value != null) {
			return value;
		}
		value = ++this._lastColorId;
		this._color2id.put(color, value);
		return value;
	}
	
	public String getColor(int id) {
		for (Entry<String, Integer> entry : _color2id.entrySet()) {
			if (id == entry.getValue()) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Set<String> getColorMap() {
		return this._color2id.keySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_color2id == null) ? 0 : _color2id.hashCode());
		result = prime * result + _lastColorId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColorMap other = (ColorMap) obj;
		if (_color2id == null) {
			if (other._color2id != null)
				return false;
		} else if (!_color2id.equals(other._color2id))
			return false;
		if (_lastColorId != other._lastColorId)
			return false;
		return true;
	}

	
}
