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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.oniguruma.OnigCaptureIndex;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/rule.ts#L504">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts</a>
 */
public final class BeginEndRule extends Rule {

	private final RegExpSource begin;
	public final List<@Nullable CaptureRule> beginCaptures;

	private final RegExpSource end;
	public final List<@Nullable CaptureRule> endCaptures;
	public final boolean endHasBackReferences;
	private final boolean applyEndPatternLast;

	final boolean hasMissingPatterns;
	final RuleId[] patterns;

	@Nullable
	private RegExpSourceList cachedCompiledPatterns;

	BeginEndRule(final RuleId id, @Nullable final String name, @Nullable final String contentName, final String begin,
			final List<@Nullable CaptureRule> beginCaptures, @Nullable final String end,
			final List<@Nullable CaptureRule> endCaptures, final boolean applyEndPatternLast,
			final CompilePatternsResult patterns) {
		super(id, name, contentName);
		this.begin = new RegExpSource(begin, this.id);
		this.beginCaptures = beginCaptures;
		this.end = new RegExpSource(defaultIfNull(end, "\uFFFF"), RuleId.END_RULE);
		this.endHasBackReferences = this.end.hasBackReferences;
		this.endCaptures = endCaptures;
		this.applyEndPatternLast = applyEndPatternLast;
		this.patterns = patterns.patterns;
		this.hasMissingPatterns = patterns.hasMissingPatterns;
	}

	public String getEndWithResolvedBackReferences(final String lineText, final OnigCaptureIndex[] captureIndices) {
		return this.end.resolveBackReferences(lineText, captureIndices);
	}

	@Override
	public void collectPatternsRecursive(final IRuleRegistry grammar, final RegExpSourceList out,
			final boolean isFirst) {
		if (isFirst) {
			for (final RuleId pattern : this.patterns) {
				final Rule rule = grammar.getRule(pattern);
				rule.collectPatternsRecursive(grammar, out, false);
			}
		} else {
			out.add(this.begin);
		}
	}

	@Override
	public CompiledRule compile(final IRuleRegistry grammar, @Nullable final String endRegexSource) {
		return getCachedCompiledPatterns(grammar, endRegexSource).compile();
	}

	@Override
	public CompiledRule compileAG(final IRuleRegistry grammar, @Nullable final String endRegexSource,
			final boolean allowA, final boolean allowG) {
		return getCachedCompiledPatterns(grammar, endRegexSource).compileAG(allowA, allowG);
	}

	private RegExpSourceList getCachedCompiledPatterns(final IRuleRegistry grammar,
			@Nullable final String endRegexSource) {
		var cachedCompiledPatterns = this.cachedCompiledPatterns;
		if (cachedCompiledPatterns == null) {
			cachedCompiledPatterns = new RegExpSourceList();

			collectPatternsRecursive(grammar, cachedCompiledPatterns, true);

			if (this.applyEndPatternLast) {
				cachedCompiledPatterns.add(this.endHasBackReferences ? this.end.clone() : this.end);
			} else {
				cachedCompiledPatterns.remove(this.endHasBackReferences ? this.end.clone() : this.end);
			}
			this.cachedCompiledPatterns = cachedCompiledPatterns;
		}
		if (this.endHasBackReferences && endRegexSource != null) {
			if (this.applyEndPatternLast) {
				cachedCompiledPatterns.setSource(cachedCompiledPatterns.length() - 1, endRegexSource);
			} else {
				cachedCompiledPatterns.setSource(0, endRegexSource);
			}
		}
		return cachedCompiledPatterns;
	}
}
