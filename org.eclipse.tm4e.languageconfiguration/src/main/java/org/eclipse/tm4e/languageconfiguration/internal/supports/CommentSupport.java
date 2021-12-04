/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class CommentSupport {

	private final Comments comments;

	public CommentSupport(Comments comments) {
		this.comments = comments;
	}

	public boolean isInComment(IDocument document, int offset) {
		try {
			if (isInBlockComment(document.get(0, offset))) {
				return true;
			}
			int line = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(line);
			return isInLineComment(document.get(lineOffset, offset - lineOffset));
		} catch (BadLocationException e) {
			return false;
		}
	}

	public String getLineComment() {
		return comments.getLineComment();
	}

	public CharacterPair getBlockComment() {
		return comments.getBlockComment();
	}

	private boolean isInLineComment(String indexLinePrefix) {
		return indexLinePrefix.indexOf(comments.getLineComment()) != -1;
	}

	private boolean isInBlockComment(String indexPrefix) {
		String commentOpen = comments.getBlockComment().getKey();
		String commentClose = comments.getBlockComment().getValue();
		int index = indexPrefix.indexOf(commentOpen);
		while (index != -1 && index < indexPrefix.length()) {
			int closeIndex = indexPrefix.indexOf(commentClose, index + commentOpen.length());
			if (closeIndex == -1) {
				return true;
			}
			index = indexPrefix.indexOf(commentOpen, closeIndex + commentClose.length());
		}
		return false;
	}
}
