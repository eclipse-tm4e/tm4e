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
package org.eclipse.tm4e.languageconfiguration.internal.folding;

import java.util.function.Supplier;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.model.FoldingRules;
import org.eclipse.tm4e.languageconfiguration.internal.model.RegExPattern;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;

public final class FoldingSupport {

	public static @Nullable FoldingRules getFoldingRules(final IDocument doc) {
		final ContentTypeInfo info = ContentTypeHelper.findContentTypes(doc);
		return info == null ? null : getFoldingRules(info);
	}

	public static @Nullable FoldingRules getFoldingRules(final ContentTypeInfo info) {
		return computeFoldingRules(info.getContentTypes(), () -> findGrammar(info));
	}

	public static @Nullable FoldingRules getFoldingRules(final IContentType... types) {
		return computeFoldingRules(types, () -> findGrammar(types));
	}

	private static @Nullable FoldingRules computeFoldingRules(final IContentType[] contentTypes,
			final Supplier<@Nullable IGrammar> grammarProvider) {
		if (contentTypes.length == 0)
			return null;

		/*
		 * 1) try language-configuration folding
		 */
		final var langCfg = LanguageConfigurationRegistryManager.getInstance()
				.getLanguageConfigurationFor(contentTypes);
		if (langCfg != null && langCfg.getFolding() != null)
			return langCfg.getFolding();

		/*
		 * 2) fallback to TextMate grammar folding
		 */
		final IGrammar grammar = grammarProvider.get();
		if (grammar == null)
			return null;
		try {
			final var start = grammar.getFoldingStartMarker();
			if (start == null || start.isBlank())
				return null;
			final var end = grammar.getFoldingEndMarker();
			if (end == null || end.isBlank())
				return null;
			return new FoldingRules(false, RegExPattern.of(start),
					RegExPattern.of(end));
		} catch (final TMException ex) {
			LanguageConfigurationPlugin.logError(ex);
		}
		return null;
	}

	private static @Nullable IGrammar findGrammar(final ContentTypeInfo info) {
		// try to determine the grammar based on the content types
		IGrammar grammar = findGrammar(info.getContentTypes());
		if (grammar == null) {
			// try to determine the grammar based on the file name extension
			final String ext = new Path(info.getFileName()).getFileExtension();
			if (ext != null) {
				grammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarForFileExtension(ext);
			}
		}
		return grammar;
	}

	private static @Nullable IGrammar findGrammar(final IContentType[] types) {
		return TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(types);
	}

	private FoldingSupport() {
	}
}
