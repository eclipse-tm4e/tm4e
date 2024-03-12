/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal;

import static org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils.*;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.model.CursorConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.supports.IndentForEnterHelper.IIndentConverter;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextEditorPrefs;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.UI;

/**
 * {@link IAutoEditStrategy} which uses VSCode language-configuration.json.
 */
public class LanguageConfigurationAutoEditStrategy implements IAutoEditStrategy {

	private static final IContentType[] EMPTY_CONTENT_TYPES = new IContentType[0];

	private IContentType[] contentTypes = EMPTY_CONTENT_TYPES;
	private @Nullable IDocument document;

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/cursor/cursorTypeOperations.ts#L934">
	 *      github.com/microsoft/vscode/src/vs/editor/common/cursor/cursorTypeOperations.ts#typeWithInterceptors</a>
	 */
	@Override
	public void customizeDocumentCommand(@Nullable final IDocument doc, @Nullable final DocumentCommand command) {
		if (doc == null || command == null || command.text.isEmpty())
			return;

		if (!doc.equals(this.document)) {
			final var contentTypeInfo = ContentTypeHelper.findContentTypes(doc);
			this.contentTypes = contentTypeInfo == null ? EMPTY_CONTENT_TYPES : contentTypeInfo.getContentTypes();
			this.document = doc;
		}

		if (contentTypes.length == 0 || command.getCommandCount() > 1)
			return;

		if (isEnter(doc, command)) {
			// enter-key pressed
			final var cursorCfg = TextEditorPrefs.getCursorConfiguration(UI.getActiveTextEditor());
			onEnter(cursorCfg, doc, contentTypes, command);
			return;
		}

		final var registry = LanguageConfigurationRegistryManager.getInstance();

		if (command.text.length() == 1) {
			// auto surround pair
			final var textSelection = UI.getActiveTextSelection();
			if (textSelection != null && textSelection.getLength() > 0) {
				for (final IContentType contentType : contentTypes) {
					if (!registry.shouldSurroundingPairs(contentType))
						continue;

					final List<AutoClosingPair> surroundingPairs = registry.getSurroundingPairs(contentType);
					if (surroundingPairs.isEmpty())
						continue;

					for (final AutoClosingPair pair : surroundingPairs) {
						if (command.text.equals(pair.open)) {
							// surround selection with pairs
							try {
								command.addCommand(command.offset + textSelection.getLength(), 0, pair.close, null);
								command.length = 0;
								command.caretOffset = command.offset + textSelection.getLength() + pair.open.length();
								command.shiftsCaret = false;
							} catch (final BadLocationException ex) {
								LanguageConfigurationPlugin.logError(ex);
							}
							return;
						}
					}
				}
			}

			// auto close pair
			for (final IContentType contentType : contentTypes) {
				final var autoClosingPair = registry.getAutoClosingPair(doc.get(), command.offset, command.text, contentType);
				if (autoClosingPair == null) {
					continue;
				}
				command.caretOffset = command.offset + command.text.length();
				command.shiftsCaret = false;
				if (command.text.equals(autoClosingPair.open) && isFollowedBy(doc, command.offset, autoClosingPair.open)) {
					command.text = "";
				} else if (command.text.equals(autoClosingPair.close) && isFollowedBy(doc, command.offset, autoClosingPair.close)) {
					command.text = "";
				} else if (isAutoClosingAllowed(doc, contentType, command.offset, autoClosingPair)) {
					command.text += autoClosingPair.close;
				}
				return;
			}

			if (Arrays.stream(contentTypes)
					.flatMap(contentType -> registry.getEnabledAutoClosingPairs(contentType).stream())
					.anyMatch(charPair -> charPair.close.equals(command.text)
							&& isFollowedBy(doc, command.offset, charPair.close))) {
				command.caretOffset = command.offset + command.text.length();
				command.shiftsCaret = false;
				command.text = "";
			}

		} else {
			// auto-indent pasted text
			final var cursorCfg = TextEditorPrefs.getCursorConfiguration(UI.getActiveTextEditor());
			for (final IContentType contentType : contentTypes) {
				if (!registry.shouldIndentForEnter(contentType))
					continue;
				try {
					final var isPastedTextMultiLine = command.text.contains("\n");
					final var lineIndex = doc.getLineOfOffset(command.offset);
					final var isTargetLineBlank = TextUtils.isBlankLine(doc, lineIndex);

					if (isPastedTextMultiLine || isTargetLineBlank) {
						final var newIndent = registry.getGoodIndentForLine(doc, lineIndex, contentType, IIndentConverter.of(cursorCfg));
						if (newIndent != null) {
							final var lineStartOffset = doc.getLineOffset(lineIndex);

							// check if the content was pasted into a line while the cursor was not at the beginning of the line
							// but inside or at the end of an existing line indentation
							final var offsetInLine = command.offset - lineStartOffset;
							if (offsetInLine > 0 && doc.get(lineStartOffset, offsetInLine).isBlank()) {
								command.offset = lineStartOffset;
								command.length += offsetInLine;
							}
							command.text = TextUtils.replaceIndent(command.text, cursorCfg.indentSize,
									cursorCfg.normalizeIndentation(newIndent), false).toString();
							command.shiftsCaret = true;
						}
					}
				} catch (final BadLocationException ex) {
					LanguageConfigurationPlugin.logError(ex);
				}
			}
		}
	}

	/**
	 * @return true if auto closing is enabled for the given {@link AutoClosingPairConditional} at the given offset
	 */
	private boolean isAutoClosingAllowed(final IDocument doc, final IContentType contentType, final int offset,
			final AutoClosingPairConditional pair) {

		// only consider auto-closing if the next char is configured in autoCloseBefore
		try {
			final var ch = doc.getChar(offset);
			if (!Character.isWhitespace(ch)) {
				final var registry = LanguageConfigurationRegistryManager.getInstance();
				if (registry.getAutoCloseBefore(contentType).indexOf(ch) < 0)
					return false;
			}
		} catch (final Exception ex) {
			// ignore
		}

		if (!pair.notIn.isEmpty()) {
			final var docModel = TMModelManager.INSTANCE.connect(doc);
			try {
				final var lineIndex = doc.getLineOfOffset(offset);
				final var tokens = docModel.getLineTokens(lineIndex);
				if (tokens != null) {
					final var lineCharOffset = offset - doc.getLineOffset(lineIndex) - 1;
					TMToken tokenAtOffset = null;
					for (final var token : tokens) {
						if (token.startIndex > lineCharOffset)
							break;
						tokenAtOffset = token;
					}
					if (tokenAtOffset != null) {
						for (final var notIn : pair.notIn) {
							if (tokenAtOffset.type.contains(notIn))
								return false;
						}
					}
				}
			} catch (final BadLocationException ex) {
				// ignore
			}
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if the content after the given offset is followed
	 * by the given <code>value</code> and false otherwise.
	 *
	 * @param doc the document
	 * @param offset the offset
	 * @param value the content value to check
	 *
	 * @return <code>true</code> if the content after the given offset is followed
	 *         by the given <code>value</code> and false otherwise.
	 */
	private static boolean isFollowedBy(final IDocument doc, int offset, final String value) {
		for (int i = 0; i < value.length(); i++) {
			if (doc.getLength() <= offset)
				return false;

			try {
				if (doc.getChar(offset) != value.charAt(i))
					return false;
			} catch (final BadLocationException e) {
				return false;
			}
			offset++;
		}
		return true;
	}

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/cursor/cursorTypeOperations.ts#L299">
	 *      github.com/microsoft/vscode/src/vs/editor/common/cursor/cursorTypeOperations.ts</a>
	 */
	private static void onEnter(final CursorConfiguration cursorCfg, final IDocument doc, final IContentType[] contentTypes,
			final DocumentCommand command) {
		if (contentTypes.length > 0) {
			final var registry = LanguageConfigurationRegistryManager.getInstance();
			for (final IContentType contentType : contentTypes) {
				if (registry.shouldEnterAction(contentType)) {
					final var enterAction = registry.getEnterAction(doc, command.offset, contentType);
					if (enterAction != null) {
						command.shiftsCaret = false;
						final String newLine = command.text;
						switch (enterAction.indentAction) {
							case None: {
								// Nothing special
								final String increasedIndent = cursorCfg
										.normalizeIndentation(enterAction.indentation + enterAction.appendText);
								command.text = newLine + increasedIndent;
								command.caretOffset = command.offset + command.text.length();
								break;
							}
							case Indent: {
								// Indent once
								final String increasedIndent = cursorCfg
										.normalizeIndentation(enterAction.indentation + enterAction.appendText);
								command.text = newLine + increasedIndent;
								command.caretOffset = command.offset + command.text.length();
								break;
							}
							case IndentOutdent: {
								// Ultra special
								final String normalIndent = cursorCfg.normalizeIndentation(enterAction.indentation);
								final String increasedIndent = cursorCfg
										.normalizeIndentation(enterAction.indentation + enterAction.appendText);
								command.text = newLine + increasedIndent + newLine + normalIndent;
								command.caretOffset = command.offset + (newLine + increasedIndent).length();
								break;
							}
							case Outdent:
								final String indentation = getIndentationFromWhitespace(enterAction.indentation, cursorCfg);
								final String outdentedText = cursorCfg
										.outdentString(cursorCfg.normalizeIndentation(indentation + enterAction.appendText));
								command.text = newLine + outdentedText;
								command.caretOffset = command.offset + command.text.length();
								break;
						}
						return;
					}
				}

				if (registry.shouldIndentForEnter(contentType)) {
					final var indentForEnter = registry.getIndentForEnter(doc, command.offset, contentType, IIndentConverter.of(cursorCfg));
					if (indentForEnter != null) {
						final String newLine = command.text;
						command.text = newLine + cursorCfg.normalizeIndentation(indentForEnter.afterEnter);
						command.caretOffset = command.offset;
						return;
					}
				}
			}
		}

		// fail back to default for indentation
		new DefaultIndentLineAutoEditStrategy().customizeDocumentCommand(doc, command);
	}
}
