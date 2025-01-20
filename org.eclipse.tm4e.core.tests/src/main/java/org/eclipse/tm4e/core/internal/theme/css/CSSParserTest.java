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
package org.eclipse.tm4e.core.internal.theme.css;

import static org.assertj.core.api.Assertions.*;

import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.core.theme.css.CSSParser;
import org.junit.jupiter.api.Test;

class CSSParserTest {

	@Test
	void testCSSParser() throws Exception {
		final var parser = new CSSParser("""
			.invalid { background-color: rgb(255,128,128); }
			.storage.invalid { background-color: red; }
			""");

		assertThat(parser.getBestStyle("undefined")).isNull();
		assertThat(parser.getBestStyle("invalid").getBackgroundColor()).isEqualTo(new RGB(255, 128, 128));
		assertThat(parser.getBestStyle("storage", "invalid").getBackgroundColor()).isEqualTo(new RGB(255, 0, 0));
		assertThat(parser.getBestStyle("storage", "modifier", "invalid", "deprecated").getBackgroundColor()).isEqualTo(new RGB(255, 0, 0));
	}
}
