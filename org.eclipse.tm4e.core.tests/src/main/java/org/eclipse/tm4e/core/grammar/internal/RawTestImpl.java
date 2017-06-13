/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.grammar.internal;

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
import org.eclipse.tm4e.core.logger.SystemLogger;
import org.eclipse.tm4e.core.registry.IRegistryOptions;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.Assert;
import org.junit.runner.Describable;
import org.junit.runner.Description;

import junit.framework.Test;
import junit.framework.TestResult;

public class RawTestImpl implements Test, Describable {

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

	@Override
	public void run(TestResult result) {
		try {
			result.startTest(this);
			executeTest(this, testLocation);
		} catch (Throwable e) {
			result.addError(this, e);
		} finally {
			result.endTest(this);
		}
	}

	@Override
	public int countTestCases() {
		return 1;
	}

	private static void executeTest(RawTestImpl test, File testLocation) throws Exception {
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
				if (scopeName.equals(test.getGrammarScopeName())) {
					return test.getGrammarInjections();
				}
				return null;
			}
		};

		Registry registry = new Registry(locator, SystemLogger.INSTANCE);
		IGrammar grammar = getGrammar(test, registry, testLocation.getParentFile());

		if (test.getGrammarScopeName() != null) {
			grammar = registry.grammarForScopeName(test.getGrammarScopeName());
		}

		if (grammar == null) {
			throw new Exception("I HAVE NO GRAMMAR FOR TEST");
		}

		StackElement prevState = null;
		for (RawTestLine testLine : test.getLines()) {
			prevState = assertLineTokenization(grammar, testLine, prevState);
		}
	}

	private static IGrammar getGrammar(RawTestImpl test, Registry registry, File testLocation) throws Exception {
		IGrammar grammar = null;
		for (String grammarPath : test.getGrammars()) {
			IGrammar tmpGrammar = registry.loadGrammarFromPathSync(new File(testLocation, grammarPath));
			if (grammarPath.equals(test.getGrammarPath())) {
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
		if (testCase.getLine().length() > 0) {
			// Remove empty tokens...
			expectedTokens = testCase.getTokens().stream().filter(token -> token.getValue().length() > 0)
					.collect(Collectors.toList());
		}
		deepEqual(actualTokens, expectedTokens, "Tokenizing line '" + testCase.getLine() + "'");

		return actual.getRuleStack();
	}

	private static void deepEqual(List<RawToken> actualTokens, List<RawToken> expextedTokens, String message) {
		// compare collection size
		Assert.assertEquals(message + " (collection size problem)", expextedTokens.size(), actualTokens.size());
		// compare item
		for (int i = 0; i < expextedTokens.size(); i++) {
			RawToken expected = expextedTokens.get(i);
			RawToken actual = actualTokens.get(i);
			Assert.assertEquals(message + " (value of item '" + i + "' problem)", expected.getValue(),
					actual.getValue());
			Assert.assertEquals(message + " (tokens of item '" + i + "' problem)", expected.getScopes(),
					actual.getScopes());
		}

	}

	private static List<RawToken> getActualTokens(IToken[] tokens, RawTestLine testCase) {
		List<RawToken> actualTokens = new ArrayList<>();
		for (int i = 0; i < tokens.length; i++) {
			IToken token = tokens[i];
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

	@Override
	public Description getDescription() {
		return Description.createSuiteDescription(desc);
	}
}
