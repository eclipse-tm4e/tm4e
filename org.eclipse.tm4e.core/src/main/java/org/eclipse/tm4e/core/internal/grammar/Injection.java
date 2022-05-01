/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/Microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.grammar;

import java.util.List;

import org.eclipse.tm4e.core.internal.matcher.Matcher;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;

class Injection {

	private final Matcher<List<String>> matcher;
	public final int priority; // -1 | 0 | 1; // 0 is the default. -1 for 'L' and 1 for 'R'
	public final int ruleId;
	public final IRawGrammar grammar;

	public Injection(final Matcher<List<String>> matcher, final int ruleId, final IRawGrammar grammar, final int priority) {
		this.matcher = matcher;
		this.ruleId = ruleId;
		this.grammar = grammar;
		this.priority = priority;
	}

	public boolean match(final List<String> states) {
		return matcher.matches(states);
	}
}
