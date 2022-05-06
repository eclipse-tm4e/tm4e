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
package org.eclipse.tm4e.languageconfiguration;

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
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TabSpacesInfo;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * {@link IAutoEditStrategy} which uses VSCode language-configuration.json.
 *
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
		final LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
		for (final IContentType contentType : contentTypes) {
			final CharacterPair autoClosingPair = registry.getAutoClosePair(document.get(), command.offset,
					command.text,
					contentType);
			if (autoClosingPair == null) {
				continue;
			}
			command.caretOffset = command.offset + command.text.length();
			command.shiftsCaret = false;
			if (command.text.equals(autoClosingPair.getKey())
					&& isFollowedBy(document, command.offset, autoClosingPair.getKey())) {
				command.text = "";
			} else if (command.text.equals(autoClosingPair.getValue())
					&& isFollowedBy(document, command.offset, autoClosingPair.getValue())) {
				command.text = "";
			} else {
				command.text += autoClosingPair.getValue();
			}
			return;
		}

		Arrays.stream(contentTypes)
				.flatMap(contentType -> registry.getEnabledAutoClosingPairs(contentType).stream())
				.map(CharacterPair::getValue)
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
		final LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
		if (contentTypes != null) {
			for (final IContentType contentType : contentTypes) {
				if (!registry.shouldEnterAction(document, command.offset, contentType)) {
					continue;
				}
				final var enterIndent = registry.getEnterAction(document, command.offset, contentType);
				if (enterIndent != null) {
					final EnterAction enterAction = enterIndent.getEnterAction();
					final String indentation = TextUtils.getIndentationFromWhitespace(enterIndent.getIndentation(),
							getTabSpaces());
					final String delim = command.text;
					switch (enterAction.getIndentAction()) {
					case None: {
						// Nothing special
						final String increasedIndent = normalizeIndentation(indentation + enterAction.getAppendText());
						final String typeText = delim + increasedIndent;

						command.text = typeText;
						command.shiftsCaret = false;
						command.caretOffset = command.offset + (delim + increasedIndent).length();
						break;
					}
					case Indent: {
						// Indent once
						final String increasedIndent = normalizeIndentation(indentation + enterAction.getAppendText());
						final String typeText = delim + increasedIndent;

						command.text = typeText;
						command.shiftsCaret = false;
						command.caretOffset = command.offset + (delim + increasedIndent).length();
						break;
					}
					case IndentOutdent: {
						// Ultra special
						final String normalIndent = normalizeIndentation(indentation);
						final String increasedIndent = normalizeIndentation(indentation + enterAction.getAppendText());
						final String typeText = delim + increasedIndent + delim + normalIndent;

						command.text = typeText;
						command.shiftsCaret = false;
						command.caretOffset = command.offset + (delim + increasedIndent).length();
						break;
					}
					case Outdent:
						final String outdentedText = outdentString(
								normalizeIndentation(indentation + enterAction.getAppendText()));

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
			final char[] chars = new char[tabSpaces.getTabSize()];
			Arrays.fill(chars, ' ');
			final String spaces = new String(chars);
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
