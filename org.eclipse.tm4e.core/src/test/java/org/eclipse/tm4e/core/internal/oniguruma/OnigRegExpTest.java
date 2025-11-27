/**
 * Copyright (c) 2022,2024 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.oniguruma;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

class OnigRegExpTest {

	private void assertOnigRegExpSearch(final String input, final @Nullable OnigResult result, final int startPosition,
			final boolean shouldMatch, final String... expectedGroups) {
		if (shouldMatch) {
			assertThat(result)
					.withFailMessage("Expected a match in input: \"%s\" starting at position %d", input, startPosition)
					.isNotNull();
			assert result != null; // only to help Eclipse compiler
			assertThat(result.count())
					.withFailMessage("Expected %d groups, but found %d in input: \"%s\"", expectedGroups.length, result.count(), input)
					.isEqualTo(expectedGroups.length);

			for (int i = 0; i < expectedGroups.length; i++) {
				final String expectedGroup = expectedGroups[i];
				final int start = result.locationAt(i);
				final int end = start + result.lengthAt(i);
				final String actualGroup = input.substring(start, end);
				assertThat(actualGroup)
						.withFailMessage("Expected group %d to be \"%s\" but found \"%s\" in input: \"%s\"", i, expectedGroup, actualGroup,
								input)
						.isEqualTo(expectedGroup);
			}
		} else {
			assertThat(result)
					.withFailMessage("Did not expect a match in input: \"%s\" starting at position %d", input, startPosition)
					.isNull();
		}
	}

	private void assertOnigRegExpSearch(final String pattern, final String input, final int startPosition, final boolean shouldMatch,
			final String... expectedGroups) {
		final OnigRegExp regexp = new OnigRegExp(pattern);
		final OnigResult result = regexp.search(OnigString.of(input), startPosition);
		assertOnigRegExpSearch(input, result, startPosition, shouldMatch, expectedGroups);
	}

	@Test
	void testOnigRegExp() {
		assertOnigRegExpSearch(
				"\\G(MAKEFILES|VPATH|SHELL|MAKESHELL|MAKE|MAKELEVEL|MAKEFLAGS|MAKECMDGOALS|CURDIR|SUFFIXES|\\.LIBPATTERNS)(?=\\s*\\))",
				"ifeq (version,$(firstword $(MAKECMDGOALS))\n",
				28,
				true,
				"MAKECMDGOALS",
				"MAKECMDGOALS");
	}

	/**
	 * Tests that no caching is performed if the regexp contains a \G anchor
	 */
	@Test
	void testOnigRegExpCaching() {
		final var regexp = new OnigRegExp(
				"\\G(MAKEFILES|VPATH|SHELL|MAKESHELL|MAKE|MAKELEVEL|MAKEFLAGS|MAKECMDGOALS|CURDIR|SUFFIXES|\\.LIBPATTERNS)(?=\\s*\\))");

		final var line = "ifeq (version,$(firstword $(MAKECMDGOALS))\n";
		final var onigLine = OnigString.of(line);

		var result = regexp.search(onigLine, 10);
		assertThat(result)
				.withFailMessage("Did not expect a match in input: \"%s\" starting at position %d", line, 10)
				.isNull();

		result = regexp.search(onigLine, 28);
		assertOnigRegExpSearch(line, result, 28, true, "MAKECMDGOALS", "MAKECMDGOALS");
	}

	@Test
	void testNegativeLookBehinds() {
		// test of OnigRegExp.rewritePatternIfRequired (lookbehind1)
		assertOnigRegExpSearch("(?<!\\.\\s*)\\b(await)\\b", "await", 0, true, "await", "await");
		assertOnigRegExpSearch("(?<!\\.\\s*)\\b(await)\\b", "  await", 0, true, "  await", "await");
		assertOnigRegExpSearch("(?<!\\.\\s*)\\b(await)\\b", ".await", 0, false);
		assertOnigRegExpSearch("(?<!\\.\\s*)\\b(await)\\b", "  .await", 0, false);

		// test of OnigRegExp.rewritePatternIfRequired (lookbehind2)
		assertOnigRegExpSearch("(?<=^\\s*)\\\\fi", "\\fi", 0, true, "\\fi");
		assertOnigRegExpSearch("(?<=^\\s*)\\\\fi", "  \\fi", 0, true, "  \\fi");

		// test of OnigRegExp.rewritePatternIfRequired (lookbehind3)
		assertOnigRegExpSearch("(?<=\\s*\\.)\\w+", ".foo", 0, true, ".foo");
		assertOnigRegExpSearch("(?<=\\s*\\.)\\w+", "  .foo", 0, true, "  .foo");
	}
}
