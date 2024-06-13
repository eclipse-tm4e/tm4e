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
package org.eclipse.tm4e.languageconfiguration.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.ui.texteditor.ITextEditor;

public final class HasLanguageConfigurationPropertyTester extends PropertyTester {

	@Override
	public boolean test(@Nullable final Object receiver, final String property, final Object[] args, @Nullable final Object expectedValue) {

		final var editor = Adapters.adapt(receiver, ITextEditor.class);
		if (editor == null) {
			return false;
		}

		final var input = editor.getEditorInput();
		final var docProvider = editor.getDocumentProvider();
		if (docProvider == null || input == null) {
			return false;
		}

		final var document = docProvider.getDocument(input);
		if (document == null) {
			return false;
		}

		final ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
		if (info == null) {
			return false;
		}

		final var registry = LanguageConfigurationRegistryManager.getInstance();
		return registry.getLanguageConfigurationFor(info.getContentTypes()) != null;
	}
}
