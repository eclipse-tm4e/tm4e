/**
 * Copyright (c) 2026 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 */
package org.eclipse.tm4e.core.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

/**
 * Regression test for applyEndPatternLast with zero-width begin patterns.
 *
 * <p>
 * Gson parses JSON number {@code 1} as {@link Double}, but
 * {@code isApplyEndPatternLast()} only checked for {@link Integer},
 * causing the end pattern to be placed first instead of last.
 * This triggered a false "endless loop" when begin was zero-width.
 * </p>
 */
class GroovyMethodTokenizationTest {

	@Test
	void applyEndPatternLast_withZeroWidthBegin_shouldTokenizeInnerPatterns() throws Exception {
		final var registry = new Registry();
		final var grammar = registry.addGrammar(
				IGrammarSource.fromResource(GroovyMethodTokenizationTest.class, "/test-method-grammar.tmLanguage.json"));

		final var result = grammar.tokenizeLine("def foo() {", null, null);
		final var tokens = result.getTokens();

		assertTrue(tokens.length > 1,
				"Should produce multiple tokens, not a single token from endless-loop bailout");

		// 'def' token: [0-3] with storage.type.def scope
		assertEquals(0, tokens[0].getStartIndex());
		assertEquals(3, tokens[0].getEndIndex());
		assertTrue(tokens[0].getScopes().stream().anyMatch(s -> s.contains("storage.type.def")),
				"'def' should have storage.type.def scope, got: " + tokens[0].getScopes());

		// 'foo' token should have entity.name.function scope
		boolean hasFuncName = false;
		for (final IToken token : tokens) {
			if (token.getScopes().stream().anyMatch(s -> s.contains("entity.name.function"))) {
				assertEquals(4, token.getStartIndex());
				assertEquals(7, token.getEndIndex());
				hasFuncName = true;
			}
		}
		assertTrue(hasFuncName, "'foo' should have entity.name.function scope");
	}
}
