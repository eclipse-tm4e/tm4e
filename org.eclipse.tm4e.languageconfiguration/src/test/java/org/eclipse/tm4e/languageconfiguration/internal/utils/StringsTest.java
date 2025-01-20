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
import static org.eclipse.tm4e.languageconfiguration.internal.utils.Strings.*;

import org.junit.jupiter.api.Test;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/base/test/common/strings.test.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/base/test/common/strings.test.ts</a>
 */
class StringsTest {

	@Test
	void testLastNonWhitespaceIndex() {
		assertThat(lastNonWhitespaceIndex("abc  \t \t ")).isEqualTo(2);
		assertThat(lastNonWhitespaceIndex("abc")).isEqualTo(2);
		assertThat(lastNonWhitespaceIndex("abc\t")).isEqualTo(2);
		assertThat(lastNonWhitespaceIndex("abc ")).isEqualTo(2);
		assertThat(lastNonWhitespaceIndex("abc  \t \t ")).isEqualTo(2);
		assertThat(lastNonWhitespaceIndex("abc  \t \t abc \t \t ")).isEqualTo(11);
		assertThat(lastNonWhitespaceIndex("abc  \t \t abc \t \t ", 8)).isEqualTo(2);
		assertThat(lastNonWhitespaceIndex("  \t \t ")).isEqualTo(-1);
	}

	@Test
	void testGetLeadingWhitespace() {
		assertThat(getLeadingWhitespace("  foo")).isEqualTo("  ");
		assertThat(getLeadingWhitespace("  foo", 2)).isEqualTo("");
		assertThat(getLeadingWhitespace("  foo", 1, 1)).isEqualTo("");
		assertThat(getLeadingWhitespace("  foo", 0, 1)).isEqualTo(" ");
		assertThat(getLeadingWhitespace("  ")).isEqualTo("  ");
		assertThat(getLeadingWhitespace("  ", 1)).isEqualTo(" ");
		assertThat(getLeadingWhitespace("  ", 0, 1)).isEqualTo(" ");
		assertThat(getLeadingWhitespace("\t\tfunction foo(){", 0, 1)).isEqualTo("\t");
		assertThat(getLeadingWhitespace("\t\tfunction foo(){", 0, 2)).isEqualTo("\t\t");
	}
}
