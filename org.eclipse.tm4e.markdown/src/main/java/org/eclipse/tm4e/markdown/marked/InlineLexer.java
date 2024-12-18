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

import static org.eclipse.tm4e.markdown.marked.Helpers.htmlEscape;

import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.utils.StringUtils;

public class InlineLexer {

	private final Options options;
	private final InlineRules rules;
	private final IRenderer renderer;

	public InlineLexer(final @Nullable Object links, final @Nullable Options options, final @Nullable IRenderer renderer) {
		this.options = options != null ? options : Options.DEFAULTS;
		// this.links = links;
		this.renderer = renderer != null ? renderer : new HTMLRenderer();
		// this.renderer = this.options.renderer || new Renderer;
		// this.renderer.options = this.options;

		// if (!this.links) {
		// throw new
		// Error('Tokens array requires a `links` property.');
		// }

		if (this.options.isGfm()) {
			if (this.options.isBreaks()) {
				this.rules = InlineRules.breaks;
			} else {
				this.rules = InlineRules.gfm;
			}
		} else if (this.options.isPedantic()) {
			this.rules = InlineRules.pedantic;
		} else {
			this.rules = InlineRules.normal;
		}
	}

	public void output(String src) {
		Matcher cap = null;
		while (!StringUtils.isNullOrEmpty(src)) {

			// strong
			if ((cap = this.rules.strong.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				this.renderer.startStrong();
				this.output(!StringUtils.isNullOrEmpty(cap.group(2)) ? cap.group(2) : cap.group(1));
				this.renderer.endStrong();
				continue;
			}

			// em
			if ((cap = this.rules.em.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				this.renderer.startEm();
				this.output(!StringUtils.isNullOrEmpty(cap.group(2)) ? cap.group(2) : cap.group(1));
				this.renderer.endEm();
				continue;
			}

			// code
			if ((cap = this.rules.code.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				this.renderer.codespan(htmlEscape(cap.group(2), true));
				continue;
			}

			// text
			if ((cap = this.rules.text.exec(src)) != null) {
				src = src.substring(cap.group(0).length());
				this.renderer.text(htmlEscape(this.smartypants(cap.group(0))));
				continue;
			}
		}
	}

	private String smartypants(final String text) {
		return text;
	}

}
