/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke - initial implementation
 */
package org.eclipse.tm4e.core.grammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.google.common.base.Splitter;

public final class TokenizationUtils {

	private static final Splitter BY_LINE_SPLITTER = Splitter.onPattern("\\r?\\n");

	/**
	 * Lazy tokenizes the given text.
	 *
	 * @param text the text to tokenize
	 *
	 * @return The stream of {@link ITokenizeLineResult}, each item covering 1 line of the text
	 */
	public static Stream<ITokenizeLineResult<IToken[]>> tokenizeText(final CharSequence text, final IGrammar grammar) {
		if (text.length() == 0) {
			return Stream.empty();
		}

		final var prevStack = new AtomicReference<IStateStack>();
		return BY_LINE_SPLITTER.splitToStream(text).map(line -> {
			final var tokenized = grammar.tokenizeLine(line, prevStack.get(), null);
			prevStack.set(tokenized.getRuleStack());
			return tokenized;
		});
	}

	/**
	 * Lazy tokenizes the text provided by the given input stream.
	 *
	 * @param text the text to tokenize
	 *
	 * @return The stream of {@link ITokenizeLineResult}, each item covering 1 line of the text
	 *
	 * @throws IOException
	 */
	public static Stream<ITokenizeLineResult<IToken[]>> tokenizeText(final InputStream text, final IGrammar grammar) throws IOException {
		final var reader = new BufferedReader(new InputStreamReader(text));

		final var prevStack = new AtomicReference<IStateStack>();
		return reader.lines().map(line -> {
			final var tokenized = grammar.tokenizeLine(line, prevStack.get(), null);
			prevStack.set(tokenized.getRuleStack());
			return tokenized;
		});
	}

	private TokenizationUtils() {
	}
}
