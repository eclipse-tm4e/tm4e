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
package org.eclipse.tm4e.core.internal.rule;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/rule.ts#L96">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts</a>
 */
public final class CaptureRule extends Rule {

	@Nullable
	public final Integer retokenizeCapturedWithRuleId;

	CaptureRule(int id, @Nullable String name, @Nullable String contentName,
			@Nullable Integer retokenizeCapturedWithRuleId) {
		super(id, name, contentName);
		this.retokenizeCapturedWithRuleId = retokenizeCapturedWithRuleId;
	}

	@Override
	public void collectPatternsRecursive(IRuleRegistry grammar, RegExpSourceList out, boolean isFirst) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompiledRule compile(IRuleRegistry grammar, @Nullable String endRegexSource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompiledRule compileAG(IRuleRegistry grammar, @Nullable String endRegexSource, boolean allowA,
			boolean allowG) {
		throw new UnsupportedOperationException();
	}
}
