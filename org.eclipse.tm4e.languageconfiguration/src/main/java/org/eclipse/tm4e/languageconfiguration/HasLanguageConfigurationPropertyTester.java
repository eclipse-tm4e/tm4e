/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.utils.ContentTypeInfo;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class HasLanguageConfigurationPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		ITextEditor editor = Adapters.adapt(receiver, ITextEditor.class);
		if (editor == null) {
			return false;
		}

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
			ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
			if(info == null) {
				return false;
			}
			contentTypes = info.getContentTypes();
		} catch (CoreException e) {
			return false;
		}

		LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
		return registry.getLanguageConfigurationFor(contentTypes) != null;
	}

}
