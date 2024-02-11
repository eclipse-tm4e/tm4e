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

import org.w3c.css.sac.SimpleSelector;

public interface ExtendedSelector extends SimpleSelector {

	/**
	 * @return Total number of {@link CSSClassCondition}s evaluated via {@link #nbMatch(String...)}
	 */
	@SuppressWarnings("javadoc")
	int nbClass();

	/**
	 * @return Number of matching {@link CSSClassCondition}s
	 */
	@SuppressWarnings("javadoc")
	int nbMatch(String... cssClassNames);
}
