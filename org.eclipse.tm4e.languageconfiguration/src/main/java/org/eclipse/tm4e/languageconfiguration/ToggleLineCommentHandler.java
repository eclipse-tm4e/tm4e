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
package org.eclipse.tm4e.languageconfiguration;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMultiTextSelection;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentSupport;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class ToggleLineCommentHandler extends AbstractHandler {
	public static final String TOGGLE_LINE_COMMENT_COMMAND_ID = "org.eclipse.tm4e.languageconfiguration.togglelinecommentcommand";
	public static final String ADD_BLOCK_COMMENT_COMMAND_ID = "org.eclipse.tm4e.languageconfiguration.addblockcommentcommand";
	public static final String REMOVE_BLOCK_COMMENT_COMMAND_ID = "org.eclipse.tm4e.languageconfiguration.removeblockcommentcommand";

	@Nullable
	private static <T> T adapt(@Nullable final Object sourceObject, final Class<T> adapter) {
		return Adapters.adapt(sourceObject, adapter);
	}

	@Nullable
	@Override
	public Object execute(@Nullable final ExecutionEvent event) throws ExecutionException {
		if (event == null) {
			return null;
		}
		final var part = HandlerUtil.getActiveEditor(event);
		final var editor = adapt(part, ITextEditor.class);
		if (editor == null) {
			return null;
		}
		final var selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof final ITextSelection textSelection) {
			final var input = editor.getEditorInput();
			final var docProvider = editor.getDocumentProvider();
			if (docProvider == null || input == null) {
				return null;
			}

			final var document = docProvider.getDocument(input);
			if (document == null) {
				return null;
			}

			final ContentTypeInfo info;
			try {
				info = ContentTypeHelper.findContentTypes(document);
				if (info == null)
					return null;
			} catch (final CoreException e) {
				return null;
			}
			final var contentTypes = info.getContentTypes();
			final var command = event.getCommand();
			final var commentSupport = getCommentSupport(contentTypes);
			if (commentSupport == null) {
				return null;
			}
			// Check if comment support is valid according the command to do.
			if (!isValid(commentSupport, command)) {
				return null;
			}

			final var target = adapt(editor, IRewriteTarget.class);
			if (target != null) {
				target.beginCompoundChange();
			}
			try {
				switch (command.getId()) {
				case TOGGLE_LINE_COMMENT_COMMAND_ID: {
					final var lineComment = commentSupport.getLineComment();
					if (lineComment != null && !lineComment.isEmpty()) {
						updateLineComment(document, textSelection, lineComment, editor);
					} else {
						final var blockComment = commentSupport.getBlockComment();
						if (blockComment != null) {
							final Set<Integer> lines = computeLines(textSelection, document);
							for (final int line : lines) {
								final int lineOffset = document.getLineOffset(line);
								final int lineLength = document.getLineLength(line);
								final var range = new TextSelection(lineOffset,
										line == document.getNumberOfLines() - 1 ? lineLength : lineLength - 1);
								toggleBlockComment(document, range, commentSupport, editor);
							}
						}
					}
					break;
				}

				case ADD_BLOCK_COMMENT_COMMAND_ID: {
					final IRegion existingBlock = getBlockComment(document, textSelection, commentSupport);
					final var blockComment = commentSupport.getBlockComment();
					if (existingBlock != null && blockComment != null) {
						addBlockComment(document, textSelection, blockComment, editor);
					}
					break;
				}

				case REMOVE_BLOCK_COMMENT_COMMAND_ID: {
					final IRegion existingBlock = getBlockComment(document, textSelection, commentSupport);
					final var blockComment = commentSupport.getBlockComment();
					if (existingBlock != null && blockComment != null) {
						removeBlockComment(document, textSelection, existingBlock, blockComment, editor);
					}
					break;
				}
				}
			} catch (final BadLocationException e) {
				// Caught by making no changes
			} finally {
				if (target != null) {
					target.endCompoundChange();
				}
			}
		}
		return null;
	}

	private Set<Integer> computeLines(final ITextSelection textSelection, final IDocument document)
			throws BadLocationException {
		final var regions = textSelection instanceof final IMultiTextSelection multiSelection
				? multiSelection.getRegions()
				: new IRegion[] { new Region(textSelection.getOffset(), textSelection.getLength()) };
		final var lines = new HashSet<Integer>();
		for (final var region : regions) {
			final int lineFrom = document.getLineOfOffset(region.getOffset());
			final int lineTo = document.getLineOfOffset(region.getOffset() + region.getLength());
			for (int line = lineFrom; line <= lineTo; line++) {
				lines.add(line);
			}
		}
		return lines;
	}

	private void toggleBlockComment(final IDocument document, final ITextSelection textSelection,
			final CommentSupport commentSupport, final ITextEditor editor) throws BadLocationException {
		final var existingBlock = getBlockComment(document, textSelection, commentSupport);
		final var blockComment = commentSupport.getBlockComment();
		if (blockComment == null) {
			return;
		}
		if (existingBlock == null) {
			addBlockComment(document, textSelection, blockComment, editor);
		} else {
			removeBlockComment(document, textSelection, existingBlock, blockComment, editor);
		}
	}

	/**
	 * Returns true if comment support is valid according the command to do and
	 * false otherwise.
	 *
	 * @param commentSupport
	 * @param command
	 *
	 * @return true if comment support is valid according the command to do and
	 *         false otherwise.
	 */
	private boolean isValid(final CommentSupport commentSupport, final Command command) {
		final var lineComment = commentSupport.getLineComment();
		if (TOGGLE_LINE_COMMENT_COMMAND_ID.equals(command.getId()) && lineComment != null
				&& !lineComment.isEmpty()) {
			return true;
		}
		// check if block comment is valid
		final var blockComment = commentSupport.getBlockComment();
		return blockComment != null
				&& !"".equals(blockComment.getKey())
				&& !"".equals(blockComment.getValue());
	}

	/**
	 * Returns the comment support from the given list of content types and null otherwise.
	 *
	 * @param contentTypes
	 *
	 * @return the comment support from the given list of content types and null otherwise.
	 */
	@Nullable
	private CommentSupport getCommentSupport(final IContentType[] contentTypes) {
		final var registry = LanguageConfigurationRegistryManager.getInstance();
		for (final var contentType : contentTypes) {
			if (!registry.shouldComment(contentType)) {
				continue;
			}
			final var commentSupport = registry.getCommentSupport(contentType);
			if (commentSupport != null) {
				return commentSupport;
			}
		}
		return null;
	}

	private void updateLineComment(final IDocument document, final ITextSelection selection, final String comment,
			final ITextEditor editor) throws BadLocationException {
		if (areLinesCommented(document, selection, comment)) {
			removeLineComments(document, selection, comment, editor);
		} else {
			addLineComments(document, selection, comment, editor);
		}
	}

	private boolean areLinesCommented(final IDocument document, final ITextSelection selection, final String comment)
			throws BadLocationException {
		int lineNumber = selection.getStartLine();
		while (lineNumber <= selection.getEndLine()) {
			final var lineRegion = document.getLineInformation(lineNumber);
			if (!document.get(lineRegion.getOffset(), lineRegion.getLength()).trim().startsWith(comment)) {
				return false;
			}
			lineNumber++;
		}
		return true;
	}

	@Nullable
	private IRegion getBlockComment(final IDocument document, final ITextSelection selection,
			final CommentSupport commentSupport) throws BadLocationException {
		if (selection.getText() == null) {
			return null;
		}
		final var blockComment = commentSupport.getBlockComment();
		if (blockComment == null) {
			return null;
		}
		final String text = document.get();
		final String open = blockComment.getKey();
		final String close = blockComment.getValue();
		final int selectionStart = selection.getOffset();
		final int selectionEnd = selectionStart + selection.getLength();
		int openOffset = TextUtils.startIndexOfOffsetTouchingString(text, selectionStart, open);
		if (openOffset == -1) {
			openOffset = text.lastIndexOf(open, selectionStart);
			if (openOffset == -1 || openOffset < document.getLineOffset(selection.getStartLine())) {
				return null;
			}
		}

		int closeOffset = TextUtils.startIndexOfOffsetTouchingString(text, selectionEnd, close);
		if (closeOffset == -1 || closeOffset < openOffset + open.length()) {
			closeOffset = text.indexOf(close, selectionEnd);
			final IRegion endLineRegion = document.getLineInformation(document.getLineOfOffset(selectionEnd));
			if (openOffset == -1 || closeOffset < openOffset + open.length()
					|| closeOffset > endLineRegion.getOffset() + endLineRegion.getLength()) {
				return null;
			}
		}

		// Make sure there isn't a different block closer before the one we found
		int othercloseOffset = text.indexOf(close, openOffset + open.length());
		while (othercloseOffset != -1 && othercloseOffset < closeOffset) {
			final int startOfLineOffset = document.getLineOffset(document.getLineOfOffset(othercloseOffset));
			if (commentSupport.getLineComment() != null && text.substring(startOfLineOffset, othercloseOffset)
					.indexOf(commentSupport.getLineComment()) != -1) {
				return null;
			}
			othercloseOffset = text.indexOf(close, othercloseOffset + close.length());
		}
		return new Region(openOffset, closeOffset - openOffset);
	}

	private void removeLineComments(final IDocument document, final ITextSelection selection, final String comment,
			final ITextEditor editor) throws BadLocationException {
		int lineNumber = selection.getStartLine();
		final int endLineNumber = selection.getEndLine();
		final String oldText = document.get();
		int deletedChars = 0;
		boolean isStartBeforeComment = false;

		while (lineNumber <= endLineNumber) {
			final int commentOffset = oldText.indexOf(comment, document.getLineOffset(lineNumber) + deletedChars);
			document.replace(commentOffset - deletedChars, comment.length(), "");
			if (deletedChars == 0) {
				isStartBeforeComment = commentOffset > selection.getOffset();
			}
			if (lineNumber != endLineNumber) {
				deletedChars += comment.length();
			}
			lineNumber++;
		}
		final var newSelection = new TextSelection(
				selection.getOffset() - (isStartBeforeComment ? 0 : comment.length()),
				selection.getLength() - deletedChars);
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}

	private void addLineComments(final IDocument document, final ITextSelection selection, final String comment,
			final ITextEditor editor) throws BadLocationException {
		int insertedChars = 0;

		for (final int lineNumber : computeLines(selection, document)) {
			document.replace(document.getLineOffset(lineNumber), 0, comment);
			insertedChars += comment.length();
		}
		final var newSelection = new TextSelection(selection.getOffset() + comment.length(),
				selection.getLength() + insertedChars);
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}

	private void removeBlockComment(final IDocument document, final ITextSelection selection,
			final IRegion existingBlock, final CharacterPair blockComment, final ITextEditor editor)
			throws BadLocationException {
		final int openOffset = existingBlock.getOffset();
		final int openLength = blockComment.getKey().length();
		final int closeOffset = existingBlock.getOffset() + existingBlock.getLength();
		final int closeLength = blockComment.getValue().length();
		document.replace(openOffset, openLength, "");
		document.replace(closeOffset - openLength, closeLength, "");

		int offsetFix = openLength;
		int lengthFix = 0;
		if (selection.getOffset() < openOffset + openLength) {
			offsetFix = selection.getOffset() - openOffset;
			lengthFix = openLength - offsetFix;
		}
		if (selection.getOffset() + selection.getLength() > closeOffset) {
			lengthFix += selection.getOffset() + selection.getLength() - closeOffset;
		}
		final var newSelection = new TextSelection(selection.getOffset() - offsetFix,
				selection.getLength() - lengthFix);
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}

	private void addBlockComment(final IDocument document, final ITextSelection selection,
			final CharacterPair blockComment, final ITextEditor editor) throws BadLocationException {
		document.replace(selection.getOffset(), 0, blockComment.getKey());
		document.replace(selection.getOffset() + selection.getLength() + blockComment.getKey().length(), 0,
				blockComment.getValue());
		final var newSelection = new TextSelection(selection.getOffset() + blockComment.getKey().length(),
				selection.getLength());
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}
}