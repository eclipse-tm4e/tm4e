/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.internal.grammar.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.tm4e.core.internal.grammar.RawGrammar;
import org.eclipse.tm4e.core.internal.grammar.RawRule;
import org.eclipse.tm4e.core.internal.parser.PListParser;
import org.eclipse.tm4e.core.internal.parser.PListParserXML;
import org.eclipse.tm4e.core.internal.parser.PListPath;
import org.eclipse.tm4e.core.internal.parser.PropertySettable;
import org.junit.jupiter.api.Test;

public class FooTest {

	private static final PropertySettable.Factory<PListPath> OBJECT_FACTORY = path -> {
		switch (path.size()) {
		case 0:
			return new RawGrammar();
		case 1:
			switch (path.last()) {
			case "repository":
				return new RawRule();
			}
		}
		return new RawRule();
	};


	@Test
	void testParseXMLPlist() throws Exception {
		final PListParser<RawGrammar> parser = new PListParserXML<>(OBJECT_FACTORY);
		try (final var is = FooTest.class.getResourceAsStream("Foo.xml")) {
			final var grammar = parser.parse(is);
			assertNotNull(grammar);
			System.out.println(grammar);
		}
	}
}
