/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.grammar.internal;

import java.util.Collection;
import java.util.List;

import org.eclipse.tm4e.core.internal.matcher.IMatcher;
import org.eclipse.tm4e.core.internal.matcher.IMatchesName;
import org.eclipse.tm4e.core.internal.matcher.Matcher;
import org.junit.Assert;
import org.junit.runner.Describable;
import org.junit.runner.Description;

import junit.framework.Test;
import junit.framework.TestResult;

public class MatcherTestImpl implements Test, Describable {

	private static final IMatchesName<List<String>> nameMatcher = new IMatchesName<List<String>>() {

		@Override
		public boolean match(Collection<String> identifers, List<String> stackElements) {
			int lastIndex = 0;
			// every
			for (String identifier : identifers) {
				lastIndex = match(identifier, stackElements, lastIndex);
				if (lastIndex == -1) {
					return false;
				}
			}
			return true;
		}

		private int match(String identifier, List<String> stackElements, int lastIndex) {
			for (int i = lastIndex; i < stackElements.size(); i++) {
				if (stackElements.get(i).equals(identifier)) {
					lastIndex = i + 1;
					return lastIndex;
				}
			}
			return -1;
		}
	};

	private String desc;
	private String expression;
	private List<String> input;
	private boolean result;

	public MatcherTestImpl() {
	}

	@Override
	public void run(TestResult result) {
		try {
			result.startTest(this);
			executeTest();
		} catch (Throwable e) {
			result.addError(this, e);
		} finally {
			result.endTest(this);
		}
	}

	private void executeTest() {
		IMatcher<List<String>> matcher = Matcher.createMatcher(expression, nameMatcher);
		boolean result = matcher.match(input);
		Assert.assertEquals(result, this.result);
	}

	@Override
	public int countTestCases() {
		return 1;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public Description getDescription() {
		return Description.createSuiteDescription(desc);
	}
}
