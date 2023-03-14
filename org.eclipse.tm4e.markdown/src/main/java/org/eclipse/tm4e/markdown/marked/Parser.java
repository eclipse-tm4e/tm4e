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

import com.google.common.base.Strings;

public class Parser {

	private final Options options;
	private final IRenderer renderer;

	@Nullable
	private Token token;

	public Parser(@Nullable final Options options, @Nullable final IRenderer renderer) {
		this.options = options != null ? options : Options.DEFAULTS;
		this.renderer = renderer != null ? renderer : new HTMLRenderer();
	}

	public static IRenderer parse(final Tokens src, @Nullable final Options options,
			@Nullable final IRenderer renderer) {
		final var parser = new Parser(options, renderer);
		return parser.parse(src);
	}

	private IRenderer parse(final Tokens src) {
		final var inline = new InlineLexer(src.links, this.options, this.renderer);
		final var tokens = src.reverse();

		// var out = '';
		while (this.next(tokens)) {
			// out += this.tok();
			this.tok(inline);
		}

		return renderer;
	}

	/**
	 * Next Token
	 */
	private boolean next(final Tokens tokens) {
		return (this.token = tokens.pop()) != null;
	}

	/**
	 * Parse Current Token
	 */
	private void tok(final InlineLexer inline) {
		final var token = this.token;
		if (token == null)
			return;

		switch (token.type) {
			case space:
				break;
			case hr:
				this.renderer.hr();
				break;
			case heading:
				// this.renderer.heading(this.inline.output(token.text), token.depth, token.text);
				this.renderer.heading(Strings.nullToEmpty(token.text), token.depth, Strings.nullToEmpty(token.text));
				break;
			case code:
				this.renderer.code(Strings.nullToEmpty(token.text), token.lang, token.escaped);
				break;
			case paragraph:
				this.renderer.startParagraph();
				inline.output(Strings.nullToEmpty(token.text));
				this.renderer.endParagraph();
				break;
		}
	}
}
