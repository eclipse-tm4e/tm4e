/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.theme.css;

import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;

public abstract class AbstractCombinatorCondition implements CombinatorCondition, ExtendedCondition {

	private final Condition firstCondition;
	private final Condition secondCondition;

	/**
	 * Creates a new CombinatorCondition object.
	 */
	protected AbstractCombinatorCondition(final Condition c1, final Condition c2) {
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
		return ((ExtendedCondition) getFirstCondition()).getSpecificity()
				+ ((ExtendedCondition) getSecondCondition()).getSpecificity();
	}
}
