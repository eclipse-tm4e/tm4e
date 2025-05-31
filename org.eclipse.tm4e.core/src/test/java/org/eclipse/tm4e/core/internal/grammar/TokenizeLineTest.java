/**
 * Copyright (c) 2022,2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 * Alexander Shapayev - add testTokenizeMultiByteLine2
 */
package org.eclipse.tm4e.core.internal.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.grammar.tokenattrs.EncodedTokenAttributes;
import org.eclipse.tm4e.core.internal.grammar.tokenattrs.OptionalStandardTokenType;
import org.eclipse.tm4e.core.registry.IGrammarConfiguration;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

class TokenizeLineTest {

	@Test
	void testTokenizeLine() throws Exception {
		final var grammar = new Registry().addGrammar(IGrammarSource.fromResource(Data.class, "JavaScript.tmLanguage"));

		final var expected = """
			Token from 0 to 8 with scopes [source.js, meta.function.js, storage.type.function.js]
			Token from 8 to 9 with scopes [source.js, meta.function.js]
			Token from 9 to 12 with scopes [source.js, meta.function.js, entity.name.function.js]
			Token from 12 to 13 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, meta.brace.round.js]
			Token from 13 to 14 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, parameter.name.js, variable.parameter.js]
			Token from 14 to 15 with scopes [source.js, meta.function.js, meta.function.type.parameter.js]
			Token from 15 to 16 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, parameter.name.js, variable.parameter.js]
			Token from 16 to 17 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, meta.brace.round.js]
			Token from 17 to 18 with scopes [source.js, meta.function.js]
			Token from 18 to 19 with scopes [source.js, meta.function.js, meta.decl.block.js, meta.brace.curly.js]
			Token from 19 to 20 with scopes [source.js, meta.function.js, meta.decl.block.js]
			Token from 20 to 26 with scopes [source.js, meta.function.js, meta.decl.block.js, keyword.control.js]
			Token from 26 to 28 with scopes [source.js, meta.function.js, meta.decl.block.js]
			Token from 28 to 29 with scopes [source.js, meta.function.js, meta.decl.block.js, keyword.operator.arithmetic.js]
			Token from 29 to 32 with scopes [source.js, meta.function.js, meta.decl.block.js]
			Token from 32 to 33 with scopes [source.js, meta.function.js, meta.decl.block.js, meta.brace.curly.js]
			"""
				.split("\n");

		final var lineText = "function add(a,b) { return a+b; }";
		final var lineTokens = grammar.tokenizeLine(lineText);
		for (int i = 0; i < lineTokens.getTokens().length; i++) {
			final var token = lineTokens.getTokens()[i];
			assertThat(token.getStartIndex()).isGreaterThanOrEqualTo(0);
			assertThat(token.getStartIndex()).isLessThanOrEqualTo(lineText.length());
			assertThat(token.getEndIndex()).isGreaterThanOrEqualTo(0);
			assertThat(token.getEndIndex()).isLessThanOrEqualTo(lineText.length());
			assertThat("Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes " + token.getScopes())
					.isEqualTo(expected[i]);
		}
	}

	@Test
	void testTokenizeLine2() throws Exception {
		final var jsScope = "source.js";

		final var languageMap = new HashMap<String, Integer>();
		languageMap.put(jsScope, 1);

		final var tokenMap = new HashMap<String, Integer>();
		tokenMap.put("comment.block.js", OptionalStandardTokenType.Comment);
		tokenMap.put("string.single.js", OptionalStandardTokenType.String);

		final var registry = new Registry(new IRegistryOptions() {
			@Override
			public @Nullable IGrammarSource getGrammarSource(final String scopeName) {
				return switch (scopeName) {
					case jsScope -> IGrammarSource.fromResource(Data.class, "JavaScript.tmLanguage.json");
					default -> null;
				};
			}
		});

		final var grammar = castNonNull(registry.loadGrammarWithConfiguration(jsScope, castNonNull(languageMap.get(jsScope)),
				new IGrammarConfiguration() {

					@Override
					public @Nullable Map<String, Integer> getTokenTypes() {
						return tokenMap;
					}
				}));

		final var lineText = "console.log('hi'); /*comment*/}";
		final var lineTokens2 = grammar.tokenizeLine2(lineText);
		final int[] encodedTokens = lineTokens2.getTokens();

		final var lineTokens = new ArrayList<String>();
		for (int i = 0; i < encodedTokens.length; i += 2) {
			final int meta = encodedTokens[i + 1];

			final int languageId = EncodedTokenAttributes.getLanguageId(meta);
			assertThat(languageId).isEqualTo(languageMap.get(jsScope));

			final int start = encodedTokens[i];
			final int end = i + 2 < encodedTokens.length ? encodedTokens[i + 2] : lineText.length();
			final int tokenType = EncodedTokenAttributes.getTokenType(meta);
			lineTokens.add("Token [start=" + start + ", end=" + end + ", tokenType=" + tokenType + "]");
		}

		assertThat(lineTokens).containsExactly(
				"Token [start=0, end=12, tokenType=" + OptionalStandardTokenType.Other + "]",
				"Token [start=12, end=16, tokenType=" + OptionalStandardTokenType.String + "]",
				"Token [start=16, end=19, tokenType=" + OptionalStandardTokenType.Other + "]",
				"Token [start=19, end=30, tokenType=" + OptionalStandardTokenType.Comment + "]",
				"Token [start=30, end=31, tokenType=" + OptionalStandardTokenType.Other + "]");
	}

	@Test
	void testTokenizeMultiByteLine() throws Exception {
		final var grammar = new Registry().addGrammar(IGrammarSource.fromResource(Data.class, "c.tmLanguage.json"));

		final var expected = """
			Token from 0 to 4 with scopes [source.c, storage.type.built-in.primitive.c]
			Token from 4 to 8 with scopes [source.c]
			Token from 8 to 10 with scopes [source.c, storage.modifier.array.bracket.square.c]
			Token from 10 to 11 with scopes [source.c]
			Token from 11 to 12 with scopes [source.c, keyword.operator.assignment.c]
			Token from 12 to 13 with scopes [source.c]
			Token from 13 to 14 with scopes [source.c, meta.block.c, punctuation.section.block.begin.bracket.curly.c]
			Token from 14 to 15 with scopes [source.c, meta.block.c, string.quoted.double.c, punctuation.definition.string.begin.c]
			Token from 15 to 20 with scopes [source.c, meta.block.c, string.quoted.double.c]
			Token from 20 to 21 with scopes [source.c, meta.block.c, string.quoted.double.c, punctuation.definition.string.end.c]
			Token from 21 to 22 with scopes [source.c, meta.block.c, punctuation.section.block.end.bracket.curly.c]
			Token from 22 to 23 with scopes [source.c, punctuation.terminator.statement.c]
			Token from 23 to 24 with scopes [source.c]
			Token from 24 to 28 with scopes [source.c, storage.type.built-in.primitive.c]
			Token from 28 to 35 with scopes [source.c]
			Token from 35 to 36 with scopes [source.c, keyword.operator.assignment.c]
			Token from 36 to 37 with scopes [source.c]
			Token from 37 to 38 with scopes [source.c, keyword.operator.c]
			Token from 38 to 39 with scopes [source.c, constant.numeric.decimal.c]
			Token from 39 to 40 with scopes [source.c, punctuation.terminator.statement.c]
			""".split("\n");

		final var lineText = "char cat[] = {\"кошка\"}; char mouse = -1;\n";
		final var lineTokens = grammar.tokenizeLine(lineText);
		for (int i = 0; i < lineTokens.getTokens().length; i++) {
			final var token = lineTokens.getTokens()[i];

			assertThat(token.getStartIndex()).isGreaterThanOrEqualTo(0);
			assertThat(token.getStartIndex()).isLessThanOrEqualTo(lineText.length());
			assertThat(token.getEndIndex()).isGreaterThanOrEqualTo(0);
			assertThat(token.getEndIndex()).isLessThanOrEqualTo(lineText.length());
			assertThat("Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes " + token.getScopes())
					.isEqualTo(expected[i]);
		}
	}
}
