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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tm4e.core.internal.theme.css.util.AbstractConditionFactory;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CombinatorCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionFactory;

@NonNullByDefault({})
public final class CSSConditionFactory extends AbstractConditionFactory {

	public static final ConditionFactory INSTANCE = new CSSConditionFactory();

	@Override
	public CombinatorCondition createAndCondition(final Condition first, final Condition second) throws CSSException {
		return new CSSAndCondition((ExtendedCondition) first, (ExtendedCondition) second);
	}

	@Override
	public AttributeCondition createClassCondition(final String namespaceURI, final String value) throws CSSException {
		return new CSSClassCondition(null, "class", value);
	}
}
