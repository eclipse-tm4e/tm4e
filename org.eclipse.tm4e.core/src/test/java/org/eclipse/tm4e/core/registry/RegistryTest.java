/**
 * Copyright (c) 2023 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.core.registry;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
class RegistryTest {

	@Test
	void testLanguagePackGrammars() throws IOException {
		final var reg = new Registry();
		final var count = new AtomicInteger();
		Files.walkFileTree(Paths.get("../org.eclipse.tm4e.language_pack"), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final @Nullable BasicFileAttributes attrs) throws IOException {
				if (file.getFileName().toString().endsWith("tmLanguage.json")) {
					try (var input = Files.newBufferedReader(file)) {
						System.out.println("Parsing [" + file + "]...");
						final var grammar = reg.addGrammar(IGrammarSource.fromFile(file));
						count.incrementAndGet();
						assertFalse(grammar.getScopeName().isBlank());
						assertNotNull(grammar.getFileTypes());
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		System.out.println("Successfully parsed " + count.intValue() + " grammars.");
		assertTrue(count.intValue() > 10, "Only " + count.intValue() + " grammars found, expected more than 10!");
	}

	@Test
	void testLoadingUnknownGrammar() {
		final var reg = new Registry();
		assertNull(reg.grammarForScopeName("undefined"));
		assertNull(reg.loadGrammar("undefined"));
	}
}
