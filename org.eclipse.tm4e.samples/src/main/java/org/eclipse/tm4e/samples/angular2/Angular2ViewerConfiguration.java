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
package org.eclipse.tm4e.samples.angular2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;

public class Angular2ViewerConfiguration extends SourceViewerConfiguration {

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer viewer) {
		// Defines a TextMate Presentation reconcilier
		TMPresentationReconciler reconciler = new TMPresentationReconciler();
		// Set the Angular2 grammar
		reconciler.setGrammar(getGrammar());
		return reconciler;
	}

	private IGrammar getGrammar() {
		Registry registry = new Registry(new IRegistryOptions() {

			@Override
			public InputStream getInputStream(String scopeName) throws IOException {
				return Angular2ViewerConfiguration.class.getResourceAsStream(getFilePath(scopeName));
			}

			@Override
			public Collection<String> getInjections(String scopeName) {
				return Arrays.asList("template.ng", "styles.ng", "source.ng.css");
			}

			@Override
			public String getFilePath(String scopeName) {
//				if ("source.ng.css".equals(scopeName)) {
//					return "source.ng.css.json";
//				} else if ("source.ng.ts".equals(scopeName)) {
//					return "source.ng.ts.json";
//				} else if ("template.ng".equals(scopeName)) {
//					return "template.ng.json";
//				} else if ("styles.ng".equals(scopeName)) {
//					return "styles.ng.json";
//				}
				return scopeName + ".json";
			}
		});
		return registry.loadGrammar("source.ng.ts");
	}

}
