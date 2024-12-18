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

import org.eclipse.jdt.annotation.Nullable;

public class HTMLRenderer implements IRenderer {

	protected final StringBuilder html;

	public HTMLRenderer() {
		this(new StringBuilder());
	}

	public HTMLRenderer(final StringBuilder html) {
		this.html = html;
	}

	@Override
	public void code(final String code, final @Nullable String lang, final boolean escaped) {
		if (lang == null) {
			html.append("<pre><code>");
			html.append(escaped ? code : htmlEscape(code, true));
			html.append("\n</code></pre>");
		} else {
			html.append("<pre><code>");
			html.append(escaped ? code : htmlEscape(code, true));
			html.append("\n</code></pre>");
		}
	}

	@Override
	public void blockquote(final String quote) {
		// TODO Auto-generated method stub

	}

	@Override
	public void html(final String html) {
		// TODO Auto-generated method stub

	}

	@Override
	public void heading(final String text, final int level, final String raw) {
		html.append("<h");
		html.append(level);
		html.append(" id=\"");
		html.append("\">");
		html.append(text);
		html.append("</h");
		html.append(level);
		html.append(">\n");
	}

	@Override
	public void hr() {
		html.append("<hr />\n");
	}

	@Override
	public void list(final String body, final boolean ordered) {
		// TODO Auto-generated method stub

	}

	@Override
	public void listitem(final String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startParagraph() {
		html.append("<p>");
	}

	@Override
	public void endParagraph() {
		html.append("</p>\n");
	}

	@Override
	public void table(final String header, final String body) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tablerow(final String content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tablecell(final String content, final String flags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startEm() {
		html.append("<em>");
	}

	@Override
	public void endEm() {
		html.append("</em>");
	}

	@Override
	public void startStrong() {
		html.append("<strong>");
	}

	@Override
	public void endStrong() {
		html.append("</strong>");
	}

	@Override
	public void codespan(final String text) {
		html.append("<code>");
		html.append(text);
		html.append("</code>");
	}

	@Override
	public void br() {
		// TODO Auto-generated method stub

	}

	@Override
	public void del(final String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void link(final String href, final String title, final String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void image(final String href, final String title, final String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void text(final String text) {
		html.append(text);
	}

	@Override
	public String toString() {
		return html.toString();
	}
}
