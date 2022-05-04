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
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/rule.ts#L453">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts</a>
 */
final class IncludeOnlyRule extends Rule {

	final boolean hasMissingPatterns;
	final int[] patterns;

	@Nullable
	private RegExpSourceList cachedCompiledPatterns;

	IncludeOnlyRule(final int id, @Nullable final String name, @Nullable final String contentName,
			final CompilePatternsResult patterns) {
		super(id, name, contentName);
		this.patterns = patterns.patterns;
		this.hasMissingPatterns = patterns.hasMissingPatterns;
	}

	@Override
	public void collectPatternsRecursive(final IRuleRegistry grammar, final RegExpSourceList out,
			final boolean isFirst) {
		for (final int pattern : this.patterns) {
			final Rule rule = grammar.getRule(pattern);
			rule.collectPatternsRecursive(grammar, out, false);
		}
	}

	@Override
	public CompiledRule compile(final IRuleRegistry grammar, @Nullable final String endRegexSource) {
		return getCachedCompiledPatterns(grammar).compile();
	}

	@Override
	public CompiledRule compileAG(final IRuleRegistry grammar, @Nullable final String endRegexSource, final boolean allowA,
			final boolean allowG) {
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
