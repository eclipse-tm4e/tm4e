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

import static org.eclipse.tm4e.core.registry.IGrammarSource.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test for grammar tokenizer.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
class GrammarTest {

	private static final String[] EXPECTED_SINGLE_LINE_TOKENS = {
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
	void testTokenizeSingleLineExpression() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(Data.class, "JavaScript.tmLanguage"));
		final var lineTokens = grammar.tokenizeLine("function add(a,b) { return a+b; }");
		assertFalse(lineTokens.isStoppedEarly());
		for (int i = 0; i < lineTokens.getTokens().length; i++) {
			final IToken token = lineTokens.getTokens()[i];
			final String s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
				+ token.getScopes();
			Assertions.assertEquals(EXPECTED_SINGLE_LINE_TOKENS[i], s);
		}
	}

	@Test
	void testTokenizeMultilineExpression() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(Data.class, "JavaScript.tmLanguage"));

		IStateStack ruleStack = null;
		int i = 0;
		int j = 0;
		final String[] lines = { "function add(a,b)", "{ return a+b; }" };
		for (final String line : lines) {
			final var lineTokens = grammar.tokenizeLine(line, ruleStack, null);
			assertFalse(lineTokens.isStoppedEarly());
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
	void testTokenize0Tokens() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(Data.class, "JavaScript.tmLanguage"));
		final String lineText = "";
		final var lineTokens = grammar.tokenizeLine(lineText);
		assertFalse(lineTokens.isStoppedEarly());

		final var endIndexOffset = 1; // IToken's end-indexes are exclusive

		final var tokens = lineTokens.getTokens();
		assertEquals(1, tokens.length);
		assertEquals(0, tokens[0].getStartIndex());
		assertEquals(0 + endIndexOffset, tokens[0].getEndIndex());
	}

	@Test
	void testTokenize1Token() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(Data.class, "JavaScript.tmLanguage"));
		final String lineText = "true";
		final var lineTokens = grammar.tokenizeLine(lineText);
		assertFalse(lineTokens.isStoppedEarly());

		final var endIndexOffset = 1; // IToken's end-indexes are exclusive

		final var tokens = lineTokens.getTokens();
		assertEquals(1, tokens.length);
		assertEquals(0, tokens[0].getStartIndex());
		assertEquals(3 + endIndexOffset, tokens[0].getEndIndex());
	}

	@Test
	void testTokenize1TokenWithNewLine() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(Data.class, "JavaScript.tmLanguage"));
		final String lineText = "true\n";
		final var lineTokens = grammar.tokenizeLine(lineText);
		assertFalse(lineTokens.isStoppedEarly());

		final var endIndexOffset = 1; // IToken's end-indexes are exclusive

		System.out.println(Arrays.toString(lineTokens.getTokens()));
		final var tokens = lineTokens.getTokens();
		assertEquals(1, tokens.length); // TODO why is only 1 token returned? The token for \n is missing
		assertEquals(0, tokens[0].getStartIndex());
		assertEquals(3 + endIndexOffset, tokens[0].getEndIndex());
	}

	@Test
	void testTokenize1IllegalToken() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(Data.class, "JavaScript.tmLanguage"));
		final String lineText = "@"; // Uncaught SyntaxError: illegal character U+0040
		final var lineTokens = grammar.tokenizeLine(lineText);
		assertFalse(lineTokens.isStoppedEarly());
		final var endIndexOffset = 1; // IToken's end-indexes are exclusive

		final var tokens = lineTokens.getTokens();
		assertEquals(1, tokens.length);
		assertEquals(0, tokens[0].getStartIndex());
		assertEquals(0 + endIndexOffset + 1, tokens[0].getEndIndex()); // TODO why does end-index have extra +1 offset?
	}

	@Test
	void testTokenize2Tokens() throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(fromResource(Data.class, "JavaScript.tmLanguage"));

		final String lineText = "{}";
		final var lineTokens = grammar.tokenizeLine(lineText);
		assertFalse(lineTokens.isStoppedEarly());

		final var endIndexOffset = 1; // IToken's end-indexes are exclusive

		final var tokens = lineTokens.getTokens();
		assertEquals(2, tokens.length);
		assertEquals(0, tokens[0].getStartIndex());
		assertEquals(0 + endIndexOffset, tokens[0].getEndIndex());
		assertEquals(1, tokens[1].getStartIndex());
		assertEquals(1 + endIndexOffset, tokens[1].getEndIndex());
	}

	@Test
	void testTokenizeMultilineYaml() throws Exception {
		final var registry = new Registry();
		final var grammar = registry.addGrammar(fromResource(Data.class, "yaml.tmLanguage.json"));
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
	void testTokenizeTypeScriptFile() throws Exception {
		final var grammar = new Registry().addGrammar(fromResource(Data.class, "TypeScript.tmLanguage.json"));

		final List<String> expectedTokens;
		try (var resource = Data.class.getResourceAsStream("raytracer_tokens.txt")) {
			expectedTokens = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
				.lines().toList();
		}

		IStateStack stateStack = null;
		int tokenIndex = -1;
		try (var reader = new BufferedReader(new InputStreamReader(Data.class.getResourceAsStream("raytracer.ts")))) {
			String line;
			while ((line = reader.readLine()) != null) {
				final var lineTokens = grammar.tokenizeLine(line, stateStack, null);
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

	@Test
	void testTokenizeWithTimeout() throws IOException {
		final var grammar = new Registry().addGrammar(fromResource(Data.class, "TypeScript.tmLanguage.json"));

		try (var reader = new BufferedReader(new InputStreamReader(Data.class.getResourceAsStream("raytracer.ts")))) {
			final String veryLongLine = reader.lines().collect(Collectors.joining());
			final var result1 = grammar.tokenizeLine(veryLongLine);
			assertFalse(result1.isStoppedEarly());
			final var lastToken1 = result1.getTokens()[result1.getTokens().length - 1];

			final var result2 = grammar.tokenizeLine(veryLongLine, null, Duration.ofMillis(10));
			assertTrue(result2.isStoppedEarly());
			assertNotEquals(result1.getTokens().length, result2.getTokens().length);
			final var lastToken2 = result2.getTokens()[result2.getTokens().length - 1];
			assertTrue(lastToken2.getEndIndex() < lastToken1.getEndIndex());
		}
	}

	// TODO see https://github.com/microsoft/vscode-textmate/issues/173
	@Disabled
	@Test
	void testShadowedRulesAreResolvedCorrectly() {
		final var registry = new Registry();
		final var grammar = registry.addGrammar(fromString(IGrammarSource.ContentType.JSON, """
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
			"""));

		final var result = grammar.tokenizeLine("bar1");
		assertFalse(result.isStoppedEarly());
		assertEquals("[{startIndex: 0, endIndex: 4, scopes: [source.test, outer]}]",
			Arrays.toString(result.getTokens()));
	}
}
