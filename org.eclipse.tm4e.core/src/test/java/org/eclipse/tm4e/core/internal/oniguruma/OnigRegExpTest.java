/*
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.oniguruma;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnigRegExpTest {

	@Test
	void testOnigRegExp() throws Exception {
		final var regexp = new OnigRegExp(
				"\\G(MAKEFILES|VPATH|SHELL|MAKESHELL|MAKE|MAKELEVEL|MAKEFLAGS|MAKECMDGOALS|CURDIR|SUFFIXES|\\.LIBPATTERNS)(?=\\s*\\))");

		final var line = "ifeq (version,$(firstword $(MAKECMDGOALS))\n";
		final var onigLine = OnigString.of(line);

		final var result = regexp.search(onigLine, 28);
		assertNotNull(result);
		assertEquals(2, result.count());
		assertEquals("MAKECMDGOALS", line.substring(result.locationAt(0), result.locationAt(0) + result.lengthAt(0)));
		assertEquals("MAKECMDGOALS", line.substring(result.locationAt(1), result.locationAt(1) + result.lengthAt(1)));
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
		assertNull(result);

		result = regexp.search(onigLine, 28);
		assertNotNull(result);
		assertEquals(2, result.count());
		assertEquals("MAKECMDGOALS", line.substring(result.locationAt(0), result.locationAt(0) + result.lengthAt(0)));
		assertEquals("MAKECMDGOALS", line.substring(result.locationAt(1), result.locationAt(1) + result.lengthAt(1)));
	}
}
