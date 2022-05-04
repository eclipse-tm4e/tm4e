/**
 * Copyright (c) 2015-2019 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.grammar;

import java.util.HashMap;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.parser.PropertySettable;
import org.eclipse.tm4e.core.internal.types.IRawRepository;
import org.eclipse.tm4e.core.internal.types.IRawRule;
import org.eclipse.tm4e.core.internal.utils.DeepCloneable;

public class RawRepository extends HashMap<String, IRawRule>
		implements IRawRepository, DeepCloneable, PropertySettable<IRawRule> {

	private static final long serialVersionUID = 1L;

	public static final String DOLLAR_BASE = "$base";
	public static final String DOLLAR_SELF = "$self";

	@Override
	public RawRepository deepClone() {
		final var clone = new RawRepository();
		for (final var entry : entrySet()) {
			clone.put(entry.getKey(), DeepCloneable.deepClone(entry.getValue()));
		}
		return clone;
	}

	@SuppressWarnings({ "null", "unused" })
	private IRawRule getSafe(final String key) {
		final IRawRule obj = get(key);
		if (obj == null) {
			throw new NoSuchElementException("Key '" + key + "' does not exit found");
		}
		return obj;
	}

	@Override
	@Nullable
	public IRawRule getRule(final String name) {
		return get(name);
	}

	@Override
	public IRawRule getBase() {
		return getSafe(DOLLAR_BASE);
	}

	@Override
	public void setBase(final IRawRule base) {
		super.put(DOLLAR_BASE, base);
	}

	@Override
	public IRawRule getSelf() {
		return getSafe(DOLLAR_SELF);
	}

	@Override
	public void setSelf(final IRawRule self) {
		super.put(DOLLAR_SELF, self);
	}

	@Override
	public void putEntries(PropertySettable<IRawRule> target) {
		for (final var entry : entrySet()) {
			target.setProperty(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void setProperty(final String name, final IRawRule value) {
		put(name, value);
	}
}