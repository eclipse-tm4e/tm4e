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

import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.utils.StringUtils;

public class Lexer {

	private final BlockRules rules;
	private final Tokens tokens;
	private final Options options;

	public Lexer(final @Nullable Options options) {
		this.tokens = new Tokens();
		this.options = options != null ? options : Options.DEFAULTS;

		if (this.options.isGfm()) {
			if (this.options.isTables()) {
				this.rules = BlockRules.tables;
			} else {
				this.rules = BlockRules.gfm;
			}
		} else {
			this.rules = BlockRules.normal;
		}
	}

	public static Tokens lex(final String src, final @Nullable Options options) {
		final var lexer = new Lexer(options);
		return lexer.lex(src);
	}

	private Tokens lex(String src) {
		src = src.replaceAll("\r\n|\r", "\n") //
				.replace("\t", "    ") //
				.replace("\u00a0", " ") //
				.replace("\u2424", "\n");
		return this.token(src, true);
	}

	private Tokens token(final String src, final boolean top) {
		return token(src, top, null);
	}

	private Tokens token(String src, final boolean top, final @Nullable Object bq) {
		src = src.replaceAll("^ +$", "");
		Matcher cap;
		while (!StringUtils.isNullOrEmpty(src)) {

			// newline
			if ((cap = this.rules.newline.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				if (cap.group(0).length() > 1) {
					this.tokens.add(new Token(TokenType.space));
				}
			}

			// code
			// if ((cap = this.rules.code.exec(src)) != null) {
			// src = src.substring(cap.group(0).length());
			// cap = cap.group(0).matches("^ {4}", "");
			// String text = !this.options.pedantic
			// ? cap.replace(/\n+$/, '')
			// : cap;
			// this.tokens.add(new Token(TokenType.type));
			// continue;
			// }

			// fences (gfm)
			if ((cap = this.rules.fences.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				final String lang = cap.group(2);
				final String text = !StringUtils.isNullOrEmpty(cap.group(3)) ? cap.group(3) : "";
				this.tokens.add(new Token(TokenType.code, lang, text));
				continue;
			}

			// heading
			if ((cap = this.rules.heading.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				final String text = cap.group(2);
				final int depth = cap.group(1).length();
				this.tokens.add(new Token(TokenType.heading, text, depth));
				continue;
			}

			// table no leading pipe (gfm)
			// TODO

			// lheading
			if ((cap = this.rules.lheading.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				final String text = cap.group(1);
				final int depth = cap.group(2).equals("=") ? 1 : 2;
				this.tokens.add(new Token(TokenType.heading, text, depth));
				continue;
			}

			// hr
			if ((cap = this.rules.hr.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				this.tokens.add(new Token(TokenType.hr));
				continue;
			}

			// top-level paragraph
			if (top && (cap = this.rules.paragraph.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				final String text = cap.group(1).charAt(cap.group(1).length() - 1) == '\n' ? cap.group(1) : cap.group(1);
				this.tokens.add(new Token(TokenType.paragraph, text));
				continue;
			}
		}
		return this.tokens;
	}

}
