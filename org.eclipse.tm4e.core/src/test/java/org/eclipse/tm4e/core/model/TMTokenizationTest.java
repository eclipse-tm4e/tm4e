/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.model;

import static org.eclipse.tm4e.core.registry.IGrammarSource.fromResource;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Collectors;

import org.eclipse.tm4e.core.Data;
import org.eclipse.tm4e.core.internal.utils.ResourceUtils;
import org.eclipse.tm4e.core.registry.Registry;
import org.junit.jupiter.api.Test;

class TMTokenizationTest {

	@Test
	void testTokenizeWithTimeout() throws IOException {
		final var grammar = new Registry().addGrammar(fromResource(Data.class, "TypeScript.tmLanguage.json"));

		final var tokenizer = new TMTokenizationSupport(grammar);
		try (var reader = ResourceUtils.getResourceReader(Data.class, "raytracer.ts")) {
			final String veryLongLine = reader.lines().collect(Collectors.joining());
			final var result1 = tokenizer.tokenize(veryLongLine, null);
			assertFalse(result1.stoppedEarly);

			final var result2 = tokenizer.tokenize(veryLongLine, null, null, Duration.ofMillis(10));
			assertTrue(result2.stoppedEarly);

			assertNotEquals(result1.tokens.size(), result2.tokens.size());
		}
	}
}
