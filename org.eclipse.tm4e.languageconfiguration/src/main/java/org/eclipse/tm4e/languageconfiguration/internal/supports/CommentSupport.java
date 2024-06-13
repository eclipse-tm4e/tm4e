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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.internal.model.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.CommentRule;

public final class CommentSupport {

	private final @Nullable CommentRule comments;

	public CommentSupport(@Nullable final CommentRule comments) {
		this.comments = comments;
	}

	private boolean isInComment(final IDocument document, final int offset) {
		try {
			if (isInBlockComment(document.get(0, offset))) {
				return true;
			}
			final int line = document.getLineOfOffset(offset);
			final int lineOffset = document.getLineOffset(line);
			return isInLineComment(document.get(lineOffset, offset - lineOffset));
		} catch (final BadLocationException e) {
			return false;
		}
	}

	public @Nullable String getLineComment() {
		final var comments = this.comments;
		return comments == null ? null : comments.lineComment;
	}

	public @Nullable CharacterPair getBlockComment() {
		final var comments = this.comments;
		return comments == null ? null : comments.blockComment;
	}

	private boolean isInLineComment(final String indexLinePrefix) {
		final var comments = this.comments;
		if (comments == null)
			return false;

		final var lineComment = comments.lineComment;
		if (lineComment == null)
			return false;

		return indexLinePrefix.indexOf(lineComment) != -1;
	}

	private boolean isInBlockComment(final String indexPrefix) {
		final var comments = this.comments;
		if (comments == null)
			return false;

		final var blockComment = comments.blockComment;
		if (blockComment == null)
			return false;

		final String commentOpen = blockComment.open;
		final String commentClose = blockComment.close;
		int index = indexPrefix.indexOf(commentOpen);
		while (index != -1 && index < indexPrefix.length()) {
			final int closeIndex = indexPrefix.indexOf(commentClose, index + commentOpen.length());
			if (closeIndex == -1) {
				return true;
			}
			index = indexPrefix.indexOf(commentOpen, closeIndex + commentClose.length());
		}
		return false;
	}
}
