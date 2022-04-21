/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
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
 *  - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.rule;

import java.util.List;

import org.eclipse.tm4e.core.internal.oniguruma.OnigCaptureIndex;

public final class BeginEndRule extends Rule {

	private final RegExpSource begin;
	public final List<CaptureRule> beginCaptures;
	private final RegExpSource end;
	public final boolean endHasBackReferences;
	public final List<CaptureRule> endCaptures;
	private final boolean applyEndPatternLast;
	final boolean hasMissingPatterns;
	final Integer[] patterns;
	private RegExpSourceList cachedCompiledPatterns;

	BeginEndRule(int id, String name, String contentName, String begin, List<CaptureRule> beginCaptures,
			String end, List<CaptureRule> endCaptures, boolean applyEndPatternLast, CompilePatternsResult patterns) {
		super(id, name, contentName);
		this.begin = new RegExpSource(begin, this.id);
		this.beginCaptures = beginCaptures;
		this.end = new RegExpSource(end, -1);
		this.endHasBackReferences = this.end.hasBackReferences();
		this.endCaptures = endCaptures;
		this.applyEndPatternLast = applyEndPatternLast;
		this.patterns = patterns.patterns;
		this.hasMissingPatterns = patterns.hasMissingPatterns;
	}

	public String getEndWithResolvedBackReferences(String lineText, OnigCaptureIndex[] captureIndices) {
		return this.end.resolveBackReferences(lineText, captureIndices);
	}

	@Override
	void collectPatternsRecursive(IRuleRegistry grammar, RegExpSourceList out, boolean isFirst) {
		if (isFirst) {
			for (Integer pattern : this.patterns) {
				Rule rule = grammar.getRule(pattern);
				rule.collectPatternsRecursive(grammar, out, false);
			}
		} else {
			out.push(this.begin);
		}
	}

	@Override
	public CompiledRule compile(IRuleRegistry grammar, String endRegexSource, boolean allowA, boolean allowG) {
		RegExpSourceList precompiled = this.precompile(grammar);
		if (this.end.hasBackReferences()) {
			if (this.applyEndPatternLast) {
				precompiled.setSource(precompiled.length() - 1, endRegexSource);
			} else {
				precompiled.setSource(0, endRegexSource);
			}
		}
		return this.cachedCompiledPatterns.compile(grammar, allowA, allowG);
	}

	private RegExpSourceList precompile(IRuleRegistry grammar) {
		if (this.cachedCompiledPatterns == null) {
			this.cachedCompiledPatterns = new RegExpSourceList();

			this.collectPatternsRecursive(grammar, this.cachedCompiledPatterns, true);

			if (this.applyEndPatternLast) {
				this.cachedCompiledPatterns.push(this.end.hasBackReferences() ? this.end.clone() : this.end);
			} else {
				this.cachedCompiledPatterns.unshift(this.end.hasBackReferences() ? this.end.clone() : this.end);
			}
		}
		return this.cachedCompiledPatterns;
	}

}
