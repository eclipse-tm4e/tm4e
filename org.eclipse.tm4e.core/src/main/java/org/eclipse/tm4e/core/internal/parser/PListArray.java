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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

final class PListArray extends PListObject {

	private final List<Object> values = new ArrayList<>();

	PListArray(@Nullable final PListObject parent) {
		super(parent);
	}

	@Override
	void addValue(final Object value) {
		values.add(value);
	}

	@Override
	Object getValue() {
		return values;
	}
}
