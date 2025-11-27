/*******************************************************************************
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Sebastian Thomschke (Vegard IT) - moved to separate utility class
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.utils;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;

public final class GrammarUtils {

	/**
	 * Finds a grammar for the given document.
	 */
	public static @Nullable IGrammar findGrammar(final IDocument doc) {
		final ContentTypeInfo info = ContentTypeHelper.findContentTypes(doc);
		if (info == null)
			return null;

		final IContentType[] contentTypes = info.getContentTypes();
		final var registry = TMEclipseRegistryPlugin.getGrammarRegistryManager();
		// try to determine the grammar based on the content types
		IGrammar grammar = registry.getGrammarFor(contentTypes);
		if (grammar == null) {
			// try to determine the grammar based on the file type
			final String fileName = info.getFileName();
			if (fileName.indexOf('.') > -1) {
				final String fileExtension = new Path(fileName).getFileExtension();
				if (fileExtension != null) {
					grammar = registry.getGrammarForFileExtension(fileExtension);
				}
			}
		}
		return grammar;
	}

	private GrammarUtils() {
	}
}
