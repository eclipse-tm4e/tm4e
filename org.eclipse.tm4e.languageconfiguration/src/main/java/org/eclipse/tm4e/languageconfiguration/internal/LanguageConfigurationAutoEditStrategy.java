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

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TabSpacesInfo;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * {@link IAutoEditStrategy} which uses VSCode language-configuration.json.
 */
public class LanguageConfigurationAutoEditStrategy implements IAutoEditStrategy {

	@Nullable
	private IDocument document;

	private IContentType @Nullable [] contentTypes;

	@Nullable
	private TabSpacesInfo tabSpacesInfo;

	@Nullable
	private ITextViewer viewer;

	@Override
	public void customizeDocumentCommand(@Nullable final IDocument document, @Nullable final DocumentCommand command) {
		if (document == null || command == null)
			return;

		final IContentType[] contentTypes = findContentTypes(document);
		if (contentTypes == null || command.text.isEmpty()) {
			return;
		}
		installViewer();

		if (TextUtils.isEnter(document, command)) {
			// key enter pressed
			onEnter(document, command);
			return;
		}

		// Auto close pair
		final var registry = LanguageConfigurationRegistryManager.getInstance();
		for (final IContentType contentType : contentTypes) {
			final var autoClosingPair = registry.getAutoClosingPair(document.get(), command.offset,
					command.text, contentType);
			if (autoClosingPair == null) {
				continue;
			}
			command.caretOffset = command.offset + command.text.length();
			command.shiftsCaret = false;
			if (command.text.equals(autoClosingPair.open)
					&& isFollowedBy(document, command.offset, autoClosingPair.open)) {
				command.text = "";
			} else if (command.text.equals(autoClosingPair.close)
					&& isFollowedBy(document, command.offset, autoClosingPair.close)) {
				command.text = "";
			} else if (isAutoClosingAllowed(document, contentType, command.offset, autoClosingPair)) {
				command.text += autoClosingPair.close;
			}
			return;
		}

		Arrays.stream(contentTypes)
				.flatMap(contentType -> registry.getEnabledAutoClosingPairs(contentType).stream())
				.map(cp -> cp.close)
				.filter(command.text::equals)
				.filter(closing -> isFollowedBy(document, command.offset, closing))
				.findFirst()
				.ifPresent(closing -> {
					command.caretOffset = command.offset + command.text.length();
					command.shiftsCaret = false;
					command.text = "";
				});

	}

	/**
	 * @return true if auto closing is enabled for the given {@link AutoClosingPairConditional} at the given
	 *         offset
	 */
	private boolean isAutoClosingAllowed(final IDocument document, final IContentType contentType, final int offset,
			final AutoClosingPairConditional pair) {

		// only consider auto-closing if the next char is configured in autoCloseBefore
		try {
			final var ch = document.getChar(offset);
			if (!Character.isWhitespace(ch)) {
				final var registry = LanguageConfigurationRegistryManager.getInstance();
				if (registry.getAutoCloseBefore(contentType).indexOf(ch) < 0)
					return false;
			}
		} catch (final Exception ex) {
			// ignore
		}

		if (!pair.notIn.isEmpty()) {
			final var docModel = TMModelManager.INSTANCE.connect(document);
			try {
				final var lineIndex = document.getLineOfOffset(offset);
				final var tokens = docModel.getLineTokens(lineIndex);
				if (tokens != null) {
					final var lineCharOffset = offset - document.getLineOffset(lineIndex) - 1;
					TMToken tokenAtOffset = null;
					for (final var token : tokens) {
						if (token.startIndex > lineCharOffset)
							break;
						tokenAtOffset = token;
					}
					if (tokenAtOffset != null) {
						for (final var notIn : pair.notIn) {
							if (tokenAtOffset.type.contains(notIn)) {
								return false;
							}
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
	 * @param document the document
	 * @param offset the offset
	 * @param value the content value to check
	 *
	 * @return <code>true</code> if the content after the given offset is followed
	 *         by the given <code>value</code> and false otherwise.
	 */
	private static boolean isFollowedBy(final IDocument document, int offset, final String value) {
		for (int i = 0; i < value.length(); i++) {
			if (document.getLength() <= offset) {
				return false;
			}
			try {
				if (document.getChar(offset) != value.charAt(i)) {
					return false;
				}
			} catch (final BadLocationException e) {
				return false;
			}
			offset++;
		}
		return true;
	}

	private void onEnter(final IDocument document, final DocumentCommand command) {
		if (contentTypes != null) {
			final var registry = LanguageConfigurationRegistryManager.getInstance();
			for (final IContentType contentType : contentTypes) {
				if (!registry.shouldEnterAction(document, command.offset, contentType)) {
					continue;
				}
				// https://github.com/microsoft/vscode/blob/bf63ea1932dd253745f38a4cbe26bb9be01801b1/src/vs/editor/common/cursor/cursorTypeOperations.ts#L309
				final var enterAction = registry.getEnterAction(document, command.offset, contentType);
				if (enterAction != null) {
					final String delim = command.text;
					switch (enterAction.indentAction) {
						case None: {
							// Nothing special
							final String increasedIndent = normalizeIndentation(enterAction.indentation + enterAction.appendText);
							final String typeText = delim + increasedIndent;

							command.text = typeText;
							command.shiftsCaret = false;
							command.caretOffset = command.offset + (delim + increasedIndent).length();
							break;
						}
						case Indent: {
							// Indent once
							final String increasedIndent = normalizeIndentation(enterAction.indentation + enterAction.appendText);
							final String typeText = delim + increasedIndent;

							command.text = typeText;
							command.shiftsCaret = false;
							command.caretOffset = command.offset + (delim + increasedIndent).length();
							break;
						}
						case IndentOutdent: {
							// Ultra special
							final String normalIndent = normalizeIndentation(enterAction.indentation);
							final String increasedIndent = normalizeIndentation(enterAction.indentation + enterAction.appendText);
							final String typeText = delim + increasedIndent + delim + normalIndent;

							command.text = typeText;
							command.shiftsCaret = false;
							command.caretOffset = command.offset + (delim + increasedIndent).length();
							break;
						}
						case Outdent:
							final String indentation = TextUtils.getIndentationFromWhitespace(enterAction.indentation, getTabSpaces());
							final String outdentedText = outdentString(normalizeIndentation(indentation + enterAction.appendText));

							command.text = delim + outdentedText;
							command.shiftsCaret = false;
							command.caretOffset = command.offset + (delim + outdentedText).length();
							break;
					}
					return;
				}
			}
		}

		// fail back to default for indentation
		new DefaultIndentLineAutoEditStrategy().customizeDocumentCommand(document, command);
	}

	private IContentType @Nullable [] findContentTypes(final IDocument document) {
		if (this.document != null && this.document.equals(document)) {
			return contentTypes;
		}
		try {
			final ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
			this.contentTypes = info == null ? null : info.getContentTypes();
			this.document = document;
		} catch (final CoreException e) {
			e.printStackTrace();
		}
		return contentTypes;
	}

	private String outdentString(final String str) {
		if (str.startsWith("\t")) {//$NON-NLS-1$
			return str.substring(1);
		}
		final TabSpacesInfo tabSpaces = getTabSpaces();
		if (tabSpaces.isInsertSpaces()) {
			final var chars = new char[tabSpaces.getTabSize()];
			Arrays.fill(chars, ' ');
			final var spaces = new String(chars);
			if (str.startsWith(spaces)) {
				return str.substring(spaces.length());
			}
		}
		return str;
	}

	private String normalizeIndentation(final String str) {
		final TabSpacesInfo tabSpaces = getTabSpaces();
		return TextUtils.normalizeIndentation(str, tabSpaces.getTabSize(), tabSpaces.isInsertSpaces());
	}

	private TabSpacesInfo getTabSpaces() {
		// For performance reason, tab spaces info are cached.
		// If user change preferences (tab size, insert spaces), he must close the editor
		// FIXME : how to detect changes of (tab size, insert spaces) with a generic mean?
		if (tabSpacesInfo != null) {
			return tabSpacesInfo;
		}
		tabSpacesInfo = TextUtils.getTabSpaces(viewer);
		return tabSpacesInfo;
	}

	private void installViewer() {
		if (viewer == null) {
			final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			final IEditorPart editorPart = page.getActiveEditor();
			viewer = editorPart.getAdapter(ITextViewer.class);
		}
	}
}
