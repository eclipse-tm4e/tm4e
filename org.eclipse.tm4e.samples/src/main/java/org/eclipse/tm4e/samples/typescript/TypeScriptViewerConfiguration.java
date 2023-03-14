/**
 * Copyright (c) 2015-2018 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.samples.typescript;

import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;

public class TypeScriptViewerConfiguration extends SourceViewerConfiguration {

	@Override
	public IPresentationReconciler getPresentationReconciler(final ISourceViewer viewer) {
		// Defines a TextMate Presentation reconcilier
		final var reconciler = new TMPresentationReconciler();
		// Set the TypeScript grammar
		reconciler.setGrammar(getGrammar());
		// reconciler.setThemeId(ThemeIdConstants.Monokai);
		return reconciler;
	}

	private IGrammar getGrammar() {
		// TODO: cache the grammar
		final var registry = new Registry();
		try {
			return registry.addGrammar(
					IGrammarSource.fromResource(TypeScriptViewerConfiguration.class, "TypeScript.tmLanguage.json"));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
