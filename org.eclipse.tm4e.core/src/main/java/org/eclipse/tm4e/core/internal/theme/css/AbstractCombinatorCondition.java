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

/**
 * @see <a href=
 *      "https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/sac/AbstractCombinatorCondition.java">github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/sac/AbstractCombinatorCondition.java</a>
 */
abstract class AbstractCombinatorCondition implements CombinatorCondition, ExtendedCondition {

	final ExtendedCondition firstCondition;
	final ExtendedCondition secondCondition;

	/**
	 * Creates a new CombinatorCondition object.
	 */
	AbstractCombinatorCondition(final ExtendedCondition c1, final ExtendedCondition c2) {
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
}
