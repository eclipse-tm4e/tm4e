/**
 * Copyright (c) 2015-2019 Angelo ZERR, and others
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.registry.IGrammarSource.fromString;

import org.eclipse.jface.text.Document;
import org.eclipse.tm4e.core.registry.IGrammarSource.ContentType;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

class TMDocumentModelTest {

	@Test
	void testMultiLineChange() throws InterruptedException {
		final var grammar = new Registry().addGrammar(fromString(ContentType.YAML, "scopeName: dummy"));

		final var document = new Document();
		final var tmModel = new TMDocumentModel(document);

		// setting grammar and listener is required to start background TokenizerThread
		tmModel.setGrammar(grammar);
		tmModel.addModelTokensChangedListener(ev -> {
		});

		Thread.sleep(300);
		assertThat(tmModel.getNumberOfLines()).isEqualTo(1);

		document.set("a\nb\nc\nd");
		Thread.sleep(300);
		assertThat(tmModel.getNumberOfLines()).isEqualTo(4);

		document.set("a\nb");
		Thread.sleep(300);
		assertThat(tmModel.getNumberOfLines()).isEqualTo(2);
	}
}
