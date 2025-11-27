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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.junit.jupiter.api.Test;

class RawThemeReaderTest {

	@Test
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
						assertThat(castNonNull(rawTheme.getName())).isNotEmpty();
						assertThat(castNonNull(rawTheme.getSettings())).isNotEmpty();

						for (final var setting : castNonNull(rawTheme.getSettings())) {
							assertThat(setting.getSetting()).isNotNull();
						}
						final var theme = Theme.createFromRawTheme(rawTheme, null);
						assertThat(theme.getColorMap()).isNotEmpty();
						assertThat(theme.getDefaults()).isNotNull();
					} catch (final Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			});
		}
		System.out.println("Successfully parsed " + count.intValue() + " themes.");
		assertThat(count.intValue())
				.withFailMessage("Only %d themes found, expected more than 10!", count.intValue())
				.isGreaterThan(10);
	}
}
