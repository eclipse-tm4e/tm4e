/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Describes how comments for a language work.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/d79132281222cdab77abeacca1af700e34c2f30b/src/vs/editor/common/languages/languageConfiguration.ts#L13">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L13</a>
 */
public final class CommentRule {

	/**
	 * The line comment token, like `// this is a comment`
	 */
	@Nullable
	public final String lineComment;

	/**
	 * The block comment character pair, like `/* block comment *&#47;`
	 */
	@Nullable
	public final CharacterPair blockComment;

	public CommentRule(@Nullable final String lineComment, @Nullable final CharacterPair blockComment) {
		this.lineComment = lineComment;
		this.blockComment = blockComment;
	}
}