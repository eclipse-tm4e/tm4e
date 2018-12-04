/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterActionAndIndent;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TabSpacesInfo;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.utils.ContentTypeInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * {@link IAutoEditStrategy} which uses VSCode language-configuration.json.
 *
 */
public class LanguageConfigurationAutoEditStrategy implements IAutoEditStrategy {

	private IDocument document;
	private IContentType[] contentTypes;

	private TabSpacesInfo tabSpacesInfo;
	private ITextViewer viewer;

	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		IContentType[] contentTypes = findContentTypes(document);
		if (contentTypes == null || command.text.isEmpty()) {
			return;
		}
		installViewer();

		if (TextUtils.isEnter(document, command)) {
			// key enter pressed
			onEnter(document, command, false);
			return;
		}

		// Auto close pair
		LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
		for (IContentType contentType : contentTypes) {
			CharacterPair autoClosingPair = registry.getAutoClosePair(document.get(), command.offset, command.text,
					contentType);
			if (autoClosingPair == null) {
				continue;
			}
			command.caretOffset = command.offset + 1;
			command.shiftsCaret = false;
			if (isFollowedBy(document, command.offset, autoClosingPair.getValue())) {
				command.text = "";
			} else {
				command.text += autoClosingPair.getValue();
			}
			break;
		}

		// ITMModel model = TMUIPlugin.getTMModelManager().connect(document);
		// try {
		// int lineNumber = document.getLineOfOffset(command.offset);
		// model.forceTokenization(lineNumber);
		// List<TMToken> tokens = model.getLineTokens(lineNumber);
		// System.err.println(tokens.size());
		// } catch (BadLocationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// if (registry.shouldAutoClosePair(command.text, ".java")) {
		// List<IAutoClosingPair> autoClosingPairs =
		// registry.getAutoClosingPairs(".java");
		// for (IAutoClosingPair autoClosingPair : autoClosingPairs) {
		// if (command.text.equals(autoClosingPair.getOpen())) {
		// command.text += autoClosingPair.getClose();
		// command.caretOffset = command.offset + 1;
		// command.shiftsCaret = false;
		// break;
		// }
		// }
		// }
	}

	/**
	 * Returns <code>true</code> if the content after the given offset is followed
	 * by the given <code>value</code> and false otherwise.
	 * 
	 * @param document the document
	 * @param offset   the offset
	 * @param value    the content value to check
	 * @return <code>true</code> if the content after the given offset is followed
	 *         by the given <code>value</code> and false otherwise.
	 */
	private static boolean isFollowedBy(IDocument document, int offset, String value) {
		for (int i = 0; i < value.length(); i++) {
			if (document.getLength() <= offset) {
				return false;
			}
			try {
				if (document.getChar(offset) != value.charAt(i)) {
					return false;
				}
			} catch (BadLocationException e) {
				return false;
			}
			offset++;
		}
		return true;
	}

	private void onEnter(IDocument document, DocumentCommand command, boolean keepPosition) {
		LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
		for (IContentType contentType : contentTypes) {
			if (!registry.shouldEnterAction(document, command.offset, contentType)) {
				continue;
			}
			EnterActionAndIndent r = registry.getEnterAction(document, command.offset, contentType);
			if (r != null) {
				EnterAction enterAction = r.getEnterAction();
				String indentation = TextUtils.getIndentationFromWhitespace(r.getIndentation(), getTabSpaces());
				String delim = command.text;
				switch (enterAction.getIndentAction()) {
				case None: {
					// Nothing special
					String increasedIndent = normalizeIndentation(indentation + enterAction.getAppendText());
					String typeText = delim + increasedIndent;

					command.text = typeText;
					command.shiftsCaret = false;

					if (keepPosition) {

					} else {
						command.caretOffset = command.offset + (delim + increasedIndent).length();
					}
					break;
				}
				case Indent: {
					// Indent once
					String increasedIndent = normalizeIndentation(indentation + enterAction.getAppendText());
					String typeText = delim + increasedIndent;

					command.text = typeText;
					command.shiftsCaret = false;

					if (keepPosition) {

					} else {
						command.caretOffset = command.offset + (delim + increasedIndent).length();
					}
					break;
				}
				case IndentOutdent: {
					// Ultra special
					String normalIndent = normalizeIndentation(indentation);
					String increasedIndent = normalizeIndentation(indentation + enterAction.getAppendText());

					String typeText = delim + increasedIndent + delim + normalIndent;
					command.text = typeText;
					command.shiftsCaret = false;

					if (keepPosition) {

					} else {
						command.caretOffset = command.offset + (delim + increasedIndent).length();
					}
					break;
				}
				case Outdent:
					String outdentedText = outdentString(
							normalizeIndentation(indentation + enterAction.getAppendText()));
					command.text = delim + outdentedText;
					command.shiftsCaret = false;
					if (keepPosition) {

					} else {
						command.caretOffset = command.offset + (delim + outdentedText).length();
					}
					break;
				}
				return;
			}
		}

		// no enter rules applied, we should check indentation rules then.
		String indentation = TextUtils.getLinePrefixingWhitespaceAtPosition(document, command.offset);
		String increasedIndent = normalizeIndentation(indentation);
		String delim = TextUtilities.getDefaultLineDelimiter(document);
		String typeText = delim + increasedIndent;

		command.text = typeText;
		command.shiftsCaret = false;

		if (keepPosition) {

		} else {
			command.caretOffset = command.offset + (delim + increasedIndent).length();
		}
	}

	private IContentType[] findContentTypes(IDocument document) {
		if (this.document != null && this.document.equals(document)) {
			return contentTypes;
		}
		try {
			ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
			this.contentTypes = info.getContentTypes();
			this.document = document;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return contentTypes;
	}

	private String outdentString(String str) {
		if (str.startsWith("\t")) {//$NON-NLS-1$
			return str.substring(1);
		}
		TabSpacesInfo tabSpaces = getTabSpaces();
		if (tabSpaces.isInsertSpaces()) {
			char[] chars = new char[tabSpaces.getTabSize()];
			Arrays.fill(chars, ' ');
			String spaces = new String(chars);
			if (str.startsWith(spaces)) {
				return str.substring(spaces.length());
			}
		}
		return str;
	}

	private String normalizeIndentation(String str) {
		TabSpacesInfo tabSpaces = getTabSpaces();
		return TextUtils.normalizeIndentation(str, tabSpaces.getTabSize(), tabSpaces.isInsertSpaces());
	}

	private TabSpacesInfo getTabSpaces() {
		// For performance reason, tab spaces info are cached.
		// If user change preferences (tab size, insert spaces), he must close the
		// editor
		// FIXME : how to detect changes of (tab size, insert spaces) with a generic
		// mean?
		if (tabSpacesInfo != null) {
			return tabSpacesInfo;
		}
		tabSpacesInfo = TextUtils.getTabSpaces(viewer);
		return tabSpacesInfo;
	}

	private void installViewer() {
		if (viewer == null) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorPart editorPart = page.getActiveEditor();
			viewer = editorPart.getAdapter(ITextViewer.class);
		}
	}

}
