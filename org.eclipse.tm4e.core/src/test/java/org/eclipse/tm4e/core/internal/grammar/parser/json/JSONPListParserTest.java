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
package org.eclipse.tm4e.core.internal.grammar.parser.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.grammar.reader.GrammarReader;
import org.eclipse.tm4e.core.internal.grammar.reader.IGrammarParser;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;

import org.junit.jupiter.api.Test;

public class JSONPListParserTest {

	@Test
	public void testParseJSONPList() throws Exception {
		IGrammarParser parser = GrammarReader.JSON_PARSER;
		try (InputStream is = Data.class.getResourceAsStream("csharp.json")) {
			IRawGrammar grammar = parser.parse(is);
			assertNotNull(grammar);
			assertFalse(grammar.getFileTypes().isEmpty());
			System.err.println(grammar);
		}
	}
}
