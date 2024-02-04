/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Sebastian Thomschke (Vegard IT) - translation and adaptation to Java
 */
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.internal.model.CursorConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.IndentForEnter;
import org.eclipse.tm4e.languageconfiguration.internal.supports.IndentRulesSupport.IndentConsts;
import org.eclipse.tm4e.languageconfiguration.internal.utils.Strings;
import org.eclipse.tm4e.ui.internal.model.DocumentHelper;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/autoIndent.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/autoIndent.ts</a>
 */
public final class IndentForEnterHelper {

	public interface IVirtualModel {
		String getLineContent(int lineIndex) throws BadLocationException;
	}

	public interface IIndentConverter {
		public static IIndentConverter of(final CursorConfiguration cursorCfg) {
			return new IIndentConverter() {
				@Override
				public String shiftIndent(final String indentation) {
					return indentation + "\t";
				}

				@Override
				public String unshiftIndent(final String indentation) {
					return cursorCfg.outdentString(indentation);
				}

				@Override
				public String normalizeIndentation(final String indent) {
					return cursorCfg.normalizeIndentation(indent);
				}
			};
		}

		String shiftIndent(String indentation);

		String unshiftIndent(String indentation);

		String normalizeIndentation(String indentation);
	}

	/**
	 * Get nearest preceding line which doesn't match unIndentPattern or contains all whitespace.
	 * Result:
	 * <li>-1: run into the boundary of embedded languages
	 * <li>0: every line above are invalid
	 * <li>else: nearest preceding line of the same language
	 */
	private static int getPrecedingValidLine(final IVirtualModel model, int lineIndex, final IndentRulesSupport indentRulesSupport)
			throws BadLocationException {
		//const languageId = model.tokenization.getLanguageIdAtPosition(lineNumber, 0);
		if (lineIndex > 0) {
			//int resultLineIndex = -1;

			for (int lastLineIndex = lineIndex - 1; lastLineIndex >= 0; lastLineIndex--) {
				//if (model.tokenization.getLanguageIdAtPosition(lastLineIndex, 0) !== languageId) {
				//	return resultLineIndex;
				//}

				final String text = model.getLineContent(lastLineIndex);
				if (text.isBlank() || indentRulesSupport.shouldIgnore(text)) {
					//resultLineIndex = lastLineIndex;
					continue;
				}
				return lastLineIndex;
			}
		}
		return -1;
	}

	private static final class InheritedIndentation {
		static final InheritedIndentation EMPTY = new InheritedIndentation("", null);

		final String indentation;
		final EnterAction.@Nullable IndentAction action;
		final @Nullable Integer lineIndex;

		InheritedIndentation(final String indentation, final EnterAction.@Nullable IndentAction action) {
			this.indentation = indentation;
			this.action = action;
			lineIndex = null;
		}

		InheritedIndentation(final String indentation, final EnterAction.@Nullable IndentAction action, final int lineIndex) {
			this.indentation = indentation;
			this.action = action;
			this.lineIndex = lineIndex;
		}
	}

	/**
	 * Get inherited indentation from above lines.
	 * <ol>
	 * <li>Find the nearest preceding line which doesn't match unIndentedLinePattern.
	 * <li>If this line matches indentNextLinePattern or increaseIndentPattern, it means that the indent level of `lineNumber` should be 1*
	 * greater than this line.
	 * <li>If this line doesn't match any indent rules
	 * <ol>
	 * <li>check whether the line above it matches indentNextLinePattern
	 * <li>If not, the indent level of this line is the result
	 * <li>If so, it means the indent of this line is *temporary*, go upward utill we find a line whose indent is not temporary (the same
	 * workflow a -> b -> c).
	 * </ol>
	 * <li>Otherwise, we fail to get an inherited indent from aboves. Return null and we should not touch the indent of `lineNumber`
	 * </ol>
	 *
	 * This function only returns the inherited indent based on above lines, it doesn't check whether current line should decrease or not.
	 */
	private static @Nullable InheritedIndentation getInheritIndentForLine(final IVirtualModel model, final boolean honorIntentialIndent,
			final int lineIndex, final IndentRulesSupport indentRulesSupport) throws BadLocationException {
		if (lineIndex < 1)
			return InheritedIndentation.EMPTY;

		// Use no indent if this is the first non-blank line
		for (var priorLineIndex = lineIndex - 1; priorLineIndex >= 0; priorLineIndex--) {
			if (!model.getLineContent(priorLineIndex).isEmpty()) {
				break;
			}
			if (priorLineIndex == 0)
				return InheritedIndentation.EMPTY;
		}

		final int precedingUnIgnoredLineIndex = getPrecedingValidLine(model, lineIndex, indentRulesSupport);
		if (precedingUnIgnoredLineIndex < -1)
			return null;
		if (precedingUnIgnoredLineIndex < 0)
			return InheritedIndentation.EMPTY;

		final String precedingUnIgnoredLineContent = model.getLineContent(precedingUnIgnoredLineIndex);
		if (indentRulesSupport.shouldIncrease(precedingUnIgnoredLineContent)
				|| indentRulesSupport.shouldIndentNextLine(precedingUnIgnoredLineContent)) {
			return new InheritedIndentation(
					Strings.getLeadingWhitespace(precedingUnIgnoredLineContent),
					IndentAction.Indent,
					precedingUnIgnoredLineIndex);
		}
		if (indentRulesSupport.shouldDecrease(precedingUnIgnoredLineContent)) {
			return new InheritedIndentation(
					Strings.getLeadingWhitespace(precedingUnIgnoredLineContent),
					null,
					precedingUnIgnoredLineIndex);
		}
		// precedingUnIgnoredLine can not be ignored.
		// it doesn't increase indent of following lines
		// it doesn't increase just next line
		// so current line is not affect by precedingUnIgnoredLine
		// and then we should get a correct inherited indentation from above lines
		if (precedingUnIgnoredLineIndex == 0)
			return new InheritedIndentation(
					Strings.getLeadingWhitespace(precedingUnIgnoredLineContent),
					null,
					precedingUnIgnoredLineIndex);

		final var previousLineIndex = precedingUnIgnoredLineIndex - 1;

		final var previousLineContent = model.getLineContent(previousLineIndex);
		final int previousLineIndentMetadata = indentRulesSupport.getIndentMetadata(previousLineContent);
		if (((previousLineIndentMetadata & (IndentConsts.INCREASE_MASK | IndentConsts.DECREASE_MASK)) == 0)
				&& ((previousLineIndentMetadata & IndentConsts.INDENT_NEXTLINE_MASK) != 0)) {
			int stopLineIndex = -1;
			for (int i = previousLineIndex - 1; i >= 0; i--) {
				if (indentRulesSupport.shouldIndentNextLine(model.getLineContent(i)))
					continue;
				stopLineIndex = i;
				break;
			}
			return new InheritedIndentation(
					Strings.getLeadingWhitespace(model.getLineContent(stopLineIndex + 1)),
					null,
					stopLineIndex + 1);
		}
		if (honorIntentialIndent)
			return new InheritedIndentation(
					Strings.getLeadingWhitespace(precedingUnIgnoredLineContent),
					null,
					precedingUnIgnoredLineIndex);

		// search from precedingUnIgnoredLine until we find one whose indent is not temporary
		for (int i = precedingUnIgnoredLineIndex; i >= 0; i--) {
			final String lineContent = model.getLineContent(i);
			if (indentRulesSupport.shouldIncrease(lineContent))
				return new InheritedIndentation(
						Strings.getLeadingWhitespace(lineContent),
						null,
						i);

			if (indentRulesSupport.shouldIndentNextLine(lineContent)) {
				int stopLineIndex = -1;
				for (int j = i - 1; j >= 0; j--) {
					if (indentRulesSupport.shouldIndentNextLine(model.getLineContent(i))) {
						continue;
					}
					stopLineIndex = j;
					break;
				}
				return new InheritedIndentation(
						Strings.getLeadingWhitespace(model.getLineContent(stopLineIndex + 1)),
						null,
						stopLineIndex + 1);
			}

			if (indentRulesSupport.shouldDecrease(lineContent))
				return new InheritedIndentation(
						Strings.getLeadingWhitespace(lineContent),
						null,
						i);
		}
		return new InheritedIndentation(
				Strings.getLeadingWhitespace(model.getLineContent(0)),
				null,
				0);
	}

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/autoIndent.ts#L216">
	 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/autoIndent.ts</a>
	 */
	public static @Nullable String getGoodIndentForLine(
			final IDocument doc,
			final int lineIndex,
			final IIndentConverter indentConverter,
			final IndentRulesSupport indentRulesSupport,
			final OnEnterSupport onEnterSupport) throws BadLocationException {

		final var virtualModel = new IVirtualModel() {
			@Override
			public String getLineContent(int lineIndex) throws BadLocationException {
				return DocumentHelper.getLineText(doc, lineIndex, false);
			}
		};
		InheritedIndentation indent = getInheritIndentForLine(virtualModel, true, lineIndex, indentRulesSupport);

		if (indent != null) {
			final String lineContent = virtualModel.getLineContent(lineIndex);
			final var inheritLineIndex = indent.lineIndex;
			if (inheritLineIndex != null) {
				// Apply enter action as long as there are only whitespace lines between inherited line and this line.
				boolean shouldApplyEnterRules = true;
				for (int inBetweenLine = inheritLineIndex; inBetweenLine < lineIndex - 1; inBetweenLine++) {
					if (!virtualModel.getLineContent(inBetweenLine).isBlank()) {
						shouldApplyEnterRules = false;
						break;
					}
				}
				if (shouldApplyEnterRules) {
					final EnterAction enterResult = onEnterSupport.onEnter("", virtualModel.getLineContent(inheritLineIndex), "");

					if (enterResult != null) {
						String indentation = Strings.getLeadingWhitespace(virtualModel.getLineContent(inheritLineIndex));

						if (enterResult.removeText != null) {
							indentation = Strings.getLeadingWhitespace(virtualModel.getLineContent(inheritLineIndex));
						}

						if (enterResult.indentAction == IndentAction.Indent
								|| enterResult.indentAction == IndentAction.IndentOutdent) {
							indentation = indentConverter.shiftIndent(indentation);
						} else if (enterResult.indentAction == IndentAction.Outdent) {
							indentation = indentConverter.unshiftIndent(indentation);
						}

						if (indentRulesSupport.shouldDecrease(lineContent)) {
							indentation = indentConverter.unshiftIndent(indentation);
						}

						if (enterResult.appendText != null) {
							indentation += enterResult.appendText;
						}

						return Strings.getLeadingWhitespace(indentation);
					}
				}
			}

			if (indentRulesSupport.shouldDecrease(lineContent)) {
				if (indent.action == IndentAction.Indent) {
					return indent.indentation;
				}
				return indentConverter.unshiftIndent(indent.indentation);
			}
			if (indent.action == IndentAction.Indent) {
				return indentConverter.shiftIndent(indent.indentation);
			}
			return indent.indentation;
		}
		return null;
	}

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/autoIndent.ts#L301">
	 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/autoIndent.ts</a>
	 */
	public static @Nullable IndentForEnter getIndentForEnter(
			final IDocument doc,
			final int offset,
			final IIndentConverter indentConverter,
			final IndentRulesSupport indentRulesSupport) {

		try {
			final int lineIndex = doc.getLineOfOffset(offset);
			final int lineLength = doc.getLineLength(lineIndex);
			final int lineStartOffset = doc.getLineOffset(lineIndex);

			// if (scopedLineTokens.firstCharOffset > 0 && lineTokens.getLanguageId(0) !== scopedLineTokens.languageId) {
			// 	// we are in the embedded language content
			// 	embeddedLanguage = true; // if embeddedLanguage is true, then we don't touch the indentation of current line
			// 	beforeEnterText = scopedLineText.substr(0, range.startColumn - 1 - scopedLineTokens.firstCharOffset);
			// } else {
			// 	beforeEnterText = lineTokens.getLineContent().substring(0, range.startColumn - 1);
			final String beforeEnterText = doc.get(lineStartOffset, offset - lineStartOffset);
			// }

			// let afterEnterText: string;
			// if (range.isEmpty()) {
			final String afterEnterText = doc.get(offset, lineLength - (offset - lineStartOffset));
			// 	afterEnterText = scopedLineText.substr(range.startColumn - 1 - scopedLineTokens.firstCharOffset);
			// } else {
			// 	const endScopedLineTokens = getScopedLineTokens(model, range.endLineNumber, range.endColumn);
			// 	afterEnterText = endScopedLineTokens.getLineContent().substr(range.endColumn - 1 - scopedLineTokens.firstCharOffset);
			// }

			final String beforeEnterResult = beforeEnterText;
			final String beforeEnterIndent = Strings.getLeadingWhitespace(beforeEnterText);

			final var virtualModel = new IVirtualModel() {

				@Override
				public String getLineContent(int theLineIndex) throws BadLocationException {
					if (theLineIndex == lineIndex) {
						return beforeEnterResult;
					}
					return DocumentHelper.getLineText(doc, theLineIndex, false);
				}
			};

			//final String currentLineIndent = TextUtils.getIndentationAtPosition(doc, offset);
			final InheritedIndentation afterEnterAction = getInheritIndentForLine(virtualModel, true, lineIndex + 1, indentRulesSupport);

			if (afterEnterAction == null) {
				final String beforeEnter = /*embeddedLanguage ? currentLineIndent :*/ beforeEnterIndent;
				return new IndentForEnter(
						beforeEnter,
						beforeEnter);
			}

			String afterEnterIndent = /*embeddedLanguage ? currentLineIndent : */afterEnterAction.indentation;

			if (afterEnterAction.action == IndentAction.Indent) {
				afterEnterIndent = indentConverter.shiftIndent(afterEnterIndent);
			}

			if (indentRulesSupport.shouldDecrease(afterEnterText)) {
				afterEnterIndent = indentConverter.unshiftIndent(afterEnterIndent);
			}

			return new IndentForEnter(
					/*embeddedLanguage ? currentLineIndent : */beforeEnterIndent,
					afterEnterIndent);
		} catch (final BadLocationException | RuntimeException ex) {
			return null;
		}
	}

	private IndentForEnterHelper() {
	}
}
