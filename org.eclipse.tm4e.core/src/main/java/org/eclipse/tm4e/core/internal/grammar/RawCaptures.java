/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.grammar;

import java.util.HashMap;

import org.eclipse.tm4e.core.internal.parser.PropertySettable;
import org.eclipse.tm4e.core.internal.types.IRawCaptures;
import org.eclipse.tm4e.core.internal.types.IRawRule;
import org.eclipse.tm4e.core.internal.utils.DeepCloneable;

public class RawCaptures extends HashMap<String, IRawRule> implements IRawCaptures, DeepCloneable, PropertySettable<IRawRule> {

	private static final long serialVersionUID = 1L;

	@Override
	public RawCaptures deepClone() {
		final var clone = new RawCaptures();
		for (final var entry : entrySet()) {
			clone.put(entry.getKey(), DeepCloneable.deepClone(entry.getValue()));
		}
		return clone;
	}

	@Override
	public IRawRule getCapture(final String captureId) {
		return get(captureId);
	}

	@Override
	public Iterable<String> getCaptureIds() {
		return keySet();
	}

	@Override
	public void setProperty(final String name, final IRawRule value) {
		put(name, value);
	}
}