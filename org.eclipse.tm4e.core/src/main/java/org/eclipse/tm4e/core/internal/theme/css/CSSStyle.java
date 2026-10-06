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

import java.util.List;

import org.eclipse.tm4e.core.internal.theme.Style;

public class CSSStyle extends Style {

	/** One class name list per selector of the rule, e.g. {@code [[keyword], [storage, type]]}. */
	private final List<List<String>> selectors;

	CSSStyle(final List<List<String>> selectors) {
		this.selectors = selectors;
	}

	/**
	 * Returns the class count of the most specific selector whose classes are all contained in the given names,
	 * or 0 if none matches.
	 */
	public int getSpecificity(final List<String> cssClassNames) {
		int best = 0;
		for (final List<String> classes : selectors) {
			if (classes.size() > best && cssClassNames.containsAll(classes)) {
				best = classes.size();
			}
		}
		return best;
	}
}
