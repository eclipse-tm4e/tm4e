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
package org.eclipse.tm4e.core.grammar;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

/**
 * Test for grammar tokenizer.
 *
 */
class GrammarTest2 {

	@Test
	void tokenizeLines() throws Exception {
		Registry registry = new Registry();
		String path = "JavaScript.tmLanguage";
		IGrammar grammar = castNonNull(registry.loadGrammarFromPathSync(path, Data.class.getResourceAsStream(path)));

		IStackElement ruleStack = null;
		int i = 0;

		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Data.class.getResourceAsStream("raytracer.ts")));){
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int t = 0;
		boolean stop = false;
		long start = System.currentTimeMillis();
		for (String line : lines) {
			ITokenizeLineResult lineTokens = grammar.tokenizeLine(line, ruleStack);
			if (stop) {
				t = 1000;
				Thread.sleep(t);
				stop = false;
			}
			ruleStack = lineTokens.getRuleStack();
			for (i = 0; i < lineTokens.getTokens().length; i++) {
				IToken token = lineTokens.getTokens()[i];
				String s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes "
						+ token.getScopes();
				System.err.println(s);
				// Assert.assertEquals(EXPECTED_MULTI_LINE_TOKENS[i + j], s);
			}
		}
		System.out.println(System.currentTimeMillis() - start - t);
	}
}
