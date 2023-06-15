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
package org.eclipse.tm4e.core.internal.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.grammar.raw.RawGrammarReader;
import org.eclipse.tm4e.core.internal.grammar.raw.RawGrammar;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
class PListParserTest {

	@Test
	void testParseJSONPList() throws Exception {
		final var parser = new PListParserJSON<RawGrammar>(RawGrammarReader.OBJECT_FACTORY);
		try (var is = Data.class.getResourceAsStream("csharp.json")) {
			final var grammar = parser.parse(new InputStreamReader(is));
			assertNotNull(grammar);
			assertNotNull(grammar.getRepository());
			assertFalse(grammar.getFileTypes().isEmpty());
			assertEquals(List.of("cs"), grammar.getFileTypes());
			assertEquals("C#", grammar.getName());
			assertEquals("source.cs", grammar.getScopeName());
			assertEquals(List.of("cs"), grammar.getFileTypes());
			assertEquals(Set.of("fileTypes", "foldingStartMarker", "foldingStopMarker", "name", "patterns", "repository", "scopeName"),
					grammar.keySet());
		}
	}

	@Test
	void testParseYAMLPlist() throws Exception {
		final var parser = new PListParserYAML<RawGrammar>(RawGrammarReader.OBJECT_FACTORY);
		try (var is = Data.class.getResourceAsStream("JavaScript.tmLanguage.yaml")) {
			final var grammar = parser.parse(new InputStreamReader(is));
			assertNotNull(grammar);
			assertNotNull(grammar.getRepository());
			assertFalse(grammar.getFileTypes().isEmpty());
			assertEquals(List.of("js", "jsx"), grammar.getFileTypes());
			assertEquals("JavaScript (with React support)", grammar.getName());
			assertEquals("source.js", grammar.getScopeName());
			assertEquals(Set.of("fileTypes", "name", "patterns", "repository", "scopeName", "uuid"), grammar.keySet());
		}
	}

	@Test
	void testParseXMLPlist() throws Exception {
		final var parser = new PListParserXML<RawGrammar>(RawGrammarReader.OBJECT_FACTORY);
		try (var is = Data.class.getResourceAsStream("JavaScript.tmLanguage")) {
			final var grammar = parser.parse(new InputStreamReader(is));
			assertNotNull(grammar);
			assertNotNull(grammar.getRepository());
			assertFalse(grammar.getFileTypes().isEmpty());
			assertEquals(List.of("js", "jsx"), grammar.getFileTypes());
			assertEquals("JavaScript (with React support)", grammar.getName());
			assertEquals("source.js", grammar.getScopeName());
			assertEquals(Set.of("fileTypes", "name", "patterns", "repository", "scopeName", "uuid"), grammar.keySet());
		}
	}
}
