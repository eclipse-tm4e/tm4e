/**
 * Copyright (c) 2018, 2022 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentSupport;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class ToggleLineCommentHandler extends AbstractHandler {

	public static final String TOGGLE_LINE_COMMENT_COMMAND_ID = "org.eclipse.tm4e.languageconfiguration.toggleLineCommentCommand";
	public static final String ADD_BLOCK_COMMENT_COMMAND_ID = "org.eclipse.tm4e.languageconfiguration.addBlockCommentCommand";
	public static final String REMOVE_BLOCK_COMMENT_COMMAND_ID = "org.eclipse.tm4e.languageconfiguration.removeBlockCommentCommand";

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
		if (selection instanceof ITextSelection textSelection) {
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
							if (blockComment != null && !blockComment.open.isEmpty() && !blockComment.close.isEmpty()) {
								final ITextSelection expandedSelection = expandTextSelectionToFullyIncludeCommentParts(document,
										textSelection, blockComment.open, blockComment.close);
								int shiftOffset = expandedSelection.getOffset() - textSelection.getOffset();
								int shiftLength = 0;

								Set<Integer> lines = computeLines(textSelection, document);

								// Filter out the blank lines and lines that are outside of the text selection
								final int selectionStartLine = textSelection.getStartLine();
								final int selectionEndLine = textSelection.getEndLine();
								final int lineRange[] = { -1, -1 };
								lines = lines.stream().filter(l -> l >= selectionStartLine && l <= selectionEndLine &&
										!TextUtils.isBlankLine(document, l))
										.map(l -> {
											lineRange[0] = lineRange[0] == -1 || lineRange[0] > l ? l : lineRange[0];
											lineRange[1] = lineRange[1] < l ? l : lineRange[1];
											return l;
										}).collect(Collectors.toSet());

								final int first = lineRange[0];
								final int last = lineRange[1];
								boolean isToAdd = false;
								for (final int line : lines) {
									final Set<ITypedRegion> existingBlocks = getBlockCommentPartsForLine(document, line,
											blockComment.open, blockComment.close);
									if (line == first) {
										isToAdd = existingBlocks.isEmpty();
									}

									// Remove existing comments block parts
									int deletedChars = 0;
									for (ITypedRegion existingBlock : existingBlocks) {
										existingBlock = new TypedRegion(existingBlock.getOffset() - deletedChars,
												existingBlock.getLength(), existingBlock.getType());
										document.replace(existingBlock.getOffset(), existingBlock.getLength(), "");
										deletedChars += existingBlock.getLength();

										final int selectionStart = textSelection.getOffset() + shiftOffset;
										final int selectionLength = textSelection.getLength() + shiftLength;
										final int selectionEnd = selectionStart + selectionLength;
										if (isBeforeSelection(existingBlock, selectionStart)) {
											shiftOffset -= existingBlock.getLength();
										} else if (isInsideSelection(existingBlock, selectionStart, selectionEnd)) {
											shiftLength -= existingBlock.getLength();
										}
									}
								}

								// Calculate the updated text selection
								textSelection = new TextSelection(textSelection.getOffset() + shiftOffset,
										textSelection.getLength() + shiftLength);
								shiftOffset = shiftLength = 0;

								// Add new block comments in case we need it
								if (isToAdd) {
									for (final int line : lines) {
										final int lineOffset = document.getLineOffset(line);
										final int lineLength = document.getLineLength(line);
										final String lineDelimiter = document.getLineDelimiter(line);
										final var range = new TextSelection(document, lineOffset,
												lineDelimiter != null ? lineLength - lineDelimiter.length() : lineLength);

										addBlockComment(document, range, blockComment.open, blockComment.close);

										if (line == first) {
											if (range.getOffset() <= textSelection.getOffset()) {
												shiftOffset += blockComment.open.length();
											}
											if (range.getOffset() + range.getLength() < textSelection.getOffset()
													+ textSelection.getLength()) {
												shiftLength += blockComment.close.length();
											}
										}
										if (line == last && line != first) {
											final int thisShiftLength = shiftLength;
											if (range.getOffset() <= textSelection.getOffset() + shiftOffset
													+ textSelection.getLength() + thisShiftLength) {
												shiftLength += blockComment.open.length();
											}
											if (range.getOffset() + range.getLength() < textSelection.getOffset()
													+ shiftOffset + textSelection.getLength() + thisShiftLength) {
												shiftLength += blockComment.close.length();
											}
										}
										if (line != first && line != last) {
											shiftLength += blockComment.open.length() + blockComment.close.length();
										}
									}

									// Calculate the updated text selection
									textSelection = new TextSelection(textSelection.getOffset() + shiftOffset,
											textSelection.getLength() + shiftLength);
								}

								editor.selectAndReveal(textSelection.getOffset(), textSelection.getLength());
							}
						}
						break;
					}

					case ADD_BLOCK_COMMENT_COMMAND_ID: {
						final var blockComment = commentSupport.getBlockComment();
						if (blockComment != null && !blockComment.open.isEmpty() && !blockComment.close.isEmpty()) {
							if (!isInsideBlockComment(document, textSelection, blockComment.open, blockComment.close)) {
								textSelection = removeBlockComments(document, textSelection, blockComment.open,
										blockComment.close);
								textSelection = addBlockComment(document, textSelection, blockComment.open,
										blockComment.close);
								editor.selectAndReveal(textSelection.getOffset(), 0);
							}
						} else {
							// Fallback to using line comment
							final var lineComment = commentSupport.getLineComment();
							if (lineComment != null && !lineComment.isEmpty()) {
								updateLineComment(document, textSelection, lineComment, editor);
							}
						}
						break;
					}

					case REMOVE_BLOCK_COMMENT_COMMAND_ID: {
						final var blockComment = commentSupport.getBlockComment();
						if (blockComment != null && !blockComment.open.isEmpty() && !blockComment.close.isEmpty()) {
							textSelection = removeBlockComments(document, textSelection, blockComment.open,
									blockComment.close);
							editor.selectAndReveal(textSelection.getOffset(), 0);
						} else {
							// Fallback to using line comment
							final var lineComment = commentSupport.getLineComment();
							if (lineComment != null && !lineComment.isEmpty()) {
								updateLineComment(document, textSelection, lineComment, editor);
							}
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

	private static boolean isBeforeSelection(final IRegion region, final int selectionStart) {
		final int regionStart = region.getOffset();
		final int regionEnd = regionStart + region.getLength();
		return regionStart < selectionStart && regionEnd <= selectionStart;
	}

	private static @Nullable ITypedRegion findCommentPartAtOffset(final IDocument document, final int offset, final String part)
			throws BadLocationException {
		final int length = document.getLength();
		for (int i = offset - 1; i >= 0 && i >= offset - part.length(); i--) {
			if (length < i + part.length()) {
				continue;
			}
			final String text = document.get(i, part.length());
			if (part.indexOf(text.charAt(0)) == -1) {
				return null;
			}
			if (text.indexOf(part) == 0) {
				return new TypedRegion(i, part.length(), part);
			}
		}
		return null;
	}

	private static boolean isInsideSelection(final IRegion region, final int selectionStart, final int selectionEnd) {
		final int regionStart = region.getOffset();
		final int regionEnd = regionStart + region.getLength();
		return selectionStart <= regionStart && selectionEnd >= regionEnd;
	}

	private static Set<Integer> computeLines(final ITextSelection textSelection, final IDocument document)
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

	/**
	 * Returns true if comment support is valid according the command to do and false otherwise.
	 *
	 * @return true if comment support is valid according the command to do and false otherwise.
	 */
	private boolean isValid(final CommentSupport commentSupport, final Command command) {
		// At least one of line or block comment is to be enabled by the language configuration
		final var lineComment = commentSupport.getLineComment();
		final var blockComment = commentSupport.getBlockComment();
		if ((lineComment == null || lineComment.isEmpty())
				&& (blockComment == null || blockComment.open.isEmpty() || blockComment.close.isEmpty())) {
			return false;
		}
		// A command should to be either Toggle Line comment or Add/Remove Block comment
		return TOGGLE_LINE_COMMENT_COMMAND_ID.equals(command.getId())
				|| ADD_BLOCK_COMMENT_COMMAND_ID.equals(command.getId())
				|| REMOVE_BLOCK_COMMENT_COMMAND_ID.equals(command.getId());
	}

	/**
	 * Returns the comment support from the given list of content types and null otherwise.
	 *
	 * @return the comment support from the given list of content types and null otherwise.
	 */
	@Nullable
	private static CommentSupport getCommentSupport(final IContentType[] contentTypes) {
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

	private static void updateLineComment(final IDocument document, final ITextSelection selection,
			final String comment, final ITextEditor editor) throws BadLocationException {
		if (areLinesCommented(document, selection, comment)) {
			removeLineComments(document, selection, comment, editor);
		} else {
			addLineComments(document, selection, comment, editor);
		}
	}

	private static boolean areLinesCommented(final IDocument document, final ITextSelection selection,
			final String comment) throws BadLocationException {
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

	private static Set<ITypedRegion> getBlockCommentParts(final IDocument document, final int offset, final int length,
			final String open, final String close) throws BadLocationException {
		final var result = new TreeSet<ITypedRegion>((r1, r2) -> r1.getOffset() - r2.getOffset());
		final String text = document.get(offset, length);
		int index = 0;
		while (true) {
			final int indexOpen = text.indexOf(open, index);
			final int indexClose = text.indexOf(close, index);

			if (indexOpen != -1 && (indexClose == -1 || indexOpen < indexClose)) {
				result.add(new TypedRegion(offset + indexOpen, open.length(), open));
				index = indexOpen + open.length();
			} else if (indexClose != -1) {
				result.add(new TypedRegion(offset + indexClose, close.length(), close));
				index = indexClose + close.length();
			} else {
				// No more block comment parts found
				break;
			}
		}

		return result;
	}

	private static ITextSelection expandTextSelectionToFullyIncludeCommentParts(final IDocument document,
			final ITextSelection textSelection, final String open, final String close) throws BadLocationException {
		// Expand text selection including a comment block if the selection start or end is
		// placed inside of comment start or end block
		int textSelectionStart = textSelection.getOffset();
		int textSelectionEnd = textSelectionStart + textSelection.getLength();
		ITypedRegion part = findCommentPartAtOffset(document, textSelectionStart, open);
		if (part == null) {
			part = findCommentPartAtOffset(document, textSelectionStart, close);
		}
		// Expand the beginning of text selection if needed
		textSelectionStart = part != null ? part.getOffset() : textSelectionStart;

		part = findCommentPartAtOffset(document, textSelectionEnd, open);
		if (part == null) {
			part = findCommentPartAtOffset(document, textSelectionEnd, close);
		}
		// Expand the ending of text selection if needed
		textSelectionEnd = part != null ? part.getOffset() + part.getLength() : textSelectionEnd;

		return new TextSelection(document, textSelectionStart, textSelectionEnd - textSelectionStart);
	}

	private static boolean isInsideBlockComment(final IDocument document,
			final ITextSelection textSelection, final String open, final String close) throws BadLocationException {
		final ITextSelection expandedSelection = expandTextSelectionToFullyIncludeCommentParts(document, textSelection, open,
				close);
		final String text = document.get(textSelection.getOffset(), textSelection.getLength());
		if (text.startsWith(open)) {
			return true;
		}
		return findNotClosedOpen(document, expandedSelection, open, close) != null;
	}

	private static @Nullable ITypedRegion findNotClosedOpen(final IDocument document,
			final ITextSelection textSelection, final String open, final String close) throws BadLocationException {
		final var backOrderedBeforeSelectioonComments = new TreeSet<ITypedRegion>(
				(r1, r2) -> r2.getOffset() - r1.getOffset());
		backOrderedBeforeSelectioonComments.addAll(getBlockCommentParts(document,
				0, textSelection.getOffset(), open, close));
		// Find the nearest open and or close block comment part
		if (!backOrderedBeforeSelectioonComments.isEmpty()) {
			final ITypedRegion comment = backOrderedBeforeSelectioonComments.first();
			if (open.equals(comment.getType())) {
				return comment;
			}
		}
		return null;
	}

	private static @Nullable ITypedRegion findNotOpenedClosen(final IDocument document,
			final ITextSelection textSelection, final String open, final String close) throws BadLocationException {
		final int textSelectionEnd = textSelection.getOffset() + textSelection.getLength();
		final Set<ITypedRegion> afterSelectioonComments = getBlockCommentParts(document,
				textSelectionEnd, document.getLength() - textSelectionEnd, open, close);

		// Find the nearest close block comment part
		for (final ITypedRegion comment : afterSelectioonComments) {
			if (close.equals(comment.getType())) {
				return comment;
			}
		}
		return null;
	}

	private static ITextSelection expandTextSelectionToSurroundingBlockComment(final IDocument document,
			final ITextSelection textSelection, final String open, final String close) throws BadLocationException {
		int textSelectionStart = textSelection.getOffset();
		int textSelectionEnd = textSelectionStart + textSelection.getLength();

		// Filter out the blank lines and lines that are outside of the text selection
		final int selectionStartLine = document.getLineOfOffset(textSelectionStart);
		final int selectionEndLine = document.getLineOfOffset(textSelectionEnd);
		final int lineRange[] = { -1, -1 };
		Set<Integer> lines = computeLines(new TextSelection(textSelectionStart, textSelectionEnd - textSelectionStart),
				document);
		lines = lines.stream().filter(l -> l >= selectionStartLine && l <= selectionEndLine && !TextUtils.isBlankLine(document, l))
				.map(l -> {
					lineRange[0] = lineRange[0] == -1 || lineRange[0] > l ? l : lineRange[0];
					lineRange[1] = lineRange[1] < l ? l : lineRange[1];
					return l;
				}).collect(Collectors.toSet());

		final int last = lineRange[1];
		final Set<ITypedRegion> comments = getBlockCommentParts(document, textSelectionStart,
				textSelectionEnd - textSelectionStart, open, close);
		final ITypedRegion[] brokenEnds = findBrokenBlockCommentPart(comments, open, close);

		int newCommentStart = textSelectionStart;
		int newCommentEnd = textSelectionEnd;
		if (brokenEnds[0] != null) {
			// Open comment part isn't closed - try to find the nearest closing comment part
			final ITypedRegion nextCloseCommentPart = findNotOpenedClosen(document, textSelection, open, close);
			if (nextCloseCommentPart != null) {
				newCommentEnd = nextCloseCommentPart.getOffset() + nextCloseCommentPart.getLength();
			} else { // Limit the new comment with the end of the last selected line
				newCommentEnd = document.getLineOffset(last) + document.getLineLength(last);
			}
		}
		if (brokenEnds[1] != null) {
			// Close comment part isn't opened - try to find the nearest previous open comment part
			final ITypedRegion prevOpenCommentPart = findNotClosedOpen(document, textSelection, open, close);
			if (prevOpenCommentPart != null) {
				newCommentStart = prevOpenCommentPart.getOffset();
			}
		}

		textSelectionStart = textSelectionStart > newCommentStart ? newCommentStart : textSelectionStart;
		textSelectionEnd = textSelectionEnd < newCommentEnd ? newCommentEnd : textSelectionEnd;
		return new TextSelection(document, textSelectionStart, textSelectionEnd - textSelectionStart);
	}

	private static ITypedRegion[] findBrokenBlockCommentPart(final Set<ITypedRegion> blockCommentParts, final String open,
			final String close) {
		final ITypedRegion[] brokenBlockComment = { null, null };
		blockCommentParts.stream().forEach(bc -> {
			if (open.equals(bc.getType())) {
				brokenBlockComment[0] = bc;	// Save as "broken" block comment open part, so the last open part will be the result
			} else if (close.equals(bc.getType())) {
				if (brokenBlockComment[0] != null) {
					brokenBlockComment[0] = null; // Clear "broken" block comment open part
				} else {
					if (brokenBlockComment[1] == null) {
						brokenBlockComment[1] = bc; // Save as "broken" block comment close part, we need the only first one
					}
				}
			}
		});

		return brokenBlockComment;
	}

	private Set<ITypedRegion> getBlockCommentPartsForLine(final IDocument document, final int line,
			final String open, final String close) throws BadLocationException {
		return getBlockCommentParts(document, document.getLineOffset(line), document.getLineLength(line), open, close);
	}

	private static void removeLineComments(final IDocument document, final ITextSelection selection,
			final String comment, final ITextEditor editor) throws BadLocationException {
		final String oldText = document.get();
		int deletedChars = 0;
		boolean isStartBeforeComment = false;

		// Filter out the blank lines and lines that are outside of the text selection
		final Set<Integer> lines = computeLines(selection, document).stream()
				.filter(l -> l >= selection.getStartLine() && l <= selection.getEndLine())
				.collect(Collectors.toSet());

		boolean isFirstLineUpdated = false;
		for (final int lineNumber : lines) {
			final int commentOffset = oldText.indexOf(comment, document.getLineOffset(lineNumber) + deletedChars);
			document.replace(commentOffset - deletedChars, comment.length(), "");
			deletedChars += comment.length();
			if (!isFirstLineUpdated) {
				isFirstLineUpdated = true;
				isStartBeforeComment = commentOffset >= selection.getOffset();
			}
		}
		final var newSelection = new TextSelection(
				selection.getOffset() - (isStartBeforeComment ? 0 : comment.length()),
				selection.getLength() - deletedChars + (isStartBeforeComment ? 0 : comment.length()));
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}

	private static void addLineComments(final IDocument document, final ITextSelection selection, final String comment,
			final ITextEditor editor) throws BadLocationException {
		int insertedChars = 0;

		// Filter out the blank lines and lines that are outside of the text selection
		final Set<Integer> lines = computeLines(selection, document).stream()
				.filter(l -> l >= selection.getStartLine() && l <= selection.getEndLine())
				.collect(Collectors.toSet());

		boolean isFirstLineUpdated = false;
		for (final int lineNumber : lines) {
			document.replace(document.getLineOffset(lineNumber), 0, comment);
			if (isFirstLineUpdated) {
				insertedChars += comment.length();
			} else {
				isFirstLineUpdated = true;
			}
		}
		final var newSelection = new TextSelection(selection.getOffset() + comment.length(),
				selection.getLength() + insertedChars);
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}

	private static ITextSelection removeBlockComments(final IDocument document, final ITextSelection textSelection,
			final String open, final String close) throws BadLocationException {
		ITextSelection expandedSelection = expandTextSelectionToFullyIncludeCommentParts(document, textSelection, open,
				close);
		int shiftOffset = expandedSelection.getOffset() - textSelection.getOffset();
		int shiftLength = 0;

		expandedSelection = expandTextSelectionToSurroundingBlockComment(document, expandedSelection, open, close);
		final Set<ITypedRegion> existingBlockParts = getBlockCommentParts(document, expandedSelection.getOffset(),
				expandedSelection.getLength(), open, close);

		// Remove existing comments block parts
		int deletedChars = 0;
		for (ITypedRegion existingBlock : existingBlockParts) {
			existingBlock = new TypedRegion(existingBlock.getOffset() - deletedChars, existingBlock.getLength(),
					existingBlock.getType());
			document.replace(existingBlock.getOffset(), existingBlock.getLength(), "");
			deletedChars += existingBlock.getLength();

			final int selectionStart = textSelection.getOffset() + shiftOffset;
			final int selectionLength = textSelection.getLength() + shiftLength;
			final int selectionEnd = selectionStart + selectionLength;
			if (isBeforeSelection(existingBlock, selectionStart)) {
				shiftOffset -= existingBlock.getLength();
			} else if (isInsideSelection(existingBlock, selectionStart, selectionEnd)) {
				shiftLength -= existingBlock.getLength();
			}
		}

		// Calculate the updated text selection
		return new TextSelection(textSelection.getOffset() + shiftOffset, textSelection.getLength() + shiftLength);
	}

	private static ITextSelection addBlockComment(final IDocument document, final ITextSelection selection,
			final String open, final String close) throws BadLocationException {
		document.replace(selection.getOffset(), 0, open);
		document.replace(selection.getOffset() + selection.getLength() + open.length(), 0, close);

		return new TextSelection(selection.getOffset() + open.length(), selection.getLength());
	}
}