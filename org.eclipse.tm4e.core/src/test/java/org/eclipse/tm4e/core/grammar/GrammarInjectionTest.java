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
package org.eclipse.tm4e.core.grammar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for grammar tokenizer.
 *
 */
public class GrammarInjectionTest {

	private static final String[] EXPECTED_TOKENS = {
			"Token from 0 to 1 with scopes [source.ts, meta.decorator.ts, punctuation.decorator.ts]",
			"Token from 1 to 10 with scopes [source.ts, meta.decorator.ts, entity.name.function.ts]",
			"Token from 10 to 11 with scopes [source.ts, meta.decorator.ts, meta.brace.round.ts]",
			"Token from 11 to 12 with scopes [source.ts, meta.decorator.ts, meta.object-literal.ts, punctuation.definition.block.ts]",
			"Token from 12 to 20 with scopes [source.ts, meta.decorator.ts, meta.object-literal.ts, meta.object.member.ts, meta.object-literal.key.ts]",
			"Token from 20 to 21 with scopes [source.ts, meta.decorator.ts, meta.object-literal.ts, meta.object.member.ts, meta.object-literal.key.ts, punctuation.separator.key-value.ts]",
			"Token from 21 to 22 with scopes [source.ts, meta.decorator.ts, meta.object-literal.ts, meta.object.member.ts, string.template.ts, punctuation.definition.string.template.begin.ts]",
			"Token from 22 to 38 with scopes [source.ts, meta.decorator.ts, meta.object-literal.ts, meta.object.member.ts, string.template.ts]",
			"Token from 38 to 39 with scopes [source.ts, meta.decorator.ts, meta.object-literal.ts, meta.object.member.ts, string.template.ts, punctuation.definition.string.template.end.ts]",
			"Token from 39 to 40 with scopes [source.ts, meta.decorator.ts, meta.object-literal.ts, punctuation.definition.block.ts]",
			"Token from 40 to 41 with scopes [source.ts, meta.decorator.ts, meta.brace.round.ts]" };

	@Test
	public void angular2TokenizeLine() throws Exception {
		Registry registry = new Registry(new IRegistryOptions() {

			@Override
			public InputStream getInputStream(String scopeName) throws IOException {
				return Data.class.getResourceAsStream(getFilePath(scopeName));
			}

			@Override
			public Collection<String> getInjections(String scopeName) {
				return Arrays.asList("template.ng", "styles.ng");
			}

			@Override
			public String getFilePath(String scopeName) {
				if (scopeName != null) {
					switch (scopeName) {
					case "source.js":
						return "JavaScript.tmLanguage.json";
					case "text.html.basic":
						return "html.json";
					case "source.ts":
						return "TypeScript.tmLanguage.json";
					case "template.ng":
						return "template.ng.json";
					case "styles.ng":
						return "styles.ng.json";
					}
				}
				return null;
			}
		});
		IGrammar grammar = registry.loadGrammar("source.ts");
		ITokenizeLineResult lineTokens = grammar.tokenizeLine("@Component({template:`<a href='' ></a>`})");
		for (int i = 0; i < lineTokens.getTokens().length; i++) {
			IToken token = lineTokens.getTokens()[i];
			String s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
					+ token.getScopes();
			System.err.println(s);
			Assertions.assertEquals(EXPECTED_TOKENS[i], s);
		}
	}
}
