/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
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
 *  - Christopher Jeffrey and others: Initial code, written in JavaScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.markdown.marked;

public class Parser {

	private Tokens tokens;
	private Token token;
	private final Options options;
	private InlineLexer inline;
	private final IRenderer renderer;

	public Parser(Options options, IRenderer renderer) {
		this.tokens = new Tokens();
		this.token = null;
		this.options = options != null ? options : Options.DEFAULTS;
		this.renderer = renderer != null ? renderer : new HTMLRenderer();
	}

	public static IRenderer parse(Tokens src, Options options, IRenderer renderer) {
		Parser parser = new Parser(options, renderer);
		return parser.parse(src);
	}

	private IRenderer parse(Tokens src) {
		this.inline = new InlineLexer(src.links, this.options, this.renderer);
		this.tokens = src.reverse();

		// var out = '';
		while (this.next()) {
			// out += this.tok();
			this.tok();
		}

		return renderer;
	}

	/**
	 * Next Token
	 */
	private boolean next() {
		return ((this.token = this.tokens.pop()) != null);
	}

	/**
	 * Parse Current Token
	 */
	private void tok() {
		switch (this.token.type) {
		case space:
			break;
		case hr:
			this.renderer.hr();
			break;
		case heading:
			// this.renderer.heading(this.inline.output(this.token.text),
			// this.token.depth, this.token.text);
			this.renderer.heading(this.token.text, this.token.depth, this.token.text);
			break;
		case code:
			this.renderer.code(this.token.text, this.token.lang, this.token.escaped);
			break;
		case paragraph:
			this.renderer.startParagraph();
			this.inline.output(this.token.text);
			this.renderer.endParagraph();
			break;
		}

	}

}
