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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class HasLanguageConfigurationPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof ITextEditor)) {
			return false;
		}
		ITextEditor editor = (ITextEditor) receiver;

		IEditorInput input = editor.getEditorInput();
		IDocumentProvider docProvider = editor.getDocumentProvider();
		if (docProvider == null || input == null) {
			return false;
		}

		IDocument document = docProvider.getDocument(input);
		if (document == null) {
			return false;
		}

		IContentType[] contentTypes;
		try {
			contentTypes = ContentTypeHelper.findContentTypes(document).getContentTypes();
		} catch (CoreException e) {
			return false;
		}

		LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
		return registry.getLanguageConfigurationFor(contentTypes) != null;
	}

}
