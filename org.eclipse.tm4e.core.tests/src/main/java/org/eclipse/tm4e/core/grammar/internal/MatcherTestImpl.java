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
package org.eclipse.tm4e.core.grammar.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;

import org.eclipse.tm4e.core.internal.matcher.Matcher;
import org.eclipse.tm4e.core.internal.matcher.MatcherWithPriority;

public class MatcherTestImpl {

	private String expression;
	private List<String> input;
	private boolean result;

	public MatcherTestImpl() {
	}


	public void executeTest() {
		Collection<MatcherWithPriority<List<String>>> matcher = Matcher.createMatchers(expression);
		boolean result = matcher.stream().anyMatch(m -> m.matcher.test(input));
		assertEquals(result, this.result);
	}

}
