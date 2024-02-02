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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		var cursorCfg = new CursorConfiguration(false, 4);

		assertEquals("\t", cursorCfg.normalizeIndentation("\t"));
		assertEquals("\t", cursorCfg.normalizeIndentation("    "));
		assertEquals("   ", cursorCfg.normalizeIndentation("   "));
		assertEquals("  ", cursorCfg.normalizeIndentation("  "));
		assertEquals(" ", cursorCfg.normalizeIndentation(" "));
		assertEquals("", cursorCfg.normalizeIndentation(""));
		assertEquals("\t\t", cursorCfg.normalizeIndentation(" \t    "));
		assertEquals("\t   ", cursorCfg.normalizeIndentation(" \t   "));
		assertEquals("\t  ", cursorCfg.normalizeIndentation(" \t  "));
		assertEquals("\t ", cursorCfg.normalizeIndentation(" \t "));
		assertEquals("\t", cursorCfg.normalizeIndentation(" \t"));

		assertEquals("\ta", cursorCfg.normalizeIndentation("\ta"));
		assertEquals("\ta", cursorCfg.normalizeIndentation("    a"));
		assertEquals("   a", cursorCfg.normalizeIndentation("   a"));
		assertEquals("  a", cursorCfg.normalizeIndentation("  a"));
		assertEquals(" a", cursorCfg.normalizeIndentation(" a"));
		assertEquals("a", cursorCfg.normalizeIndentation("a"));
		assertEquals("\t\ta", cursorCfg.normalizeIndentation(" \t    a"));
		assertEquals("\t   a", cursorCfg.normalizeIndentation(" \t   a"));
		assertEquals("\t  a", cursorCfg.normalizeIndentation(" \t  a"));
		assertEquals("\t a", cursorCfg.normalizeIndentation(" \t a"));
		assertEquals("\ta", cursorCfg.normalizeIndentation(" \ta"));
	}

	@Test
	void testNormalizeIndentation2() {
		var cursorCfg = new CursorConfiguration(true, 4);

		assertEquals("    a", cursorCfg.normalizeIndentation("\ta"));
		assertEquals("    a", cursorCfg.normalizeIndentation("    a"));
		assertEquals("   a", cursorCfg.normalizeIndentation("   a"));
		assertEquals("  a", cursorCfg.normalizeIndentation("  a"));
		assertEquals(" a", cursorCfg.normalizeIndentation(" a"));
		assertEquals("a", cursorCfg.normalizeIndentation("a"));
		assertEquals("        a", cursorCfg.normalizeIndentation(" \t    a"));
		assertEquals("       a", cursorCfg.normalizeIndentation(" \t   a"));
		assertEquals("      a", cursorCfg.normalizeIndentation(" \t  a"));
		assertEquals("     a", cursorCfg.normalizeIndentation(" \t a"));
		assertEquals("    a", cursorCfg.normalizeIndentation(" \ta"));
	}
}
