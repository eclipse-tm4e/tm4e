/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/chjj/marked/
 * Initial copyright Copyright (c) 2011-2014, Christopher Jeffrey and others
 * Initial license: MIT
 *
 * Contributors:
 * - Christopher Jeffrey and others: Initial code, written in JavaScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.markdown.marked;

import org.eclipse.jdt.annotation.Nullable;

public class Token {

	public final TokenType type;
	public final @Nullable String text;
	public final int depth;
	public final @Nullable String lang;
	public final boolean escaped;

	public Token(final TokenType type) {
		this(type, null);
	}

	public Token(final TokenType type, final @Nullable String text) {
		this(type, text, -1);
	}

	public Token(final TokenType type, final @Nullable String text, final int depth) {
		this(type, text, depth, null, false);
	}

	public Token(final TokenType type, final String lang, final String text) {
		this(type, text, -1, lang, false);
	}

	private Token(final TokenType type, final @Nullable String text, final int depth, final @Nullable String lang,
			final boolean escaped) {
		this.type = type;
		this.text = text;
		this.depth = depth;
		this.lang = lang;
		this.escaped = escaped;
	}
}
