/**
 * Copyright (c) 2015-2018 Angelo ZERR.
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

import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.SimpleSelector;

final class CSSConditionalSelector implements ConditionalSelector, ExtendedSelector {

	private final ExtendedSelector selector;
	private final ExtendedCondition condition;

	CSSConditionalSelector(final ExtendedSelector simpleSelector, final ExtendedCondition condition) {
		this.selector = simpleSelector;
		this.condition = condition;
	}

	@Override
	public short getSelectorType() {
		return SAC_CONDITIONAL_SELECTOR;
	}

	@Override
	public Condition getCondition() {
		return condition;
	}

	@Override
	public SimpleSelector getSimpleSelector() {
		return selector;
	}

	@Override
	public int nbClass() {
		return selector.nbClass() + condition.nbClass();
	}

	@Override
	public int nbMatch(final String... cssClassNames) {
		return selector.nbMatch(cssClassNames) + condition.nbMatch(cssClassNames);
	}
}
