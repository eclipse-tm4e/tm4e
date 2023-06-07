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

import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;

public abstract class AbstractCombinatorCondition implements CombinatorCondition, ExtendedCondition {

	protected final ExtendedCondition firstCondition;
	protected final ExtendedCondition secondCondition;

	/**
	 * Creates a new CombinatorCondition object.
	 */
	protected AbstractCombinatorCondition(final ExtendedCondition c1, final ExtendedCondition c2) {
		firstCondition = c1;
		secondCondition = c2;
	}

	@Override
	public Condition getFirstCondition() {
		return firstCondition;
	}

	@Override
	public Condition getSecondCondition() {
		return secondCondition;
	}

	@Override
	public int getSpecificity() {
		return firstCondition.getSpecificity() + secondCondition.getSpecificity();
	}
}
