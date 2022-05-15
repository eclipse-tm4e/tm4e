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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/rule.ts#L409">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts</a>
 */
public final class MatchRule extends Rule {

	private final RegExpSource match;
	public final List<@Nullable CaptureRule> captures;

	@Nullable
	private RegExpSourceList cachedCompiledPatterns;

	MatchRule(final RuleId id, @Nullable final String name, final String match,
			final List<@Nullable CaptureRule> captures) {
		super(id, name, null);
		this.match = new RegExpSource(match, this.id);
		this.captures = captures;
	}

	@Override
	public void collectPatternsRecursive(final IRuleRegistry grammar, final RegExpSourceList out,
			final boolean isFirst) {
		out.add(this.match);
	}

	@Override
	public CompiledRule compile(final IRuleRegistry grammar, @Nullable final String endRegexSource) {
		return getCachedCompiledPatterns(grammar).compile();
	}

	@Override
	public CompiledRule compileAG(final IRuleRegistry grammar, @Nullable final String endRegexSource,
			final boolean allowA, final boolean allowG) {
		return getCachedCompiledPatterns(grammar).compileAG(allowA, allowG);
	}

	private RegExpSourceList getCachedCompiledPatterns(final IRuleRegistry grammar) {
		var cachedCompiledPatterns = this.cachedCompiledPatterns;
		if (cachedCompiledPatterns == null) {
			cachedCompiledPatterns = new RegExpSourceList();
			this.collectPatternsRecursive(grammar, cachedCompiledPatterns, true);
			this.cachedCompiledPatterns = cachedCompiledPatterns;
		}
		return cachedCompiledPatterns;
	}
}
