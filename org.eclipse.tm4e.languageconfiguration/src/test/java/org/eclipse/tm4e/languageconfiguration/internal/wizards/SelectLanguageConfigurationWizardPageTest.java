/**
 * Copyright (c) 2026 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.wizards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tm4e.languageconfiguration.internal.wizards.SelectLanguageConfigurationWizardPage.*;

import org.junit.jupiter.api.Test;

/**
 * Guards the file dialog filter configuration of {@link SelectLanguageConfigurationWizardPage}.
 * <p>
 * Which files the native dialog enables cannot be tested here, so this only checks the configuration that SWT
 * relies on across platforms.
 */
class SelectLanguageConfigurationWizardPageTest {

	@Test
	void testFileFilterNamesMatchExtensions() {
		// A shorter names array makes SWT on GTK fail with an index error once a file is chosen.
		assertThat(FILE_FILTER_NAMES).hasSameSizeAs(FILE_FILTER_EXTENSIONS);
		// NLS fills a missing properties key with a placeholder instead of null.
		assertThat(FILE_FILTER_NAMES).allSatisfy(name -> assertThat(name).isNotBlank().doesNotContain("NLS missing message"));
	}

	@Test
	void testFileFilterNamesAreNotDuplicatedOnWindows() {
		for (int i = 0; i < FILE_FILTER_NAMES.length; i++) {
			final String name = FILE_FILTER_NAMES[i];
			final String extensions = FILE_FILTER_EXTENSIONS[i];
			// When Explorer shows file extensions, Windows appends the filter to a label without "*.". SWT removes
			// " (<filter>)" from such a label first, so it may only show the filter if it shows all of it. Otherwise the
			// pattern text appears twice.
			if (!name.contains("*.") && name.contains("(")) {
				assertThat(name).endsWith(" (" + extensions + ")");
			}
		}
	}

	@Test
	void testFileFiltersKeepMacOsCompatiblePatterns() {
		// SWT on macOS cannot match the "*language-configuration.json" glob (#258, #989). Only these patterns make
		// language configuration files selectable there, so they must not be removed as redundant.
		assertThat(FILE_FILTER_EXTENSIONS[0].split(";")).contains("*.language-configuration.json", "language-configuration.json");
		// On macOS, hyphenated names such as "php-language-configuration.json" can only be selected with this filter.
		assertThat(FILE_FILTER_EXTENSIONS).contains("*.json");
	}

	@Test
	void testFileFilterPatternsAreWellFormed() {
		for (final String extensions : FILE_FILTER_EXTENSIONS) {
			// SWT on macOS trims each ";"-separated pattern but GTK does not, so spaces would break matching on Linux.
			assertThat(extensions.split(";", -1)).allSatisfy(pattern -> assertThat(pattern).isNotEmpty().isEqualTo(pattern.strip()));
		}
	}
}
