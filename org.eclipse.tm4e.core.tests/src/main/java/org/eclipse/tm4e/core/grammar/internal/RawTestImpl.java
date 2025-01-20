/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.grammar.internal;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IStateStack;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;

/**
 * @see <a href="https://github.com/microsoft/vscode-textmate/blob/167bbbd509356cc4617f250c0d754aef670ab14a/src/tests/tokenization.test.ts">
 *      github.com/microsoft/vscode-textmate/blob/main/src/tests/tokenization.test.ts</a>
 */
public class RawTestImpl {

	private static final class RawTestLine {
		String line;
		List<RawToken> tokens;
	}

	private String desc;
	private List<String> grammars;
	private String grammarPath;
	private String grammarScopeName;
	private List<String> grammarInjections;
	private List<RawTestLine> lines;
	private transient File testLocation;

	public String getDesc() {
		return desc;
	}

	public List<String> getGrammars() {
		return grammars;
	}

	public String getGrammarPath() {
		return grammarPath;
	}

	public String getGrammarScopeName() {
		return grammarScopeName;
	}

	public List<String> getGrammarInjections() {
		return grammarInjections;
	}

	public void executeTest() throws Exception {
		final var options = new IRegistryOptions() {
			@Override
			public Collection<String> getInjections(final String scopeName) {
				if (scopeName.equals(getGrammarScopeName())) {
					return getGrammarInjections();
				}
				return null;
			}
		};

		final var registry = new Registry(options);
		IGrammar grammar = getGrammar(registry, testLocation.getParentFile());
		if (getGrammarScopeName() != null) {
			grammar = registry.grammarForScopeName(getGrammarScopeName());
		}
		if (grammar == null) {
			throw new IllegalStateException("I HAVE NO GRAMMAR FOR TEST");
		}

		IStateStack prevState = null;
		for (final var testLine : lines) {
			prevState = assertLineTokenization(grammar, testLine, prevState);
		}
	}

	private IGrammar getGrammar(final Registry registry, final File testLocation) throws Exception {
		IGrammar grammar = null;
		for (final String grammarPath : getGrammars()) {
			final IGrammar tmpGrammar = registry.addGrammar(IGrammarSource.fromFile(new File(testLocation, grammarPath).toPath()));
			if (grammarPath.equals(getGrammarPath())) {
				grammar = tmpGrammar;
			}
		}
		return grammar;
	}

	private static IStateStack assertLineTokenization(final IGrammar grammar, final RawTestLine testCase, final IStateStack prevState) {
		final var line = testCase.line;
		final var actual = grammar.tokenizeLine(line, prevState, null);

		final var actualTokens = Arrays.stream(actual.getTokens())
				.map(token -> new RawToken(
						line.substring(
								token.getStartIndex(),
								Math.min(token.getEndIndex(), line.length()) // TODO Math.min not required in upstream why?
						),
						token.getScopes()))
				.toList();

		// TODO@Alex: fix tests instead of working around
		if (!line.isEmpty()) {
			// Remove empty tokens...
			testCase.tokens = testCase.tokens.stream().filter(token -> !token.getValue().isEmpty()).toList();
		}

		deepEqual(actualTokens, testCase.tokens, "Tokenizing line '" + line + "'");

		return actual.getRuleStack();
	}

	private static void deepEqual(final List<RawToken> actualTokens, final List<RawToken> expextedTokens, final String message) {

		// compare collection size
		if (expextedTokens.size() != actualTokens.size()) {
			final var actualTokensStr = actualTokens.stream().map(Object::toString).collect(joining("\n"));
			final var expextedTokensStr = expextedTokens.stream().map(Object::toString).collect(joining("\n"));

			assertThat(actualTokensStr).as(message + " (collection size)").isEqualTo(expextedTokensStr);
		}

		// compare item
		for (int i = 0; i < expextedTokens.size(); i++) {
			final var expected = expextedTokens.get(i);
			final var actual = actualTokens.get(i);
			assertThat(actual.getValue()).as(message + " (value of item '" + i + "' problem)").isEqualTo(expected.getValue());
			assertThat(actual.getScopes()).as(message + " (scopes of item '" + i + "' problem)").isEqualTo(expected.getScopes());
		}
	}

	public void setTestLocation(final File testLocation) {
		this.testLocation = testLocation;
	}
}
