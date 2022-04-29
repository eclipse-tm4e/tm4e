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
package org.eclipse.tm4e.core.internal.grammar.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.grammar.Raw;
import org.eclipse.tm4e.core.internal.parser.PListParserJSON;
import org.eclipse.tm4e.core.internal.parser.PListParserXML;
import org.eclipse.tm4e.core.internal.parser.PListParserYAML;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;
import org.junit.jupiter.api.Test;

public class PListParserTest {

	@Test
	void testParseJSONPList() throws Exception {
		final var parser = new PListParserJSON<IRawGrammar>(Raw::new);
		try (final var is = Data.class.getResourceAsStream("csharp.json")) {
			final var grammar = parser.parse(is);
			assertNotNull(grammar);
			assertFalse(grammar.getFileTypes().isEmpty());
			System.out.println(grammar);
		}
	}

	@Test
	void testParseYAMLPlist() throws Exception {
		final var parser = new PListParserYAML<IRawGrammar>(Raw::new);
		try (final var is = Data.class.getResourceAsStream("JavaScript.tmLanguage.yaml")) {
			final var grammar = parser.parse(is);
			assertNotNull(grammar);
			assertFalse(grammar.getFileTypes().isEmpty());
			System.out.println(grammar);
		}
	}

	@Test
	void testParseXMLPlist() throws Exception {
		final var parser = new PListParserXML<IRawGrammar>(Raw::new);
		try (final var is = Data.class.getResourceAsStream("JavaScript.tmLanguage")) {
			final var grammar = parser.parse(is);
			assertNotNull(grammar);
			assertFalse(grammar.getFileTypes().isEmpty());
			System.out.println(grammar);
		}
	}
}
