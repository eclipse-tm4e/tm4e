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

public class BeginWhileRule extends Rule {

	private RegExpSource _begin;
	public List<CaptureRule> beginCaptures;
	public List<CaptureRule> whileCaptures;
	private RegExpSource _while;
	public boolean whileHasBackReferences;
	public boolean hasMissingPatterns;
	public Integer[] patterns;
	private RegExpSourceList _cachedCompiledPatterns;
	private RegExpSourceList _cachedCompiledWhilePatterns;

	public BeginWhileRule(/* $location:ILocation, */ int id, String name, String contentName, String begin,
			List<CaptureRule> beginCaptures, String _while, List<CaptureRule> whileCaptures,
			ICompilePatternsResult patterns) {
		super(/* $location, */id, name, contentName);
		this._begin = new RegExpSource(begin, this.id);
		this.beginCaptures = beginCaptures;
		this.whileCaptures = whileCaptures;
		this._while = new RegExpSource(_while, -2);
		this.whileHasBackReferences = this._while.hasBackReferences;
		this.patterns = patterns.patterns;
		this.hasMissingPatterns = patterns.hasMissingPatterns;
		this._cachedCompiledPatterns = null;
		this._cachedCompiledWhilePatterns = null;
	}

	public String getWhileWithResolvedBackReferences(String lineText, IOnigCaptureIndex[] captureIndices) {
		return this._while.resolveBackReferences(lineText, captureIndices);
	}

	public void collectPatternsRecursive(IRuleRegistry grammar, RegExpSourceList out, boolean isFirst) {
		if (isFirst) {
			Rule rule;
			for (int i = 0; i < patterns.length; i++) {
				rule = grammar.getRule(this.patterns[i]);
				rule.collectPatternsRecursive(grammar, out, false);
			}
		} else {
			out.push(this._begin);
		}
	}

	public ICompiledRule compile(IRuleRegistry grammar, String endRegexSource, boolean allowA, boolean allowG) {
		this._precompile(grammar);
		return this._cachedCompiledPatterns.compile(grammar, allowA, allowG);
	}

	private void _precompile(IRuleRegistry grammar) {
		if (this._cachedCompiledPatterns == null) {
			this._cachedCompiledPatterns = new RegExpSourceList();
			this.collectPatternsRecursive(grammar, this._cachedCompiledPatterns, true);
		}
	}

	public ICompiledRule compileWhile(IRuleRegistry grammar, String endRegexSource, boolean allowA, boolean allowG) {
		this._precompileWhile(grammar);
		if (this._while.hasBackReferences) {
			this._cachedCompiledWhilePatterns.setSource(0, endRegexSource);
		}
		return this._cachedCompiledWhilePatterns.compile(grammar, allowA, allowG);
	}

	private void _precompileWhile(IRuleRegistry grammar) {
		if (this._cachedCompiledWhilePatterns == null) {
			this._cachedCompiledWhilePatterns = new RegExpSourceList();
			this._cachedCompiledWhilePatterns.push(this._while.hasBackReferences ? this._while.clone() : this._while);
		}
	}

}
