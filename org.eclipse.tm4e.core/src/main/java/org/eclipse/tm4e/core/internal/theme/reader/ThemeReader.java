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
package org.eclipse.tm4e.core.internal.theme.reader;

import java.io.InputStream;

import org.eclipse.tm4e.core.internal.parser.PListParser;
import org.eclipse.tm4e.core.internal.parser.PListParserJSON;
import org.eclipse.tm4e.core.internal.parser.PListParserXML;
import org.eclipse.tm4e.core.internal.parser.PListParserYAML;
import org.eclipse.tm4e.core.internal.theme.ThemeRaw;
import org.eclipse.tm4e.core.theme.IRawTheme;

/**
 * TextMate Theme reader utilities.
 *
 */
public final class ThemeReader {

	/**
	 * Helper class, use methods statically
	 */
	private ThemeReader() {
	}

	private static final PListParserJSON<IRawTheme> JSON_PARSER = new PListParserJSON<>(ThemeRaw::new);
	private static final PListParserYAML<IRawTheme> YAML_PARSER = new PListParserYAML<>(ThemeRaw::new);
	private static final PListParserXML<IRawTheme> XML_PARSER = new PListParserXML<>(ThemeRaw::new);

	public static IRawTheme readThemeSync(final String filePath, final InputStream in) throws Exception {
		final var reader = new SyncThemeReader(in, getThemeParser(filePath));
		return reader.load();
	}

	private static PListParser<IRawTheme> getThemeParser(final String filePath) {
		String extension = filePath.substring(filePath.lastIndexOf('.') + 1).trim().toLowerCase();

		switch (extension) {

		case "json":
			return JSON_PARSER;

		case "yaml":
		case "yml":
			return YAML_PARSER;

		default:
			return XML_PARSER;
		}
	}
}
