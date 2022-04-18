/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.grammar.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;

public class RawTestImpl {

	private String desc;
	private List<String> grammars;
	private String grammarPath;
	private String grammarScopeName;
	private List<String> grammarInjections;
	private List<RawTestLine> lines;
	private File testLocation;

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

	public List<RawTestLine> getLines() {
		return lines;
	}

	public void executeTest() throws Exception {
		IRegistryOptions locator = new IRegistryOptions() {

			@Override
			public String getFilePath(String scopeName) {
				return null;
			}

			@Override
			public InputStream getInputStream(String scopeName) throws IOException {
				return null;
			}

			@Override
			public Collection<String> getInjections(String scopeName) {
				if (scopeName.equals(getGrammarScopeName())) {
					return getGrammarInjections();
				}
				return null;
			}
		};

		Registry registry = new Registry(locator);
		IGrammar grammar = getGrammar(registry, testLocation.getParentFile());

		if (getGrammarScopeName() != null) {
			grammar = registry.grammarForScopeName(getGrammarScopeName());
		}

		if (grammar == null) {
			throw new Exception("I HAVE NO GRAMMAR FOR TEST");
		}

		StackElement prevState = null;
		for (RawTestLine testLine : getLines()) {
			prevState = assertLineTokenization(grammar, testLine, prevState);
		}
	}

	private IGrammar getGrammar(Registry registry, File testLocation) throws Exception {
		IGrammar grammar = null;
		for (String grammarPath : getGrammars()) {
			IGrammar tmpGrammar = registry.loadGrammarFromPathSync(new File(testLocation, grammarPath));
			if (grammarPath.equals(getGrammarPath())) {
				grammar = tmpGrammar;
			}
		}
		return grammar;
	}

	private static StackElement assertLineTokenization(IGrammar grammar, RawTestLine testCase, StackElement prevState) {
		ITokenizeLineResult actual = grammar.tokenizeLine(testCase.getLine(), prevState);

		List<RawToken> actualTokens = getActualTokens(actual.getTokens(), testCase);

		List<RawToken> expectedTokens = testCase.getTokens();
		// // TODO@Alex: fix tests instead of working around
		if (!testCase.getLine().isEmpty()) {
			// Remove empty tokens...
			expectedTokens = testCase.getTokens().stream().filter(token -> !token.getValue().isEmpty())
					.collect(Collectors.toList());
		}
		deepEqual(actualTokens, expectedTokens, "Tokenizing line '" + testCase.getLine() + "'");

		return actual.getRuleStack();
	}

	private static void deepEqual(List<RawToken> actualTokens, List<RawToken> expextedTokens, String message) {
		// compare collection size
		assertEquals(expextedTokens.size(), actualTokens.size(), message + " (collection size problem)");
		// compare item
		for (int i = 0; i < expextedTokens.size(); i++) {
			RawToken expected = expextedTokens.get(i);
			RawToken actual = actualTokens.get(i);
			assertEquals(expected.getValue(), actual.getValue(), message + " (value of item '" + i + "' problem)");
			assertEquals(expected.getScopes(), actual.getScopes(), message + " (tokens of item '" + i + "' problem)");
		}

	}

	private static List<RawToken> getActualTokens(IToken[] tokens, RawTestLine testCase) {
		List<RawToken> actualTokens = new ArrayList<>();
		for (IToken token : tokens) {
			String value = testCase.getLine().substring(token.getStartIndex(),
					token.getEndIndex() < testCase.getLine().length() ? token.getEndIndex()
							: testCase.getLine().length());
			actualTokens.add(new RawToken(value, token.getScopes()));
		}
		return actualTokens;
	}

	public void setTestLocation(File testLocation) {
		this.testLocation = testLocation;
	}

}
