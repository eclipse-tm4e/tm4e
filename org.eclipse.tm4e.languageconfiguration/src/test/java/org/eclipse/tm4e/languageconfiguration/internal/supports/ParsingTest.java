/*********************************************************************
 * Copyright (c) 2018 Red Hat Inc., and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;
import org.junit.jupiter.api.Test;

class ParsingTest {

	@Nullable
	private LanguageConfiguration loadLanguageConfiguration(final String path) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(path)) {
			assertNotNull(is);
			return LanguageConfiguration.load(new InputStreamReader(is));
		}
	}

	@Test
	void testOniguramaFallback() throws Exception {
		final var languageConfiguration = LanguageConfiguration.load(new StringReader("""
			{
			  "onEnterRules": [{
			    "beforeText": "^[\\\\s]*///.*$",
			    "action": {
			      "indent": "none",
			    }
			  }],
			  "folding": {
			    "markers": {
			      "start": "{%\\\\s*(block|filter|for|if|macro|raw)",
			      "end": ""
			    }
			  }
			}"""));
		assertNotNull(languageConfiguration);

		assertTrue(languageConfiguration.getOnEnterRules().get(0).beforeText.getClass().getSimpleName().endsWith("JavaRegExPattern"));

		final var folding = languageConfiguration.getFolding();
		assert folding != null;
		assertTrue(folding.markersStart.getClass().getSimpleName().endsWith("OnigRegExPattern"));
	}

	@Test
	void testCanLoadPhpLanguageConfig() throws Exception {
		final var languageConfiguration = loadLanguageConfiguration("/php-language-configuration.json");
		assertNotNull(languageConfiguration);
		final var comments = languageConfiguration.getComments();
		assertNotNull(comments);
		assertNotNull(comments.blockComment);
		assertEquals("//", comments.lineComment);
		assertEquals(3, languageConfiguration.getBrackets().size());
		assertEquals(6, languageConfiguration.getAutoClosingPairs().size());
		assertEquals(";:.,=}])>` \n\t", languageConfiguration.getAutoCloseBefore());
		assertNotNull(languageConfiguration.getWordPattern());
		assertEquals(5, languageConfiguration.getOnEnterRules().size());
		assertEquals(6, languageConfiguration.getSurroundingPairs().size());
		assertNotNull(languageConfiguration.getFolding());
	}

	@Test
	void testCanLoadRustLanguageConfig() throws Exception {
		final var languageConfiguration = loadLanguageConfiguration("/rust-language-configuration.json");
		assertNotNull(languageConfiguration);
		final var comments = languageConfiguration.getComments();
		assertNotNull(comments);
		assertNotNull(comments.blockComment);
		assertEquals("//", comments.lineComment);
		assertEquals(4, languageConfiguration.getBrackets().size());
		assertEquals(6, languageConfiguration.getAutoClosingPairs().size());
		assertNull(languageConfiguration.getAutoCloseBefore());
		assertNull(languageConfiguration.getWordPattern());
		assertEquals(6, languageConfiguration.getOnEnterRules().size());
		assertEquals(6, languageConfiguration.getSurroundingPairs().size());
		assertNull(languageConfiguration.getFolding());
	}

	@Test
	void testLanguagePackLangConfigs() throws IOException {
		final var count = new AtomicInteger();
		Files.walkFileTree(Paths.get("../org.eclipse.tm4e.language_pack"), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final @Nullable BasicFileAttributes attrs) throws IOException {
				if (file.getFileName().toString().endsWith("language-configuration.json")) {
					try (var input = Files.newBufferedReader(file)) {
						System.out.println("Parsing [" + file + "]...");
						final var languageConfiguration = LanguageConfiguration.load(input);
						count.incrementAndGet();
						assertNotNull(languageConfiguration);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		System.out.println("Successfully parsed " + count.intValue() + " language configurations.");
		assertTrue(count.intValue() > 10, "Only " + count.intValue() + " language configurations found, expected more than 10!");
	}

	@Test
	void testParseColorizedBracketsPair() throws Exception {
		final var languageConfiguration = LanguageConfiguration.load(new StringReader("""
			{
				"colorizedBracketPairs": [
					["(",")"],
					["[","]"],
					["{","}"],
					["<",">"]
				],
			}"""));
		assertNotNull(languageConfiguration);

		assertEquals(4, languageConfiguration.getColorizedBracketPairs().size());
		assertEquals("(", languageConfiguration.getColorizedBracketPairs().get(0).open);
		assertEquals(")", languageConfiguration.getColorizedBracketPairs().get(0).close);
	}

	@Test
	void testIndentationRules() throws Exception {
		final var languageConfiguration = LanguageConfiguration.load(new StringReader("""
			{
				"indentationRules": {
					"increaseIndentPattern": "(^.*\\\\{[^}]*$)",
					"decreaseIndentPattern": "^\\\\s*\\\\}"
				},
			}"""));
		assertNotNull(languageConfiguration);

		var indentationRules = languageConfiguration.getIndentationRules();
		assert indentationRules != null;
		assertEquals("(^.*\\{[^}]*$)", indentationRules.increaseIndentPattern.toString());
		assertEquals("^\\s*\\}", indentationRules.decreaseIndentPattern.toString());
	}
}
