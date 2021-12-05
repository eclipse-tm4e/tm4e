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

public abstract class Rule {

	public final int id;

	private final boolean nameIsCapturing;
	private final String name;

	private final boolean contentNameIsCapturing;
	private final String contentName;

	public Rule(int id, String name, String contentName) {
		this.id = id;
		this.name = name;
		this.nameIsCapturing = RegexSource.hasCaptures(this.name);
		this.contentName = contentName;
		this.contentNameIsCapturing = RegexSource.hasCaptures(this.contentName);
	}

	public String getName(String lineText, IOnigCaptureIndex[] captureIndices) {
		if (!this.nameIsCapturing) {
			return this.name;
		}
		return RegexSource.replaceCaptures(this.name, lineText, captureIndices);
	}

	public String getContentName(String lineText, IOnigCaptureIndex[] captureIndices) {
		if (!this.contentNameIsCapturing) {
			return this.contentName;
		}
		return RegexSource.replaceCaptures(this.contentName, lineText, captureIndices);
	}

	public abstract void collectPatternsRecursive(IRuleRegistry grammar, RegExpSourceList out, boolean isFirst);

	public abstract ICompiledRule compile(IRuleRegistry grammar, String endRegexSource, boolean allowA, boolean allowG);

}