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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.defaultIfNull;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.oniguruma.OnigCaptureIndex;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/167bbbd509356cc4617f250c0d754aef670ab14a/src/rule.ts#L209">
 *      github.com/microsoft/vscode-textmate/blob/main/src/rule.ts</a>
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

	private @Nullable RegExpSourceList cachedCompiledPatterns;

	BeginEndRule(final RuleId id, final @Nullable String name, final @Nullable String contentName, final String begin,
			final List<@Nullable CaptureRule> beginCaptures, final @Nullable String end,
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

	public String debugBeginRegExp() {
		return this.begin.getSource();
	}

	public String debugEndRegExp() {
		return this.end.getSource();
	}

	public String getEndWithResolvedBackReferences(final CharSequence lineText, final OnigCaptureIndex[] captureIndices) {
		return this.end.resolveBackReferences(lineText, captureIndices);
	}

	@Override
	public void collectPatterns(final IRuleRegistry grammar, final RegExpSourceList out) {
		out.add(this.begin);
	}

	@Override
	public CompiledRule compile(final IRuleRegistry grammar, final @Nullable String endRegexSource) {
		return getCachedCompiledPatterns(grammar, endRegexSource).compile();
	}

	@Override
	public CompiledRule compileAG(final IRuleRegistry grammar, final @Nullable String endRegexSource, final boolean allowA,
			final boolean allowG) {
		return getCachedCompiledPatterns(grammar, endRegexSource).compileAG(allowA, allowG);
	}

	private RegExpSourceList getCachedCompiledPatterns(final IRuleRegistry grammar, final @Nullable String endRegexSource) {
		var cachedCompiledPatterns = this.cachedCompiledPatterns;
		if (cachedCompiledPatterns == null) {
			cachedCompiledPatterns = new RegExpSourceList();

			for (final var pattern : this.patterns) {
				final var rule = grammar.getRule(pattern);
				rule.collectPatterns(grammar, cachedCompiledPatterns);
			}

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
