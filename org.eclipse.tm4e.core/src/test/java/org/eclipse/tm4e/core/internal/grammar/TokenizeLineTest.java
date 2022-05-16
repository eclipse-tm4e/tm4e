package org.eclipse.tm4e.core.internal.grammar;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

public class TokenizeLineTest {

	@Test
	void testTokenizeLine2() throws Exception {
		final var grammar = new Registry().addGrammar(IGrammarSource.fromResource(Data.class, "JavaScript.tmLanguage"));

		final var lineTokens = grammar.tokenizeLine("function add(a,b) { return a+b; }");
		for (int i = 0; i < lineTokens.getTokens().length; i++) {
			final var token = lineTokens.getTokens()[i];
			final var s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
				+ token.getScopes();
			System.out.println(s);
		}

		System.out.println("----------");

		final var lineTokens2 = grammar.tokenizeLine2("function add(a,b) { return a+b; }");
		for (int i = 0; i < lineTokens2.getTokens().length; i++) {
			final int token = lineTokens2.getTokens()[i];
			System.out.println(token);
		}
	}
}
