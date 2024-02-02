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

import static org.eclipse.tm4e.languageconfiguration.internal.utils.Strings.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/base/test/common/strings.test.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/base/test/common/strings.test.ts</a>
 */
class StringsTest {

	@Test
	void testLastNonWhitespaceIndex() {
		assertEquals(2, lastNonWhitespaceIndex("abc  \t \t "));
		assertEquals(2, lastNonWhitespaceIndex("abc"));
		assertEquals(2, lastNonWhitespaceIndex("abc\t"));
		assertEquals(2, lastNonWhitespaceIndex("abc "));
		assertEquals(2, lastNonWhitespaceIndex("abc  \t \t "));
		assertEquals(11, lastNonWhitespaceIndex("abc  \t \t abc \t \t "));
		assertEquals(2, lastNonWhitespaceIndex("abc  \t \t abc \t \t ", 8));
		assertEquals(-1, lastNonWhitespaceIndex("  \t \t "));
	}

	@Test
	void testGetLeadingWhitespace() {
		assertEquals("  ", getLeadingWhitespace("  foo"));
		assertEquals("", getLeadingWhitespace("  foo", 2));
		assertEquals("", getLeadingWhitespace("  foo", 1, 1));
		assertEquals(" ", getLeadingWhitespace("  foo", 0, 1));
		assertEquals("  ", getLeadingWhitespace("  "));
		assertEquals(" ", getLeadingWhitespace("  ", 1));
		assertEquals(" ", getLeadingWhitespace("  ", 0, 1));
		assertEquals("\t", getLeadingWhitespace("\t\tfunction foo(){", 0, 1));
		assertEquals("\t\t", getLeadingWhitespace("\t\tfunction foo(){", 0, 2));
	}
}
