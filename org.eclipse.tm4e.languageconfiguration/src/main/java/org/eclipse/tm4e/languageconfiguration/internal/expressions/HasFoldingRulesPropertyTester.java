/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.expressions;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.folding.FoldingSupport;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public final class HasFoldingRulesPropertyTester extends PropertyTester {

	@Override
	public boolean test(final @Nullable Object receiver, final String property, final Object[] args, final @Nullable Object expectedValue) {
		if (receiver instanceof final IFileEditorInput fileInput)
			return hasFoldingConfiguration(ContentTypeHelper.findContentTypesByFileName(fileInput.getFile().getName()));

		if (receiver instanceof final IPathEditorInput pathInput)
			return hasFoldingConfiguration(ContentTypeHelper.findContentTypesByFileName(castNonNull(pathInput.getPath().lastSegment())));

		if (receiver instanceof final IFile file)
			return hasFoldingConfiguration(ContentTypeHelper.findContentTypesByFileName(file.getName()));

		final var editor = Adapters.adapt(receiver, ITextEditor.class);
		if (editor == null)
			return false;

		final var input = editor.getEditorInput();
		final var docProvider = editor.getDocumentProvider();
		if (docProvider == null || input == null)
			return false;

		final var doc = docProvider.getDocument(input);
		if (doc == null)
			return false;

		final ContentTypeInfo info = ContentTypeHelper.findContentTypes(doc);
		if (info == null)
			return false;

		return hasFoldingConfiguration(info);
	}

	private boolean hasFoldingConfiguration(final ContentTypeInfo info) {
		return FoldingSupport.getFoldingRules(info) != null;
	}

	private boolean hasFoldingConfiguration(final IContentType[] types) {
		return FoldingSupport.getFoldingRules(types) != null;
	}
}
