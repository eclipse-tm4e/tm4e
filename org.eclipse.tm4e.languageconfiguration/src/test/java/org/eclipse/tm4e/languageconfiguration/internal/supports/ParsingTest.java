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

public class ParsingTest {

	@Nullable
	private LanguageConfiguration loadLanguageConfiguration(final String path) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(path)) {
			assertNotNull(is);
			return LanguageConfiguration.load(new InputStreamReader(is));
		}
	}

	@Test
	public void testCanLoadPhpLanguageConfig() throws Exception {
		final var languageConfiguration = loadLanguageConfiguration("/php-language-configuration.json");
		assertNotNull(languageConfiguration);
		final var comments = languageConfiguration.getComments();
		assertNotNull(comments);
		assertNotNull(comments.blockComment);
		assertEquals(comments.lineComment, "//");
		assertNotNull(languageConfiguration.getBrackets());
		assertNotNull(languageConfiguration.getAutoClosingPairs());
		assertEquals(";:.,=}])>` \n\t", languageConfiguration.getAutoCloseBefore());
		assertNotNull(languageConfiguration.getWordPattern());
		assertNotNull(languageConfiguration.getOnEnterRules());
		assertNotNull(languageConfiguration.getSurroundingPairs());
		assertNotNull(languageConfiguration.getFolding());
	}

	@Test
	public void testCanLoadRustLanguageConfig() throws Exception {
		final var languageConfiguration = loadLanguageConfiguration("/rust-language-configuration.json");
		assertNotNull(languageConfiguration);
		final var comments = languageConfiguration.getComments();
		assertNotNull(comments);
		assertNotNull(comments.blockComment);
		assertEquals(comments.lineComment, "//");
		assertNotNull(languageConfiguration.getBrackets());
		assertNotNull(languageConfiguration.getAutoClosingPairs());
		assertNull(languageConfiguration.getAutoCloseBefore());
		assertNull(languageConfiguration.getWordPattern());
		assertNotNull(languageConfiguration.getOnEnterRules());
		assertNotNull(languageConfiguration.getSurroundingPairs());
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
}
