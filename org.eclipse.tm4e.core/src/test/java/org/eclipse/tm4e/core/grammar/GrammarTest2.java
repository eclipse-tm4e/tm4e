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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

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
      final var registry = new Registry();
      final var path = "JavaScript.tmLanguage";
      try (var is = Data.class.getResourceAsStream(path)) {
         final var grammar = castNonNull(registry.loadGrammarFromPathSync(path, is));

         IStackElement ruleStack = null;
         int i = 0;

         final var lines = new ArrayList<String>();
         try (var reader = new BufferedReader(new InputStreamReader(Data.class.getResourceAsStream("raytracer.ts")));) {
            String line = null;
            while ((line = reader.readLine()) != null) {
               lines.add(line);
            }
         }

         int t = 0;
         boolean stop = false;
         final long start = System.currentTimeMillis();
         for (final String line : lines) {
            final ITokenizeLineResult lineTokens = grammar.tokenizeLine(line, ruleStack);
            if (stop) {
               t = 1000;
               Thread.sleep(t);
               stop = false;
            }
            ruleStack = lineTokens.getRuleStack();
            for (i = 0; i < lineTokens.getTokens().length; i++) {
               final IToken token = lineTokens.getTokens()[i];
               final var s = "Token from " + token.getStartIndex() + " to " + token.getEndIndex() + " with scopes " + token.getScopes();
               // System.err.println(s);
               // Assert.assertEquals(EXPECTED_MULTI_LINE_TOKENS[i + j], s);
            }
         }
         System.out.println(System.currentTimeMillis() - start - t);
      }
   }

   @Test
   public void testYamlMultiline() throws Exception {
      final var registry = new Registry();
      final var path = "yaml.tmLanguage.json";
      try (var in = Data.class.getResourceAsStream(path)) {
         final var grammar = castNonNull(registry.loadGrammarFromPathSync(path, in));
         final var lines = ">\n should.be.string.unquoted.block.yaml\n should.also.be.string.unquoted.block.yaml";
         final var result = TokenizationUtils.tokenizeText(lines, grammar).iterator();
         assertTrue(Arrays.stream(result.next().getTokens()).anyMatch(t -> t.getScopes().contains(
            "keyword.control.flow.block-scalar.folded.yaml")));
         assertTrue(Arrays.stream(result.next().getTokens()).anyMatch(t -> t.getScopes().contains("string.unquoted.block.yaml")));
         assertTrue(Arrays.stream(result.next().getTokens()).anyMatch(t -> t.getScopes().contains("string.unquoted.block.yaml")));
      }
   }
}
