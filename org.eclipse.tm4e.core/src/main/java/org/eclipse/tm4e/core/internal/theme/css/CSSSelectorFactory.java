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
 * Jochen Ulrich <jochenulrich@t-online.de> - exception messages
 */
package org.eclipse.tm4e.core.internal.theme.css;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tm4e.core.internal.theme.css.util.AbstractSelectorFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.SelectorFactory;
import org.w3c.css.sac.SimpleSelector;

@NonNullByDefault({})
public final class CSSSelectorFactory extends AbstractSelectorFactory {

	public static final SelectorFactory INSTANCE = new CSSSelectorFactory();

	@Override
	public ConditionalSelector createConditionalSelector(final SimpleSelector selector, final Condition condition) throws CSSException {
		return new CSSConditionalSelector((ExtendedSelector) selector, (ExtendedCondition) condition);
	}

	@Override
	public ElementSelector createElementSelector(final String uri, final String name) throws CSSException {
		return new CSSElementSelector(uri, name);
	}
}
