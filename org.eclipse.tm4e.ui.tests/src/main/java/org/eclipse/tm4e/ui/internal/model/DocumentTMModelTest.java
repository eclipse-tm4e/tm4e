/**
 * Copyright (c) 2019 Red Hat Inc., and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 */
package org.eclipse.tm4e.ui.internal.model;

import org.eclipse.jface.text.Document;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

class DocumentTMModelTest {

	@Test
	void testMultiLineChange() throws Exception {
		final var document = new Document();
		final var model = new TMDocumentModel(document);
		try {
			model.setGrammar(new Registry().addGrammar(IGrammarSource.fromResource(getClass(), "/grammars/TypeScript.tmLanguage.json")));
			document.set("a\nb\nc\nd");
			model.addModelTokensChangedListener(e -> {
			});
			model.forceTokenization(0);
			document.set("a");
			model.forceTokenization(0);
		} finally {
			model.dispose();
		}
	}
}
