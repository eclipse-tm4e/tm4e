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
package org.eclipse.tm4e.core.internal.parser;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

final class PListDict extends PListObject {

	private final Map<String, @Nullable Object> values;

	@Nullable
	private String lastKey;

	PListDict(@Nullable final PListObject parent, final MapFactory mapFactory) {
		super(parent);
		values = mapFactory.createMap();
	}

	@Override
	void addValue(final Object value) {
		values.put(castNonNull(lastKey), value);
	}

	@Nullable
	String getLastKey() {
		return lastKey;
	}

	@Override
	Object getValue() {
		return values;
	}

	void setLastKey(@Nullable final String lastKey) {
		this.lastKey = lastKey;
	}
}
