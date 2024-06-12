/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
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
package org.eclipse.tm4e.core.internal.theme.raw;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.junit.jupiter.api.Test;

class RawThemeReaderTest {

	@Test
	@NonNullByDefault({})
	void testLoadingThemes() throws IOException {
		final var count = new AtomicInteger();
		try (final var files = Files.list(Paths.get("../org.eclipse.tm4e.core.tests/src/main/resources/test-cases/themes"))) {
			files.forEach(file -> {
				final var fileName = file.getFileName().toString();
				if (fileName.endsWith(".json") && (fileName.contains("light") || fileName.contains("dark") || fileName.contains("black"))
						|| fileName.endsWith(".tmTheme")) {
					System.out.println("Parsing [" + file + "]...");
					try {
						final IRawTheme rawTheme = RawThemeReader.readTheme(IThemeSource.fromFile(file));
						count.incrementAndGet();
						assertFalse(castNonNull(rawTheme.getName()).isEmpty());
						assertFalse(castNonNull(rawTheme.getSettings()).isEmpty());
						for (final var setting : castNonNull(rawTheme.getSettings())) {
							assertNotNull(setting.getSetting());
						}
						final var theme = Theme.createFromRawTheme(rawTheme, null);
						assertFalse(theme.getColorMap().isEmpty());
						assertNotNull(theme.getDefaults());
					} catch (final Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			});
		}
		System.out.println("Successfully parsed " + count.intValue() + " themes.");
		assertTrue(count.intValue() > 10, "Only " + count.intValue() + " themes found, expected more than 10!");
	}
}
