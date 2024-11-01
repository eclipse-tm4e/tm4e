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
package org.eclipse.tm4e.core.internal.grammar.raw;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.parser.PropertySettable;

public final class RawRepository extends PropertySettable.HashMap<IRawRule> implements IRawRepository {

	private static final Logger LOGGER = System.getLogger(RawRepository.class.getName());

	private static final long serialVersionUID = 1L;

	public static final String DOLLAR_BASE = "$base";
	public static final String DOLLAR_SELF = "$self";

	private IRawRule getOrThrow(final String key) {
		final IRawRule obj = get(key);
		if (obj == null) {
			throw new NoSuchElementException("Key '" + key + "' does not exit found");
		}
		return obj;
	}

	@Override
	@Nullable
	public IRawRule getRule(final String name) {
		try {
			return get(name);
		} catch (final ClassCastException ex) {
			// log ClassCastException with some context, to better troubleshoot issues like https://github.com/eclipse-tm4e/tm4e/issues/754
			LOGGER.log(Level.ERROR, "Unexpected ClassCastException in RawRepository.getRule(\"" + name + "\")", ex);
			throw ex;
		}
	}

	@Override
	public IRawRule getBase() {
		return getOrThrow(DOLLAR_BASE);
	}

	@Override
	public void setBase(final IRawRule base) {
		super.put(DOLLAR_BASE, base);
	}

	@Override
	public IRawRule getSelf() {
		return getOrThrow(DOLLAR_SELF);
	}

	@Override
	public void setSelf(final IRawRule self) {
		super.put(DOLLAR_SELF, self);
	}

	@Override
	public void putEntries(final PropertySettable<IRawRule> target) {
		for (final var entry : entrySet()) {
			target.setProperty(entry.getKey(), entry.getValue());
		}
	}
}
