/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentSupport;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.utils.ContentTypeInfo;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ToggleLineCommentHandle extends AbstractHandler {
	public static final String TOGGLE_LINE_COMMENT_COMMAND_ID = "org.eclipse.tm4e.languageconfiguration.togglelinecommentcommand";
	public static final String TOGGLE_BLOCK_COMMENT_COMMAND_ID = "org.eclipse.tm4e.languageconfiguration.toggleblockcommentcommand";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if (!(part instanceof ITextEditor)) {
			return null;
		}
		ITextEditor editor = (ITextEditor) part;
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (!(selection instanceof ITextSelection)) {
			return null;
		}
		ITextSelection textSelection = (ITextSelection) selection;
		IEditorInput input = editor.getEditorInput();
		IDocumentProvider docProvider = editor.getDocumentProvider();
		if (docProvider == null || input == null) {
			return null;
		}

		IDocument document = docProvider.getDocument(input);
		if (document == null) {
			return null;
		}

		ContentTypeInfo info;
		try {
			info = ContentTypeHelper.findContentTypes(document);
		} catch (CoreException e) {
			return null;
		}
		IContentType[] contentTypes = info.getContentTypes();
		Command command = event.getCommand();

		IRewriteTarget target = editor.getAdapter(IRewriteTarget.class);
		if (target != null) {
			target.beginCompoundChange();
		}
		try {
			LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
			for (IContentType contentType : contentTypes) {
				CommentSupport commentSupport = registry.getCommentSupport(contentType);
				if (!registry.shouldComment(contentType)) {
					continue;
				}
				if (TOGGLE_LINE_COMMENT_COMMAND_ID.equals(command.getId()) && commentSupport.getLineComment() != null) {
					updateLineComment(document, textSelection, commentSupport.getLineComment(), editor);
				} else if (TOGGLE_BLOCK_COMMENT_COMMAND_ID.equals(command.getId())
						&& commentSupport.getBlockComment() != null) {
					updateBlockComment(document, textSelection, commentSupport, editor);
				}
			}
		} catch (BadLocationException e) {
			// Caught by making no changes
		} finally {
			if (target != null) {
				target.endCompoundChange();
			}
		}
		return null;
	}

	private void updateLineComment(IDocument document, ITextSelection selection, String comment, ITextEditor editor)
			throws BadLocationException {
		if (areLinesCommented(document, selection, comment)) {
			removeLineComments(document, selection, comment, editor);
		} else {
			addLineComments(document, selection, comment, editor);
		}
	}

	private void updateBlockComment(IDocument document, ITextSelection selection, CommentSupport commentSupport,
			ITextEditor editor) throws BadLocationException {

		IRegion existingBlock = getBlockComment(document, selection, commentSupport);
		if (existingBlock != null) {
			removeBlockComment(document, selection, existingBlock, commentSupport.getBlockComment(), editor);
		} else {
			addBlockComment(document, selection, commentSupport.getBlockComment(), editor);
		}

	}

	private boolean areLinesCommented(IDocument document, ITextSelection selection, String comment)
			throws BadLocationException {
		int lineNumber = selection.getStartLine();
		while (lineNumber <= selection.getEndLine()) {
			IRegion lineRegion = document.getLineInformation(lineNumber);
			if (!document.get(lineRegion.getOffset(), lineRegion.getLength()).trim().startsWith(comment)) {
				return false;
			}
			lineNumber++;
		}
		return true;
	}

	private IRegion getBlockComment(IDocument document, ITextSelection selection, CommentSupport commentSupport)
			throws BadLocationException {
		if (selection.getText() == null) {
			return null;
		}
		String text = document.get();
		String open = commentSupport.getBlockComment().getKey();
		String close = commentSupport.getBlockComment().getValue();
		int selectionStart = selection.getOffset();
		int selectionEnd = selectionStart + selection.getLength();
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
			IRegion endLineRegion = document.getLineInformation(document.getLineOfOffset(selectionEnd));
			if (openOffset == -1 || closeOffset < openOffset + open.length()
					|| closeOffset > endLineRegion.getOffset() + endLineRegion.getLength()) {
				return null;
			}
		}

		// Make sure there isn't a different block closer before the one we found
		int othercloseOffset = text.indexOf(close, openOffset + open.length());
		while (othercloseOffset != -1 && othercloseOffset < closeOffset) {
			int startOfLineOffset = document.getLineOffset(document.getLineOfOffset(othercloseOffset));
			if (commentSupport.getLineComment() != null && text.substring(startOfLineOffset, othercloseOffset)
					.indexOf(commentSupport.getLineComment()) != -1) {
				return null;
			}
			othercloseOffset = text.indexOf(close, othercloseOffset + close.length());
		}
		return new Region(openOffset, closeOffset - openOffset);
	}

	private void removeLineComments(IDocument document, ITextSelection selection, String comment, ITextEditor editor)
			throws BadLocationException {
		int lineNumber = selection.getStartLine();
		int endLineNumber = selection.getEndLine();
		String oldText = document.get();
		int deletedChars = 0;
		Boolean isStartBeforeComment = false;

		while (lineNumber <= endLineNumber) {
			int commentOffset = oldText.indexOf(comment, document.getLineOffset(lineNumber));
			document.replace(commentOffset - deletedChars, comment.length(), "");
			if (deletedChars == 0) {
				isStartBeforeComment = commentOffset > selection.getOffset();
			}
			if (lineNumber != endLineNumber) {
				deletedChars += comment.length();
			}
			lineNumber++;
		}
		ITextSelection newSelection = new TextSelection(
				selection.getOffset() - (isStartBeforeComment ? 0 : comment.length()),
				selection.getLength() - deletedChars);
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}

	private void addLineComments(IDocument document, ITextSelection selection, String comment, ITextEditor editor)
			throws BadLocationException {
		int lineNumber = selection.getStartLine();
		int endLineNumber = selection.getEndLine();
		int insertedChars = 0;

		while (lineNumber <= endLineNumber) {
			document.replace(document.getLineOffset(lineNumber), 0, comment);
			if (lineNumber != endLineNumber) {
				insertedChars += comment.length();
			}
			lineNumber++;
		}
		ITextSelection newSelection = new TextSelection(selection.getOffset() + comment.length(),
				selection.getLength() + insertedChars);
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}

	private void removeBlockComment(IDocument document, ITextSelection selection, IRegion existingBlock,
			CharacterPair blockComment, ITextEditor editor) throws BadLocationException {
		int openOffset = existingBlock.getOffset();
		int openLength = blockComment.getKey().length();
		int closeOffset = existingBlock.getOffset() + existingBlock.getLength();
		int closeLength = blockComment.getValue().length();
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
		ITextSelection newSelection = new TextSelection(selection.getOffset() - offsetFix,
				selection.getLength() - lengthFix);
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}

	private void addBlockComment(IDocument document, ITextSelection selection, CharacterPair blockComment,
			ITextEditor editor) throws BadLocationException {
		document.replace(selection.getOffset(), 0, blockComment.getKey());
		document.replace(selection.getOffset() + selection.getLength() + blockComment.getKey().length(), 0,
				blockComment.getValue());
		ITextSelection newSelection = new TextSelection(selection.getOffset() + blockComment.getKey().length(),
				selection.getLength());
		editor.selectAndReveal(newSelection.getOffset(), newSelection.getLength());
	}
}