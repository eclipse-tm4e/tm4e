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
package org.eclipse.tm4e.samples.angular2;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;

public class Angular2ViewerConfiguration extends SourceViewerConfiguration {

	@Override
	public IPresentationReconciler getPresentationReconciler(final ISourceViewer viewer) {
		// Defines a TextMate Presentation reconcilier
		final var reconciler = new TMPresentationReconciler();
		// Set the Angular2 grammar
		reconciler.setGrammar(getGrammar());
		return reconciler;
	}

	private IGrammar getGrammar() {
		final var registry = new Registry(new IRegistryOptions() {

			@Override
			public Collection<String> getInjections(final String scopeName) {
				return Arrays.asList("template.ng", "styles.ng", "source.ng.css");
			}

			@Override
			public IGrammarSource getGrammarSource(final String scopeName) {
				final var resourceName = switch (scopeName) {
					// case "source.ng.css" -> "source.ng.css.json";
					// case "source.ng.ts" -> "source.ng.ts.json";
					// case "template.ng" -> "template.ng.json";
					// case "styles.ng" -> "styles.ng.json";
					default -> scopeName + ".json";
				};
				return IGrammarSource.fromResource(Angular2ViewerConfiguration.class, resourceName);
			}
		});
		return registry.loadGrammar("source.ng.ts");
	}

}
