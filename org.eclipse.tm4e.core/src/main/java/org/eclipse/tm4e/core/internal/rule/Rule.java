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

import org.eclipse.tm4e.core.internal.oniguruma.IOnigCaptureIndex;
import org.eclipse.tm4e.core.internal.utils.RegexSource;

public class Rule {

	public int id;

	private boolean _nameIsCapturing;
	private String _name;

	private boolean _contentNameIsCapturing;
	private String _contentName;

	public Rule(int id, String name, String contentName) {
		this.id = id;
		this._name = name; // || null;
		this._nameIsCapturing = RegexSource.hasCaptures(this._name);
		this._contentName = contentName; // || null;
		this._contentNameIsCapturing = RegexSource.hasCaptures(this._contentName);
	}

	public String getName(String lineText, IOnigCaptureIndex[] captureIndices) {
		if (!this._nameIsCapturing) {
			return this._name;
		}
		return RegexSource.replaceCaptures(this._name, lineText, captureIndices);
	}

	public String getContentName(String lineText, IOnigCaptureIndex[] captureIndices) {
		if (!this._contentNameIsCapturing) {
			return this._contentName;
		}
		return RegexSource.replaceCaptures(this._contentName, lineText, captureIndices);
	}

	public void collectPatternsRecursive(IRuleRegistry grammar, RegExpSourceList out, boolean isFirst) {
		throw new UnsupportedOperationException("Implement me!");
	}

	public ICompiledRule compile(IRuleRegistry grammar, String endRegexSource, boolean allowA, boolean allowG) {
		throw new UnsupportedOperationException("Implement me!");
	}

}