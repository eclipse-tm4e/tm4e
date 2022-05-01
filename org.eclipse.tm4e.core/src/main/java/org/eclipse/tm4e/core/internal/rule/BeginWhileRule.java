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
import org.eclipse.tm4e.core.internal.oniguruma.OnigCaptureIndex;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/rule.ts#L593">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts</a>
 */
public final class BeginWhileRule extends Rule {

	private final RegExpSource begin;
	public final List<@Nullable CaptureRule> beginCaptures;
	public final List<@Nullable CaptureRule> whileCaptures;
	private final RegExpSource _while;
	public final boolean whileHasBackReferences;
	final boolean hasMissingPatterns;
	final int[] patterns;

	@Nullable
	private RegExpSourceList cachedCompiledPatterns;

	@Nullable
	private RegExpSourceList cachedCompiledWhilePatterns;

	BeginWhileRule(int id, @Nullable String name, @Nullable String contentName, String begin,
			List<@Nullable CaptureRule> beginCaptures, String _while, List<@Nullable CaptureRule> whileCaptures,
			CompilePatternsResult patterns) {
		super(/* $location, */id, name, contentName);
		this.begin = new RegExpSource(begin, this.id);
		this.beginCaptures = beginCaptures;
		this.whileCaptures = whileCaptures;
		this._while = new RegExpSource(_while, -2);
		this.whileHasBackReferences = this._while.hasBackReferences;
		this.patterns = patterns.patterns;
		this.hasMissingPatterns = patterns.hasMissingPatterns;
	}

	public String getWhileWithResolvedBackReferences(String lineText, OnigCaptureIndex[] captureIndices) {
		return this._while.resolveBackReferences(lineText, captureIndices);
	}

	@Override
	public void collectPatternsRecursive(IRuleRegistry grammar, RegExpSourceList out, boolean isFirst) {
		if (isFirst) {
			Rule rule;
			for (int pattern : patterns) {
				rule = grammar.getRule(pattern);
				rule.collectPatternsRecursive(grammar, out, false);
			}
		} else {
			out.add(this.begin);
		}
	}

	@Override
	public CompiledRule compile(IRuleRegistry grammar, @Nullable String endRegexSource) {
		return getCachedCompiledPatterns(grammar).compile();
	}

	@Override
	public CompiledRule compileAG(IRuleRegistry grammar, @Nullable String endRegexSource, boolean allowA,
			boolean allowG) {
		return getCachedCompiledPatterns(grammar).compileAG(allowA, allowG);
	}

	private RegExpSourceList getCachedCompiledPatterns(final IRuleRegistry grammar) {
		var cachedCompiledPatterns = this.cachedCompiledPatterns;
		if (cachedCompiledPatterns == null) {
			cachedCompiledPatterns = new RegExpSourceList();
			collectPatternsRecursive(grammar, cachedCompiledPatterns, true);
			this.cachedCompiledPatterns = cachedCompiledPatterns;
		}
		return cachedCompiledPatterns;
	}

	public CompiledRule compileWhile(@Nullable final String endRegexSource) {
		return getCachedCompiledWhilePatterns(endRegexSource).compile();
	}

	public CompiledRule compileWhileAG(@Nullable final String endRegexSource, boolean allowA, boolean allowG) {
		return getCachedCompiledWhilePatterns(endRegexSource).compileAG(allowA, allowG);
	}

	private RegExpSourceList getCachedCompiledWhilePatterns(@Nullable final String endRegexSource) {
		var cachedCompiledWhilePatterns = this.cachedCompiledWhilePatterns;
		if (cachedCompiledWhilePatterns == null) {
			cachedCompiledWhilePatterns = new RegExpSourceList();
			cachedCompiledWhilePatterns.add(this.whileHasBackReferences ? this._while.clone() : this._while);
			if (whileHasBackReferences) {
				cachedCompiledWhilePatterns.setSource(0, endRegexSource != null ? endRegexSource : "\uFFFF");
			}
			this.cachedCompiledWhilePatterns = cachedCompiledWhilePatterns;
		}
		return cachedCompiledWhilePatterns;
	}
}
