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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for grammar tokenizer.
 */
public class GrammarTest {

	private static final String[] EXPECTED_TOKENS = {
		"Token from 0 to 8 with scopes [source.js, meta.function.js, storage.type.function.js]",
		"Token from 8 to 9 with scopes [source.js, meta.function.js]",
		"Token from 9 to 12 with scopes [source.js, meta.function.js, entity.name.function.js]",
		"Token from 12 to 13 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, meta.brace.round.js]",
		"Token from 13 to 14 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, parameter.name.js, variable.parameter.js]",
		"Token from 14 to 15 with scopes [source.js, meta.function.js, meta.function.type.parameter.js]",
		"Token from 15 to 16 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, parameter.name.js, variable.parameter.js]",
		"Token from 16 to 17 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, meta.brace.round.js]",
		"Token from 17 to 18 with scopes [source.js, meta.function.js]",
		"Token from 18 to 19 with scopes [source.js, meta.function.js, meta.decl.block.js, meta.brace.curly.js]",
		"Token from 19 to 20 with scopes [source.js, meta.function.js, meta.decl.block.js]",
		"Token from 20 to 26 with scopes [source.js, meta.function.js, meta.decl.block.js, keyword.control.js]",
		"Token from 26 to 28 with scopes [source.js, meta.function.js, meta.decl.block.js]",
		"Token from 28 to 29 with scopes [source.js, meta.function.js, meta.decl.block.js, keyword.operator.arithmetic.js]",
		"Token from 29 to 32 with scopes [source.js, meta.function.js, meta.decl.block.js]",
		"Token from 32 to 33 with scopes [source.js, meta.function.js, meta.decl.block.js, meta.brace.curly.js]" };

	private static final String[] EXPECTED_MULTI_LINE_TOKENS = {
		"Token from 0 to 8 with scopes [source.js, meta.function.js, storage.type.function.js]",
		"Token from 8 to 9 with scopes [source.js, meta.function.js]",
		"Token from 9 to 12 with scopes [source.js, meta.function.js, entity.name.function.js]",
		"Token from 12 to 13 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, meta.brace.round.js]",
		"Token from 13 to 14 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, parameter.name.js, variable.parameter.js]",
		"Token from 14 to 15 with scopes [source.js, meta.function.js, meta.function.type.parameter.js]",
		"Token from 15 to 16 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, parameter.name.js, variable.parameter.js]",
		"Token from 16 to 17 with scopes [source.js, meta.function.js, meta.function.type.parameter.js, meta.brace.round.js]",
		"Token from 0 to 1 with scopes [source.js, meta.function.js, meta.decl.block.js, meta.brace.curly.js]",
		"Token from 1 to 2 with scopes [source.js, meta.function.js, meta.decl.block.js]",
		"Token from 2 to 8 with scopes [source.js, meta.function.js, meta.decl.block.js, keyword.control.js]",
		"Token from 8 to 10 with scopes [source.js, meta.function.js, meta.decl.block.js]",
		"Token from 10 to 11 with scopes [source.js, meta.function.js, meta.decl.block.js, keyword.operator.arithmetic.js]",
		"Token from 11 to 14 with scopes [source.js, meta.function.js, meta.decl.block.js]",
		"Token from 14 to 15 with scopes [source.js, meta.function.js, meta.decl.block.js, meta.brace.curly.js]" };

	@Test
	public void tokenizeSingleLineExpression() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(IGrammarSource.fromResource(Data.class, "JavaScript.tmLanguage"));
		final ITokenizeLineResult lineTokens = castNonNull(grammar).tokenizeLine("function add(a,b) { return a+b; }");
		for (int i = 0; i < lineTokens.getTokens().length; i++) {
			final IToken token = lineTokens.getTokens()[i];
			final String s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
				+ token.getScopes();
			Assertions.assertEquals(EXPECTED_TOKENS[i], s);
		}
	}

	@Test
	public void tokenizeMultilineExpression() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(IGrammarSource.fromResource(Data.class, "JavaScript.tmLanguage"));

		IStateStack ruleStack = null;
		int i = 0;
		int j = 0;
		final String[] lines = { "function add(a,b)", "{ return a+b; }" };
		for (final String line : lines) {
			final ITokenizeLineResult lineTokens = castNonNull(grammar).tokenizeLine(line, ruleStack);
			ruleStack = lineTokens.getRuleStack();
			for (i = 0; i < lineTokens.getTokens().length; i++) {
				final IToken token = lineTokens.getTokens()[i];
				final String s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
					+ token.getScopes();
				Assertions.assertEquals(EXPECTED_MULTI_LINE_TOKENS[i + j], s);
			}
			j = i;
		}
	}

	@Test
	public void tokenizeMultilineYaml() throws Exception {
		final var registry = new Registry();
		final var grammar = registry.addGrammar(IGrammarSource.fromResource(Data.class, "yaml.tmLanguage.json"));
		final var lines = ">\n should.be.string.unquoted.block.yaml\n should.also.be.string.unquoted.block.yaml";
		final var result = TokenizationUtils.tokenizeText(lines, grammar).iterator();
		assertTrue(Arrays.stream(result.next().getTokens()).anyMatch(t -> t.getScopes().contains(
			"keyword.control.flow.block-scalar.folded.yaml")));
		assertTrue(Arrays.stream(result.next().getTokens())
			.anyMatch(t -> t.getScopes().contains("string.unquoted.block.yaml")));
		assertTrue(Arrays.stream(result.next().getTokens())
			.anyMatch(t -> t.getScopes().contains("string.unquoted.block.yaml")));
	}

	@Test
	void tokenizeTypeScriptFile() throws Exception {

		final var grammar = new Registry()
			.addGrammar(IGrammarSource.fromResource(Data.class, "TypeScript.tmLanguage.json"));

		final List<String> expectedTokens;
		try (var resource = Data.class.getResourceAsStream("raytracer_tokens.txt")) {
			expectedTokens = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.toList());
		}

		IStateStack stateStack = null;
		int tokenIndex = -1;
		try (var reader = new BufferedReader(new InputStreamReader(Data.class.getResourceAsStream("raytracer.ts")))) {
			while (reader.ready()) {
				final var lineTokens = grammar.tokenizeLine(reader.readLine(), stateStack);
				stateStack = lineTokens.getRuleStack();
				for (int i = 0; i < lineTokens.getTokens().length; i++) {
					tokenIndex++;
					final var token = lineTokens.getTokens()[i];
					Assertions.assertEquals(
						expectedTokens.get(tokenIndex),
						"Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
							+ token.getScopes());
				}
			}
		}
	}

	// TODO see https://github.com/microsoft/vscode-textmate/issues/173
	@Disabled
	@Test
	void testShadowedRulesAreResolvedCorrectly() {
		final var registry = new Registry();
		final var grammar = registry.addGrammar(new IGrammarSource() {

			@Override
			public Reader getReader() throws IOException {
				return new StringReader("""
					{
						"scopeName": "source.test",
						"repository": {
							"foo": {
								"include": "#bar"
							},
							"bar": {
								"match": "bar1",
								"name": "outer"
							}
						},
						"patterns": [{
								"patterns": [{
									"include": "#foo"
								}],
								"repository": {
									"bar": {
										"match": "bar1",
										"name": "inner"
									}
								}
							},
							{
								"begin": "begin",
								"patterns": [{
									"include": "#foo"
								}],
								"end": "end"
							}
						]
					}
					""");
			}

			@Override
			public String getFilePath() {
				return "test-grammar.json";
			}
		});

		final var result = grammar.tokenizeLine("bar1");
		assertEquals("[{startIndex: 0, endIndex: 4, scopes: [source.test, outer]}]",
			Arrays.toString(result.getTokens()));
	}
}
