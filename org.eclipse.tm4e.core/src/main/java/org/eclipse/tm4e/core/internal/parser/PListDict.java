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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

final class PListDict extends PListObject {

	private final Map<@Nullable String, @Nullable Object> values;

	PListDict(@Nullable final PListObject parent, final MapFactory mapFactory) {
		super(parent);
		values = mapFactory.createMap();
	}

	@Override
	void addValue(final Object value) {
		values.put(getLastKey(), value);
	}

	@Override
	Object getValue() {
		return values;
	}
}
