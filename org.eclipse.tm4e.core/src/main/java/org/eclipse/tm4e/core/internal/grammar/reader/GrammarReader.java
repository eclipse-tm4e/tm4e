/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/Microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.grammar.reader;

import java.io.InputStream;

import org.eclipse.tm4e.core.internal.grammar.Raw;
import org.eclipse.tm4e.core.internal.parser.PListParser;
import org.eclipse.tm4e.core.internal.parser.PListParserJSON;
import org.eclipse.tm4e.core.internal.parser.PListParserXML;
import org.eclipse.tm4e.core.internal.parser.PListParserYAML;
import org.eclipse.tm4e.core.internal.types.IRawGrammar;

/**
 * TextMate Grammar reader utilities.
 */
public final class GrammarReader {

	/**
	 * methods should be accessed statically
	 */
	private GrammarReader() {
	}

	private static final PListParser<IRawGrammar> JSON_PARSER = new PListParserJSON<>(Raw::new);
	private static final PListParser<IRawGrammar> XML_PARSER = new PListParserXML<>(Raw::new);
	private static final PListParser<IRawGrammar> YAML_PARSER = new PListParserYAML<>(Raw::new);

	public static IRawGrammar readGrammarSync(final String filePath, final InputStream in) throws Exception {
		return getGrammarParser(filePath).parse(in);
	}

	private static PListParser<IRawGrammar> getGrammarParser(final String filePath) {
		String extension = filePath.substring(filePath.lastIndexOf('.') + 1).trim().toLowerCase();

		switch (extension) {

		case "json":
			return JSON_PARSER;

		case "yaml":
		case "yaml-tmlanguage":
		case "yml":
			return YAML_PARSER;

		default:
			return XML_PARSER;
		}
	}
}
