/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Sebastian Thomschke (Vegard IT) - translation and adaptation to Java
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.tm4e.languageconfiguration.internal.model.CursorConfiguration;
import org.junit.jupiter.api.Test;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/test/common/model/textModel.test.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/test/common/model/textModel.test.ts</a>
 */
class IndentationTest {

	@Test
	void testNormalizeIndentation1() {
		final var cursorCfg = new CursorConfiguration(false, 4);

		assertThat(cursorCfg.normalizeIndentation("\t")).isEqualTo("\t");
		assertThat(cursorCfg.normalizeIndentation("    ")).isEqualTo("\t");
		assertThat(cursorCfg.normalizeIndentation("   ")).isEqualTo("   ");
		assertThat(cursorCfg.normalizeIndentation("  ")).isEqualTo("  ");
		assertThat(cursorCfg.normalizeIndentation(" ")).isEqualTo(" ");
		assertThat(cursorCfg.normalizeIndentation("")).isEqualTo("");
		assertThat(cursorCfg.normalizeIndentation(" \t    ")).isEqualTo("\t\t");
		assertThat(cursorCfg.normalizeIndentation(" \t   ")).isEqualTo("\t   ");
		assertThat(cursorCfg.normalizeIndentation(" \t  ")).isEqualTo("\t  ");
		assertThat(cursorCfg.normalizeIndentation(" \t ")).isEqualTo("\t ");
		assertThat(cursorCfg.normalizeIndentation(" \t")).isEqualTo("\t");

		assertThat(cursorCfg.normalizeIndentation("\ta")).isEqualTo("\ta");
		assertThat(cursorCfg.normalizeIndentation("    a")).isEqualTo("\ta");
		assertThat(cursorCfg.normalizeIndentation("   a")).isEqualTo("   a");
		assertThat(cursorCfg.normalizeIndentation("  a")).isEqualTo("  a");
		assertThat(cursorCfg.normalizeIndentation(" a")).isEqualTo(" a");
		assertThat(cursorCfg.normalizeIndentation("a")).isEqualTo("a");
		assertThat(cursorCfg.normalizeIndentation(" \t    a")).isEqualTo("\t\ta");
		assertThat(cursorCfg.normalizeIndentation(" \t   a")).isEqualTo("\t   a");
		assertThat(cursorCfg.normalizeIndentation(" \t  a")).isEqualTo("\t  a");
		assertThat(cursorCfg.normalizeIndentation(" \t a")).isEqualTo("\t a");
		assertThat(cursorCfg.normalizeIndentation(" \ta")).isEqualTo("\ta");
	}

	@Test
	void testNormalizeIndentation2() {
		final var cursorCfg = new CursorConfiguration(true, 4);

		assertThat(cursorCfg.normalizeIndentation("\ta")).isEqualTo("    a");
		assertThat(cursorCfg.normalizeIndentation("    a")).isEqualTo("    a");
		assertThat(cursorCfg.normalizeIndentation("   a")).isEqualTo("   a");
		assertThat(cursorCfg.normalizeIndentation("  a")).isEqualTo("  a");
		assertThat(cursorCfg.normalizeIndentation(" a")).isEqualTo(" a");
		assertThat(cursorCfg.normalizeIndentation("a")).isEqualTo("a");
		assertThat(cursorCfg.normalizeIndentation(" \t    a")).isEqualTo("        a");
		assertThat(cursorCfg.normalizeIndentation(" \t   a")).isEqualTo("       a");
		assertThat(cursorCfg.normalizeIndentation(" \t  a")).isEqualTo("      a");
		assertThat(cursorCfg.normalizeIndentation(" \t a")).isEqualTo("     a");
		assertThat(cursorCfg.normalizeIndentation(" \ta")).isEqualTo("    a");
	}
}
