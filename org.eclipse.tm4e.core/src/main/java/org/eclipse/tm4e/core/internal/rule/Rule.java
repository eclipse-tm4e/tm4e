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
import org.eclipse.tm4e.core.internal.oniguruma.OnigCaptureIndex;
import org.eclipse.tm4e.core.internal.utils.RegexSource;
import org.eclipse.tm4e.core.internal.utils.StringUtils;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/rule.ts#L43">
 *      github.com/microsoft/vscode-textmate/blob/main/src/rule.ts</a>
 */
public abstract class Rule {

	final RuleId id;

	/** The root scopeName of the grammar that defined this rule (e.g., "text.xml"), or null for local rules. */
	public final @Nullable String grammarScope; // custom tm4e code - not from upstream (for TMPartitioner)

	private final @Nullable String name;
	private final boolean nameIsCapturing;

	private final @Nullable String contentName;
	private final boolean contentNameIsCapturing;

	Rule(final RuleId id, final @Nullable String name, final @Nullable String contentName, final @Nullable String grammarScope) {
		this.id = id;
		this.name = name;
		this.nameIsCapturing = RegexSource.hasCaptures(name);
		this.contentName = contentName;
		this.contentNameIsCapturing = RegexSource.hasCaptures(contentName);
		this.grammarScope = grammarScope; // custom tm4e code - not from upstream (for TMPartitioner)
	}

	public @Nullable String getName(final @Nullable CharSequence lineText, final OnigCaptureIndex @Nullable [] captureIndices) {
		final var name = this.name;
		if (!nameIsCapturing || name == null || lineText == null || captureIndices == null) {
			return name;
		}
		return RegexSource.replaceCaptures(name, lineText, captureIndices);
	}

	public @Nullable String getContentName(final CharSequence lineText, final OnigCaptureIndex[] captureIndices) {
		final var contentName = this.contentName;
		if (!contentNameIsCapturing || contentName == null) {
			return contentName;
		}
		return RegexSource.replaceCaptures(contentName, lineText, captureIndices);
	}

	public abstract void collectPatterns(IRuleRegistry grammar, RegExpSourceList out);

	public abstract CompiledRule compile(IRuleRegistry grammar, @Nullable String endRegexSource);

	public abstract CompiledRule compileAG(IRuleRegistry grammar, @Nullable String endRegexSource, boolean allowA, boolean allowG);

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> {
			sb.append("id=").append(id);
			sb.append(",name=").append(name);
		});
	}
}
