/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Sebastian Thomschke - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.internal.model.CursorConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.model.IndentForEnter;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.supports.IndentForEnterHelper;
import org.eclipse.tm4e.languageconfiguration.internal.supports.IndentForEnterHelper.IIndentConverter;
import org.eclipse.tm4e.languageconfiguration.internal.supports.IndentRulesSupport;
import org.eclipse.tm4e.languageconfiguration.internal.utils.MockDocument;
import org.junit.jupiter.api.Test;

class IndentationRulesTest {

	private final IIndentConverter indentConv = IIndentConverter.of(new CursorConfiguration(true, 2));

	private final LanguageConfiguration phpLangCfg = castNonNull(LanguageConfiguration.load(new StringReader(
			"""
				{
				  "indentationRules": {
				    "increaseIndentPattern": "({(?!.*}).*|\\\\(|\\\\[|((else(\\\\s)?)?if|else|for(each)?|while|switch|case).*:)\\\\s*((/[/*].*|)?$|\\\\?>)",
				    "decreaseIndentPattern": "^(.*\\\\*\\\\/)?\\\\s*((\\\\})|(\\\\)+[;,])|(\\\\]\\\\)*[;,])|\\\\b(else:)|\\\\b((end(if|for(each)?|while|switch));))"
				  },
				}""")));

	private final IndentRulesSupport indentRulesSupport = new IndentRulesSupport(castNonNull(phpLangCfg.getIndentationRules()));

	@Test
	void testGetIdentForEnter() {
		MockDocument doc;

		doc = new MockDocument("org.eclipse.tm4e.language_pack.php", """
			class Foo {
			}
			""");
		assertResult(getIndentForEnter(doc, doc.getOffsetAfter("Foo {")),
				"", "\t", "  ");
		assertResult(getIndentForEnter(doc, doc.getOffsetAfter("Foo ")),
				"", "", "");
		assertResult(getIndentForEnter(doc, doc.getOffsetOf("}")),
				"", "", "");

		doc = new MockDocument("org.eclipse.tm4e.language_pack.php", """
			class Foo {
			  public function bar() {
			  }
			}
			""");
		assertResult(getIndentForEnter(doc, doc.getOffsetAfter("Foo {")),
				"", "\t", "  ");
		assertResult(getIndentForEnter(doc, doc.getOffsetAfter("bar() {")),
				"  ", "  \t", "    ");
		assertResult(getIndentForEnter(doc, doc.getOffsetAfter("bar() ")),
				"  ", "  ", "  ");
		assertResult(getIndentForEnter(doc, doc.getOffsetOf("}")),
				"  ", "\t", "  ");

	}

	private void assertResult(final @Nullable IndentForEnter result, final String beforeEnter, final String AfterEnter,
			final String indent) {
		assertNotNull(result);
		assertEquals(beforeEnter, result.beforeEnter);
		assertEquals(AfterEnter, result.afterEnter);
		assertEquals(indent, indentConv.normalizeIndentation(AfterEnter));
	}

	private @Nullable IndentForEnter getIndentForEnter(final IDocument doc, final int offset) {
		return IndentForEnterHelper.getIndentForEnter(doc, offset, indentConv, indentRulesSupport);
	}
}
