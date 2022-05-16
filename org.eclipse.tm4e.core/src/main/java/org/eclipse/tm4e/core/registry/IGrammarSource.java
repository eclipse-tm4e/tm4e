/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public interface IGrammarSource {

	static IGrammarSource fromFile(final Path file) {
		return new IGrammarSource() {
			@Override
			public Reader getReader() throws IOException {
				return Files.newBufferedReader(file);
			}

			@Override
			public String getFilePath() {
				return file.toAbsolutePath().toString();
			}
		};
	}

	static IGrammarSource fromResource(final Class<?> clazz, final String resourceName) {
		return new IGrammarSource() {
			@Override
			public BufferedReader getReader() throws IOException {
				return new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(resourceName)));
			}

			@Override
			public String getFilePath() {
				return resourceName;
			}
		};
	}

	String getFilePath();

	Reader getReader() throws IOException;
}
