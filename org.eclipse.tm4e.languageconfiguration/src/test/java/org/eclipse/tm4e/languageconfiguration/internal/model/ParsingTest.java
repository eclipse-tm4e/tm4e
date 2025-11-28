/*********************************************************************
 * Copyright (c) 2018 Red Hat Inc., and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tm4e.languageconfiguration.internal.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

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
import org.junit.jupiter.api.Test;

class ParsingTest {

	private LanguageConfiguration loadLanguageConfigFromClassPath(final String path) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(path)) {
			assert is != null;
			return castNonNull(LanguageConfiguration.load(new InputStreamReader(is)));
		}
	}

	private LanguageConfiguration loadLanguageConfigFromString(final String content) {
		return castNonNull(LanguageConfiguration.load(new StringReader(content)));
	}

	@Test
	void testOniguramaFallback() {
		final var languageConfiguration = loadLanguageConfigFromString("""
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
			      "end": "{%\\\\s*end(block|filter|for|if|macro|raw)\\\\s*%}"
			    }
			  }
			}""");
		assertThat(languageConfiguration.getOnEnterRules().get(0).beforeText.getClass().getSimpleName())
				.endsWith("JavaRegExPattern");

		final var folding = castNonNull(languageConfiguration.getFolding());
		assertThat(folding.markers.start.getClass().getSimpleName()).endsWith("OnigRegExPattern");
	}

	@Test
	void testCanLoadPhpLanguageConfig() throws Exception {
		final var languageConfiguration = loadLanguageConfigFromClassPath("/php-language-configuration.json");
		final var comments = castNonNull(languageConfiguration.getComments());
		assertThat(comments.blockComment).isNotNull();
		assertThat(comments.lineComment).isEqualTo("//");
		assertThat(languageConfiguration.getBrackets()).hasSize(3);
		assertThat(languageConfiguration.getAutoClosingPairs()).hasSize(6);
		assertThat(languageConfiguration.getAutoCloseBefore()).isEqualTo(";:.,=}])>` \n\t");
		assertThat(languageConfiguration.getWordPattern()).isNotNull();
		assertThat(languageConfiguration.getOnEnterRules()).hasSize(5);
		assertThat(languageConfiguration.getSurroundingPairs()).hasSize(6);
		assertThat(languageConfiguration.getFolding()).isNotNull();
	}

	@Test
	void testCanLoadRustLanguageConfig() throws Exception {
		final var languageConfiguration = loadLanguageConfigFromClassPath("/rust-language-configuration.json");
		final var comments = castNonNull(languageConfiguration.getComments());
		assertThat(comments.blockComment).isNotNull();
		assertThat(comments.lineComment).isEqualTo("//");
		assertThat(languageConfiguration.getBrackets()).hasSize(4);
		assertThat(languageConfiguration.getAutoClosingPairs()).hasSize(6);
		assertThat(languageConfiguration.getAutoCloseBefore()).isNull();
		assertThat(languageConfiguration.getWordPattern()).isNull();
		assertThat(languageConfiguration.getOnEnterRules()).hasSize(6);
		assertThat(languageConfiguration.getSurroundingPairs()).hasSize(6);
		assertThat(languageConfiguration.getFolding()).isNull();
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
						assertThat(languageConfiguration).isNotNull();
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		System.out.println("Successfully parsed " + count.intValue() + " language configurations.");
		assertThat(count.intValue()).isGreaterThan(10);
	}

	@Test
	void testParseColorizedBracketsPair() {
		final var languageConfiguration = loadLanguageConfigFromString("""
			{
				"colorizedBracketPairs": [
					["(",")"],
					["[","]"],
					["{","}"],
					["<",">"]
				],
			}""");

		assertThat(languageConfiguration.getColorizedBracketPairs()).hasSize(4);
		assertThat(languageConfiguration.getColorizedBracketPairs().get(0).open).isEqualTo("(");
		assertThat(languageConfiguration.getColorizedBracketPairs().get(0).close).isEqualTo(")");
	}

	@Test
	void testIndentationRules() {
		final var languageConfiguration = loadLanguageConfigFromString("""
			{
				"indentationRules": {
					"increaseIndentPattern": "(^.*\\\\{[^}]*$)",
					"decreaseIndentPattern": "^\\\\s*\\\\}"
				},
			}""");

		final var indentationRules = languageConfiguration.getIndentationRules();
		assert indentationRules != null;

		assertThat(indentationRules.increaseIndentPattern).hasToString("(^.*\\{[^}]*$)");
		assertThat(indentationRules.decreaseIndentPattern).hasToString("^\\s*\\}");
	}
}
