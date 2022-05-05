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
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import org.eclipse.jdt.annotation.Nullable;

public final class Comments {

	@Nullable
	private final String lineComment;

	@Nullable
	private final CharacterPair blockComment;

	public Comments(@Nullable final String lineComment, @Nullable final CharacterPair blockComment) {
		this.lineComment = lineComment;
		this.blockComment = blockComment;
	}

	@Nullable
	public String getLineComment() {
		return lineComment;
	}

	@Nullable
	public CharacterPair getBlockComment() {
		return blockComment;
	}
}