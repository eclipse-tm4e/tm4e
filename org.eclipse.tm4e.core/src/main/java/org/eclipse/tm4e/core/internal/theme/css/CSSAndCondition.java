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

final class CSSAndCondition extends AbstractCombinatorCondition {

	/**
	 * Creates a new CombinatorCondition object.
	 */
	CSSAndCondition(final ExtendedCondition c1, final ExtendedCondition c2) {
		super(c1, c2);
	}

	@Override
	public short getConditionType() {
		return SAC_AND_CONDITION;
	}

	@Override
	public int nbClass() {
		return firstCondition.nbClass() + secondCondition.nbClass();
	}

	@Override
	public int nbMatch(final String... names) {
		return firstCondition.nbMatch(names) + secondCondition.nbMatch(names);
	}

	@Override
	public String toString() {
		return "(" + getFirstCondition() + " and " + getSecondCondition() + ")";
	}
}
