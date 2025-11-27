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
package org.eclipse.tm4e.core.internal.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.grammar.raw.IRawRule;
import org.eclipse.tm4e.core.internal.grammar.raw.RawGrammar;
import org.eclipse.tm4e.core.internal.grammar.raw.RawGrammarReader;
import org.eclipse.tm4e.core.internal.oniguruma.OnigRegExp;
import org.eclipse.tm4e.core.internal.utils.ResourceUtils;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TMParserTest {

	private void validateCaptures(final RawGrammar grammar) {
		assertThat(grammar.getPatterns()).isNotNull();
		assertThat(castNonNull(grammar.getPatterns()).size()).isEqualTo(1);

		final var pattern = castNonNull(grammar.getPatterns()).iterator().next();
		assertThat(pattern.getName()).isEqualTo("THE_PATTERN");
		assertThat(pattern.getBegin()).isEqualTo("BEGIN_PATTERN");
		assertThat(pattern.getEnd()).isEqualTo("END_PATTERN");

		final var captures = castNonNull(pattern.getCaptures());
		assertThat(castNonNull(captures.getCapture("0")).getName()).isEqualTo("THE_CAPTURE");
	}

	@Test
	void testParseCapturesJSON() {
		// test capture defined as JSON map
		validateCaptures(TMParserJSON.INSTANCE.parse(new StringReader("""
			{"patterns": [{
				"name": "THE_PATTERN",
				"captures": {
					"0": { "name": "THE_CAPTURE" }
				},
				"begin": "BEGIN_PATTERN",
				"end": "END_PATTERN"
			}]}"""), RawGrammarReader.OBJECT_FACTORY));

		// test capture defined as JSON array
		validateCaptures(TMParserJSON.INSTANCE.parse(new StringReader("""
			{"patterns": [{
				"name": "THE_PATTERN",
				"captures": [
					{ "name": "THE_CAPTURE" }
				],
				"begin": "BEGIN_PATTERN",
				"end": "END_PATTERN"
			}]}"""), RawGrammarReader.OBJECT_FACTORY));
	}

	@Test
	void testParseCapturesPList() throws Exception {
		// test capture defined as PList dict
		validateCaptures(TMParserPList.INSTANCE.parse(new StringReader("""
			<plist version="1.0">
			<dict>
				<key>patterns</key>
				<array>
					<dict>
						<key>name</key>
						<string>THE_PATTERN</string>
						<key>captures</key>
						<dict>
							<key>0</key>
							<dict>
								<key>name</key>
								<string>THE_CAPTURE</string>
							</dict>
						</dict>
						<key>begin</key>
						<string>BEGIN_PATTERN</string>
						<key>end</key>
						<string>END_PATTERN</string>
					</dict>
				</array>
			</dict>
			</plist>"""), RawGrammarReader.OBJECT_FACTORY));

		// test capture defined as PList array
		validateCaptures(TMParserPList.INSTANCE.parse(new StringReader("""
			<plist version="1.0">
			<dict>
				<key>patterns</key>
				<array>
					<dict>
						<key>name</key>
						<string>THE_PATTERN</string>
						<key>captures</key>
						<array>
							<dict>
								<key>name</key>
								<string>THE_CAPTURE</string>
							</dict>
						</array>
						<key>begin</key>
						<string>BEGIN_PATTERN</string>
						<key>end</key>
						<string>END_PATTERN</string>
					</dict>
				</array>
			</dict>
			</plist>"""), RawGrammarReader.OBJECT_FACTORY));
	}

	@Test
	void testParseCapturesYAML() {
		// test capture defined as YAML map
		validateCaptures(TMParserYAML.INSTANCE.parse(new StringReader("""
			---
			patterns:
			- name: THE_PATTERN
			  captures:
			    0:
			      name: THE_CAPTURE
			  begin: "BEGIN_PATTERN"
			  end: "END_PATTERN"
			"""), RawGrammarReader.OBJECT_FACTORY));

		// test capture defined as YAML list
		validateCaptures(TMParserYAML.INSTANCE.parse(new StringReader("""
			---
			patterns:
			- name: THE_PATTERN
			  captures:
			    - name: THE_CAPTURE
			  begin: "BEGIN_PATTERN"
			  end: "END_PATTERN"
			"""), RawGrammarReader.OBJECT_FACTORY));
	}

	@Test
	void testParseJSON() throws Exception {
		try (var reader = ResourceUtils.getResourceReader(Data.class, "csharp.json")) {
			final var grammar = TMParserJSON.INSTANCE.parse(reader, RawGrammarReader.OBJECT_FACTORY);
			assertThat(grammar.getRepository()).isNotNull();
			assertThat(grammar.getFileTypes()).isNotEmpty();
			assertThat(grammar.getFileTypes()).containsExactly("cs");
			assertThat(grammar.getName()).isEqualTo("C#");
			assertThat(grammar.getScopeName()).isEqualTo("source.cs");
			assertThat(grammar.keySet())
					.isEqualTo(
							Set.of("fileTypes", "foldingStartMarker", "foldingStopMarker", "name", "patterns", "repository", "scopeName"));
		}
	}

	@Test
	void testParsePlist() throws Exception {
		try (var reader = ResourceUtils.getResourceReader(Data.class, "JavaScript.tmLanguage")) {
			final var grammar = TMParserPList.INSTANCE.parse(reader, RawGrammarReader.OBJECT_FACTORY);
			assertThat(grammar).isNotNull();
			assertThat(grammar.getRepository()).isNotNull();
			assertThat(grammar.getFileTypes()).isNotEmpty();
			assertThat(grammar.getFileTypes()).containsExactly("js", "jsx");
			assertThat(grammar.getName()).isEqualTo("JavaScript (with React support)");
			assertThat(grammar.getScopeName()).isEqualTo("source.js");
			assertThat(grammar.keySet())
					.isEqualTo(Set.of("fileTypes", "name", "patterns", "repository", "scopeName", "uuid"));
		}
	}

	@Test
	void testParseYAML() throws Exception {
		try (var reader = ResourceUtils.getResourceReader(Data.class, "JavaScript.tmLanguage.yaml")) {
			final var grammar = TMParserYAML.INSTANCE.parse(reader, RawGrammarReader.OBJECT_FACTORY);
			assertThat(grammar.getRepository()).isNotNull();
			assertThat(grammar.getFileTypes()).isNotEmpty();
			assertThat(grammar.getFileTypes()).containsExactly("js", "jsx");
			assertThat(grammar.getName()).isEqualTo("JavaScript (with React support)");
			assertThat(grammar.getScopeName()).isEqualTo("source.js");
			assertThat(grammar.keySet())
					.isEqualTo(Set.of("fileTypes", "name", "patterns", "repository", "scopeName", "uuid"));
		}
	}

	private void assertParseablePattern(final @Nullable String pattern) {
		if (pattern == null)
			return;
		try {
			assertThat(new OnigRegExp(pattern)).isNotNull();
		} catch (final RuntimeException ex) {
			final var msg = ex.getMessage();
			if (msg != null && msg.contains("invalid backref number/name")) {
				// ignore
			} else
				throw ex;
		}
	}

	private void assertParseablePatterns(final @Nullable Collection<IRawRule> patterns) {
		if (patterns == null || patterns.isEmpty())
			return;

		for (final var rule : patterns) {
			assertParseablePattern(rule.getBegin());
			assertParseablePattern(rule.getEnd());
			assertParseablePattern(rule.getMatch());
			assertParseablePattern(rule.getWhile());
			assertParseablePatterns(rule.getPatterns());
		}
	}

	@Test
	void testLanguagePackGrammars() throws IOException {
		final var count = new AtomicInteger();
		Files.walkFileTree(Paths.get("../org.eclipse.tm4e.language_pack"), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				final var fileName = file.getFileName().toString().toLowerCase();
				if (fileName.endsWith("tmlanguage.yaml")
						|| fileName.endsWith("tmlanguage.json")
						|| fileName.endsWith("plist")
						|| fileName.endsWith("tmlanguage")) {
					System.out.println("Parsing [" + file + "]...");
					try {
						final var rawGrammar = RawGrammarReader.readGrammar(IGrammarSource.fromFile(file));
						count.incrementAndGet();
						assertThat(rawGrammar.getScopeName()).isNotBlank();
						assertThat(rawGrammar.getFileTypes()).isNotNull();
						assertThat(rawGrammar.getRepository()).isNotNull();

						final var patterns = castNonNull(rawGrammar.getPatterns());
						assertThat(patterns).isNotEmpty();
						assertParseablePatterns(patterns);

						final var reg = new Registry();
						final var grammar = reg.addGrammar(IGrammarSource.fromFile(file));
						assertThat(grammar.getName()).isEqualTo(rawGrammar.getName());
						assertThat(grammar.getScopeName()).isEqualTo(rawGrammar.getScopeName());
						assertThat(grammar.getFileTypes()).isEqualTo(rawGrammar.getFileTypes());
						grammar.tokenizeLine("");
					} catch (final Exception ex) {
						throw new RuntimeException(ex);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		System.out.println("Successfully parsed " + count.intValue() + " grammars.");
		assertThat(count.intValue())
				.withFailMessage("Only %d grammars found, expected more than 10!", count.intValue())
				.isGreaterThan(10);
	}
}
