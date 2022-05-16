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
import java.io.InputStreamReader;

import org.eclipse.tm4e.core.internal.parser.PListParser;
import org.eclipse.tm4e.core.internal.theme.IRawTheme;

final class SyncThemeReader {

	private final InputStream in;
	private final PListParser<? extends IRawTheme> parser;

	SyncThemeReader(final InputStream in, final PListParser<? extends IRawTheme> parser) {
		this.in = in;
		this.parser = parser;
	}

	IRawTheme load() throws Exception {
		return this.parser.parse(new InputStreamReader(in));
	}
}
