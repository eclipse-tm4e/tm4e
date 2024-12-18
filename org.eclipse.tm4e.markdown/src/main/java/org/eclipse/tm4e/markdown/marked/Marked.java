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

public final class Marked {

	public static IRenderer parse(final String src) {
		return parse(src, (Options) null);
	}

	public static IRenderer parse(final String src, final IRenderer renderer) {
		return parse(src, null, renderer);
	}

	public static IRenderer parse(final String src, final @Nullable Options opt) {
		return parse(src, opt, null);
	}

	public static IRenderer parse(final String src, final @Nullable Options opt, final @Nullable IRenderer renderer) {
		return Parser.parse(Lexer.lex(src, opt), opt, renderer);
	}

	private Marked() {
	}
}
