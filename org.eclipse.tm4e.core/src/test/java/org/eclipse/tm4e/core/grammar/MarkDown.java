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

import java.util.ArrayList;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.utils.ResourceUtils;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;

public final class MarkDown {

	public static void main(final String[] args) throws Exception {
		final var registry = new Registry();
		final IGrammar grammar = registry.addGrammar(IGrammarSource.fromResource(Data.class, "Markdown.tmLanguage"));

		final var lines = new ArrayList<String>();
		try (var reader = ResourceUtils.getResourceReader(Data.class, "test.md.txt")) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		final long start = System.currentTimeMillis();

		IStateStack ruleStack = null;
		int i = 0;
		for (final String line : lines) {
			final var lineTokens = grammar.tokenizeLine(line, ruleStack, null);
			ruleStack = lineTokens.getRuleStack();
			for (i = 0; i < lineTokens.getTokens().length; i++) {
				// IToken token = lineTokens.getTokens()[i];
				// String s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
				// + token.getScopes();
				// System.err.println(s);
				// Assert.assertEquals(EXPECTED_MULTI_LINE_TOKENS[i + j], s);
			}
		}
		System.err.println(System.currentTimeMillis() - start);
	}

	static String convertStreamToString(final java.io.InputStream is) {
		try (var s = new java.util.Scanner(is).useDelimiter("\\A")) {
			return s.hasNext() ? s.next() : "";
		}
	}

	private MarkDown() {
	}
}
