/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.registry.IGrammarSource.fromResource;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.utils.ResourceUtils;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IGrammarSource.ContentType;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

/**
 * Verifies how {@link TMTokenizationSupport} turns grammar tokens into document tokens:
 * adjacent tokens merge only when neither scopes nor grammar origin would be lost, and tokenization honors time limits.
 */
class TMTokenizationTest {

	@Test
	void mergesAdjacentTokensWithIdenticalScopes() {
		final String grammarJson = """
			{"scopeName":"source.test","patterns":[{"match":"a","name":"meta.a"}]}""";
		// Each match yields its own grammar token; the document model keeps a single one
		assertThat(new Registry().addGrammar(IGrammarSource.fromString(ContentType.JSON, grammarJson))
				.tokenizeLine("aa").getTokens()).hasSize(2);
		assertThat(tokenize(ContentType.JSON, grammarJson, "aa")).hasSize(1);
	}

	@Test
	void keepsTokensWhoseScopesDecodeToTheSameType() {
		// The decoded type is the set of dot-separated words of all scopes except the root, so word order is lost
		final var tokens = tokenize(ContentType.JSON, """
			{"scopeName":"source.test","patterns":[
			  {"match":"a","name":"meta.first.second"},
			  {"match":"b","name":"meta.second.first"}
			]}""", "ab");
		assertThat(tokens).hasSize(2);
		assertThat(tokens.get(0).type).isEqualTo(tokens.get(1).type);
		assertThat(tokens.get(0).scopes).containsExactly("source.test", "meta.first.second");
		assertThat(tokens.get(1).scopes).containsExactly("source.test", "meta.second.first");
		assertThat(tokens.get(1).startIndex).isEqualTo(1);
	}

	@Test
	void keepsNestedScopeWhoseWordsRepeatAnOuterScope() {
		// Destructuring rule copied from the language pack's CoffeeScript grammar (coffeescript.tmLanguage.json).
		// "variable.assignment.coffee" adds no words to the enclosing "meta.variable.assignment.destructured.array.coffee",
		// so both identifiers and the ", " between them (outer scope only) decode to the same type. Merging them by type
		// would either drop the identifier scope or wrongly give it to the separator, depending on which token comes first.
		final var tokens = tokenize(ContentType.YAML, """
			scopeName: source.coffee
			patterns:
			  - name: meta.variable.assignment.destructured.array.coffee
			    begin: '(?<=\\s|^)(\\[)(?=[^''"#]+?\\][\\s\\]}]*=)'
			    beginCaptures:
			      '1': { name: punctuation.definition.destructuring.begin.bracket.square.coffee }
			    end: '\\]'
			    endCaptures:
			      '0': { name: punctuation.definition.destructuring.end.bracket.square.coffee }
			    patterns:
			      - include: $self
			      - name: variable.assignment.coffee
			        match: '[a-zA-Z$_]\\w*'
			""", "[first, second] = pair");
		assertThat(tokenAt(tokens, 1).scopes).contains("variable.assignment.coffee"); // first
		assertThat(tokenAt(tokens, 6).startIndex).isEqualTo(6); // ", "
		assertThat(tokenAt(tokens, 6).scopes).doesNotContain("variable.assignment.coffee");
		assertThat(tokenAt(tokens, 8).startIndex).isEqualTo(8); // second
		assertThat(tokenAt(tokens, 8).scopes).contains("variable.assignment.coffee");
	}

	@Test
	void keepsTokensWithEqualScopesFromDifferentGrammars() {
		final var registry = new Registry();
		registry.addGrammar(IGrammarSource.fromString(ContentType.JSON, """
			{"scopeName":"source.other","patterns":[],"repository":{"block":{"begin":"<","end":">"}}}"""));
		final var host = registry.addGrammar(IGrammarSource.fromString(ContentType.JSON, """
			{"scopeName":"source.host","patterns":[{"include":"source.other#block"}]}"""));
		// An unnamed rule included from another grammar adds no scope, so all tokens share the host's scope list.
		// Only the grammar origin tells them apart, which partitions rely on.
		final var tokens = new TMTokenizationSupport(host).tokenize("a<b>c", null).tokens;
		assertThat(tokenAt(tokens, 0).scopes).isEqualTo(tokenAt(tokens, 2).scopes);
		assertThat(tokenAt(tokens, 0).grammarScope).isEqualTo("source.host");
		assertThat(tokenAt(tokens, 2).grammarScope).isEqualTo("source.other");
	}

	private static List<TMToken> tokenize(final ContentType contentType, final String grammar, final String line) {
		return new TMTokenizationSupport(new Registry().addGrammar(IGrammarSource.fromString(contentType, grammar)))
				.tokenize(line, null).tokens;
	}

	/** Returns the document token covering the given offset. */
	private static TMToken tokenAt(final List<TMToken> tokens, final int offset) {
		TMToken result = tokens.get(0);
		for (final TMToken token : tokens) {
			if (token.startIndex > offset)
				break;
			result = token;
		}
		return result;
	}

	@Test
	void testTokenizeWithTimeout() throws IOException {
		final var grammar = new Registry().addGrammar(fromResource(Data.class, "TypeScript.tmLanguage.json"));

		final var tokenizer = new TMTokenizationSupport(grammar);
		try (var reader = ResourceUtils.getResourceReader(Data.class, "raytracer.ts")) {
			final String veryLongLine = reader.lines().collect(Collectors.joining());
			final var result1 = tokenizer.tokenize(veryLongLine, null);
			assertThat(result1.stoppedEarly).isFalse();

			final var result2 = tokenizer.tokenize(veryLongLine, null, 0, Duration.ofMillis(10));
			assertThat(result2.stoppedEarly).isTrue();

			assertThat(result1.tokens.size()).isNotEqualTo(result2.tokens.size());
		}
	}
}
