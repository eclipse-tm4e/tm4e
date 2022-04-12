/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke - initial implementation
 */
package org.eclipse.tm4e.core.internal.grammar.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.grammar.reader.GrammarReader;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.junit.jupiter.api.Test;

public class PlistFormatsTest {

	private IRawGrammar loadGrammar(String path) throws Exception {
		try (InputStream is = Data.class.getResourceAsStream(path)) {
			return GrammarReader.readGrammarSync(path, is);
		}
	}

	@Test
	public void testPlistFormats() throws Exception {
		IRawGrammar grammarFromXML = loadGrammar("JavaScript.tmLanguage");
		IRawGrammar grammarFromJSON = loadGrammar("JavaScript.tmLanguage.json");
		IRawGrammar grammarFromYAML = loadGrammar("JavaScript.tmLanguage.yaml");

		assertEquals(grammarFromXML, grammarFromJSON);
		assertEquals(grammarFromXML, grammarFromYAML);
	}
}
