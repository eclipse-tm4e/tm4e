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

import org.eclipse.tm4e.core.internal.oniguruma.IOnigCaptureIndex;

public class BeginEndRule extends Rule {

	private RegExpSource _begin;
	public List<CaptureRule> beginCaptures;
	private RegExpSource _end;
	public boolean endHasBackReferences;
	public List<CaptureRule> endCaptures;
	public boolean applyEndPatternLast;
	public final boolean hasMissingPatterns;
	public Integer[] patterns;
	private RegExpSourceList _cachedCompiledPatterns;

	public BeginEndRule(int id, String name, String contentName, String begin, List<CaptureRule> beginCaptures,
			String end, List<CaptureRule> endCaptures, boolean applyEndPatternLast, ICompilePatternsResult patterns) {
		super(id, name, contentName);
		this._begin = new RegExpSource(begin, this.id);
		this.beginCaptures = beginCaptures;
		this._end = new RegExpSource(end, -1);
		this.endHasBackReferences = this._end.hasBackReferences;
		this.endCaptures = endCaptures;
		this.applyEndPatternLast = applyEndPatternLast || false;
		this.patterns = patterns.patterns;
		this.hasMissingPatterns = patterns.hasMissingPatterns;
		this._cachedCompiledPatterns = null;
	}

	public String getEndWithResolvedBackReferences(String lineText, IOnigCaptureIndex[] captureIndices) {
		return this._end.resolveBackReferences(lineText, captureIndices);
	}

	public void collectPatternsRecursive(IRuleRegistry grammar, RegExpSourceList out, boolean isFirst) {
		if (isFirst) {
			int i;
			int len;
			Rule rule;

			for (i = 0, len = this.patterns.length; i < len; i++) {
				rule = grammar.getRule(this.patterns[i]);
				rule.collectPatternsRecursive(grammar, out, false);
			}
		} else {
			out.push(this._begin);
		}
	}

	public ICompiledRule compile(IRuleRegistry grammar, String endRegexSource, boolean allowA, boolean allowG) {
		RegExpSourceList precompiled = this._precompile(grammar);

		if (this._end.hasBackReferences) {
			if (this.applyEndPatternLast) {
				precompiled.setSource(precompiled.length() - 1, endRegexSource);
			} else {
				precompiled.setSource(0, endRegexSource);
			}
		}
		return this._cachedCompiledPatterns.compile(grammar, allowA, allowG);
	}

	private RegExpSourceList _precompile(IRuleRegistry grammar) {
		if (this._cachedCompiledPatterns == null) {
			this._cachedCompiledPatterns = new RegExpSourceList();

			this.collectPatternsRecursive(grammar, this._cachedCompiledPatterns, true);

			if (this.applyEndPatternLast) {
				this._cachedCompiledPatterns.push(this._end.hasBackReferences ? this._end.clone() : this._end);
			} else {
				this._cachedCompiledPatterns.unshift(this._end.hasBackReferences ? this._end.clone() : this._end);
			}
		}
		return this._cachedCompiledPatterns;
	}

}
