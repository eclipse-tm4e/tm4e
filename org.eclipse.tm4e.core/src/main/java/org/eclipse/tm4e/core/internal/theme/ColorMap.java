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
package org.eclipse.tm4e.core.internal.theme;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/88baacf1a6637c5ec08dce18cea518d935fcf0a0/src/theme.ts#L385">
 *      github.com/microsoft/vscode-textmate/blob/main/src/theme.ts</a>
 */
public final class ColorMap {

	private boolean _isFrozen;
	private int _lastColorId = -1; // -1 and not 0 as in upstream project on purpose
	private final List<String> _id2color = new ArrayList<>();
	private final Map<String /*color*/, @Nullable Integer /*ID color*/> _color2id = new LinkedHashMap<>();

	public ColorMap() {
		this(null);
	}

	public ColorMap(@Nullable final List<String> _colorMap) {
		if (_colorMap != null) {
			this._isFrozen = true;
			for (int i = 0, len = _colorMap.size(); i < len; i++) {
				this._color2id.put(_colorMap.get(i), i);
				this._id2color.add(_colorMap.get(i));
			}
		} else {
			this._isFrozen = false;
		}
	}

	public int getId(@Nullable final String _color) {
		if (_color == null) {
			return 0;
		}
		final var color = _color.toUpperCase();
		Integer value = _color2id.get(color);
		if (value != null) {
			return value;
		}
		if (this._isFrozen) {
			throw new TMException("Missing color in color map - " + color);
		}
		value = ++this._lastColorId;
		_color2id.put(color, value);
		if (value >= _id2color.size()) {
			_id2color.add(color);
		} else {
			_id2color.set(value, color);
		}
		return value;
	}

	public List<String> getColorMap() {
		return new ArrayList<>(_color2id.keySet());
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
		return _lastColorId == other._lastColorId
			&& _color2id.equals(other._color2id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _lastColorId;
		result = prime * result + _color2id.hashCode();
		return result;
	}
}
