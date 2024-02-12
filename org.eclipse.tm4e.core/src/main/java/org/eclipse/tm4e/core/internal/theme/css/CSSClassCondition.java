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
package org.eclipse.tm4e.core.internal.theme.css;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.theme.css.util.AbstractAttributeCondition;

final class CSSClassCondition extends AbstractAttributeCondition implements ExtendedCondition {

	protected CSSClassCondition(final @Nullable String localName, final @Nullable String namespaceURI, final String value) {
		super(localName, namespaceURI, value);
	}

	@Override
	public short getConditionType() {
		return SAC_CLASS_CONDITION;
	}

	@Override
	public boolean getSpecified() {
		return true;
	}

	@Override
	public int nbClass() {
		return 1;
	}

	@Override
	public int nbMatch(final String... cssClassNames) {
		final String value = getValue();
		for (final String name : cssClassNames) {
			if (name.equals(value)) {
				return 1;
			}
		}
		return 0;
	}

	@Override
	public String toString() {
		return "CSSClass=='" + getValue() + "'";
	}
}
