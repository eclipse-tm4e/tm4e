/**
 * Copyright (c) 2015-2018 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Lucas Bullen (Red Hat Inc.) - language configuration preferences
 * Sebastian Thomschke (Vegard IT) - move to separate class
 */
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import static org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils.getIndentationAtPosition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.internal.model.CompleteEnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;
import org.eclipse.tm4e.ui.internal.model.DocumentHelper;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/enterAction.ts">
 *      github.com/microsoft/vscode/src/vs/editor/common/languages/enterAction.ts</a>
 */
public final class EnterActionHelper {

	public static @Nullable CompleteEnterAction getEnterAction(
			// TODO autoIndent: EditorAutoIndentStrategy,
			final IDocument doc, final int offset, OnEnterSupport onEnterSupport) {
		// let scopedLineTokens = this.getScopedLineTokens(model, range.startLineNumber, range.startColumn);

		try {
			final int lineIndex = doc.getLineOfOffset(offset);
			final int lineLength = doc.getLineLength(lineIndex);
			final int lineStartOffset = doc.getLineOffset(lineIndex);

			// String scopeLineText = DocumentHelper.getLineTextOfOffset(document, offset, false);
			final String beforeEnterText = doc.get(lineStartOffset, offset - lineStartOffset);

			// selection support
			// if (range.isEmpty()) {
			final String afterEnterText = doc.get(offset, lineLength - (offset - lineStartOffset));
			//    afterEnterText = scopedLineText.substr(range.startColumn - 1 - scopedLineTokens.firstCharOffset);
			// } else {
			//    const endScopedLineTokens = this.getScopedLineTokens(model, range.endLineNumber, range.endColumn);
			//    afterEnterText = endScopedLineTokens.getLineContent().substr(range.endColumn - 1 - scopedLineTokens.firstCharOffset);
			// }

			String previousLineText = "";
			// if (range.startLineNumber > 1 && scopedLineTokens.firstCharOffset === 0) {
			if (lineIndex > 0) {
				//  // This is not the first line and the entire line belongs to this mode
				//  const oneLineAboveScopedLineTokens = getScopedLineTokens(model, range.startLineNumber - 1);
				//  if (oneLineAboveScopedLineTokens.languageId === scopedLineTokens.languageId) {
				//     // The line above ends with text belonging to the same mode
				//     previousLineText = oneLineAboveScopedLineTokens.getLineContent();
				previousLineText = DocumentHelper.getLineText(doc, lineIndex - 1, false);
				//  }
			}

			final @Nullable EnterAction enterResult = onEnterSupport.onEnter(previousLineText, beforeEnterText, afterEnterText);
			if (enterResult == null) {
				return null;
			}

			final IndentAction indentAction = enterResult.indentAction;
			@Nullable
			String appendText = enterResult.appendText;
			final @Nullable Integer removeText = enterResult.removeText;

			// Here we add `\t` to appendText first because enterAction is leveraging appendText and removeText to change indentation.
			if (appendText == null) {
				if (indentAction == IndentAction.Indent
						|| indentAction == IndentAction.IndentOutdent) {
					appendText = "\t";
				} else {
					appendText = "";
				}
			} else if (indentAction == IndentAction.Indent) {
				appendText = "\t" + appendText;
			}

			String indentation = getIndentationAtPosition(doc, offset);
			if (removeText != null) {
				indentation = indentation.substring(0, indentation.length() - removeText);
			}

			return new CompleteEnterAction(indentAction, appendText, removeText, indentation);
		} catch (final BadLocationException | RuntimeException ex) {
			// ignore
		}
		return null;
	}

	private EnterActionHelper() {
	}
}
