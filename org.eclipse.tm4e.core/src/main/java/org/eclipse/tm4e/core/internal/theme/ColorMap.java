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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.TMException;

/**
 * Based on <a href="https://github.com/microsoft/vscode-textmate/blob/167bbbd509356cc4617f250c0d754aef670ab14a/src/theme.ts#L385">
 * github.com/microsoft/vscode-textmate/blob/main/src/theme.ts#ColorMap</a>.
 * <p>
 * See also <a href=
 * "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/supports/tokenization.ts#L155">
 * github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/tokenization.ts#ColorMap</a>
 */
public final class ColorMap {

	private final boolean _isFrozen;
	private int _lastColorId = 0;
	private final List<String> _id2color = new ArrayList<>();
	private final List<String> _id2colorUnmodifiable = Collections.unmodifiableList(_id2color);
	private final Map<String /*color*/, @Nullable Integer /*ID color*/> _color2id = new HashMap<>();

	public ColorMap() {
		this(null);
	}

	public ColorMap(final @Nullable List<String> _colorMap) {
		_id2color.add(""); // required since the upstream impl works with 1-based indexes
		if (_colorMap != null) {
			this._isFrozen = true;
			for (final String color : _colorMap) {
				final String color_upper = color.toUpperCase();
				this._color2id.put(color_upper, _id2color.size());
				this._id2color.add(color_upper);
			}
		} else {
			this._isFrozen = false;
		}
	}

	public int getId(final @Nullable String color) {
		if (color == null)
			return 0;

		final String color_upper = color.toUpperCase();
		Integer value = this._color2id.get(color_upper);
		if (value != null)
			return value;

		if (this._isFrozen)
			throw new TMException("Missing color in frozen color map:" + color_upper);

		value = ++this._lastColorId;
		_color2id.put(color_upper, value);
		_id2color.add(color_upper);
		return value;
	}

	public List<String> getColorMap() {
		return _id2colorUnmodifiable;
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof final ColorMap other)
			return _lastColorId == other._lastColorId
					&& _color2id.equals(other._color2id);
		return false;
	}

	@Override
	public int hashCode() {
		return 31 * (31 + _lastColorId) + _color2id.hashCode();
	}
}
