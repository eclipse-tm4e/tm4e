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
package org.eclipse.tm4e.core.internal.grammar.reader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.junit.jupiter.api.Test;

public class GrammarReaderTest {

	private IRawGrammar loadGrammar(String path) throws Exception {
		try (InputStream is = Data.class.getResourceAsStream(path)) {
			return GrammarReader.readGrammarSync(path, is);
		}
	}

	/**
	 * Loads the same TextMate grammar in different formats and checks
	 * loading them results in equal IRawGrammar objects.
	 */
	@Test
	public void testLoadDifferentPlistFormats() throws Exception {
		IRawGrammar grammarFromXML = loadGrammar("JavaScript.tmLanguage");
		IRawGrammar grammarFromJSON = loadGrammar("JavaScript.tmLanguage.json");
		IRawGrammar grammarFromYAML = loadGrammar("JavaScript.tmLanguage.yaml");

		assertNotNull(grammarFromXML);
		assertFalse(grammarFromXML.getFileTypes().isEmpty());

		assertEquals(grammarFromXML, grammarFromJSON);
		assertEquals(grammarFromJSON, grammarFromYAML);
	}
}
