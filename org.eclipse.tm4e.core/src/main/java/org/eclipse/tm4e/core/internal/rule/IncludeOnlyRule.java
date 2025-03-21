/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode-textmate/
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
 *      "https://github.com/microsoft/vscode-textmate/blob/167bbbd509356cc4617f250c0d754aef670ab14a/src/rule.ts#L166">
 *      github.com/microsoft/vscode-textmate/blob/main/src/rule.ts</a>
 */
final class IncludeOnlyRule extends Rule {

	final boolean hasMissingPatterns;
	final RuleId[] patterns;

	private @Nullable RegExpSourceList cachedCompiledPatterns;

	IncludeOnlyRule(final RuleId id, final @Nullable String name, final @Nullable String contentName,
			final CompilePatternsResult patterns) {
		super(id, name, contentName);
		this.patterns = patterns.patterns;
		this.hasMissingPatterns = patterns.hasMissingPatterns;
	}

	@Override
	public void collectPatterns(final IRuleRegistry grammar, final RegExpSourceList out) {
		for (final RuleId pattern : this.patterns) {
			final Rule rule = grammar.getRule(pattern);
			rule.collectPatterns(grammar, out);
		}
	}

	@Override
	public CompiledRule compile(final IRuleRegistry grammar, final @Nullable String endRegexSource) {
		return getCachedCompiledPatterns(grammar).compile();
	}

	@Override
	public CompiledRule compileAG(final IRuleRegistry grammar, final @Nullable String endRegexSource,
			final boolean allowA, final boolean allowG) {
		return getCachedCompiledPatterns(grammar).compileAG(allowA, allowG);
	}

	private RegExpSourceList getCachedCompiledPatterns(final IRuleRegistry grammar) {
		var cachedCompiledPatterns = this.cachedCompiledPatterns;
		if (cachedCompiledPatterns == null) {
			cachedCompiledPatterns = new RegExpSourceList();
			this.collectPatterns(grammar, cachedCompiledPatterns);
			this.cachedCompiledPatterns = cachedCompiledPatterns;
		}
		return cachedCompiledPatterns;
	}
}
