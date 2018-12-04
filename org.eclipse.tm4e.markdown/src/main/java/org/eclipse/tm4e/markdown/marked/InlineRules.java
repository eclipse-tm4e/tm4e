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

public class InlineRules {

	private static final String INLINE_INSIDE = "(?:\\[[^\\]]*\\]|[^\\[\\]]|\\](?=[^\\[]*\\]))*";
	private static final String INLINE_HREF = "\\s*<?([\\s\\S]*?)>?(?:\\s+['\"]([\\s\\S]*?)['\"])?\\s*";

	public static final InlineRules normal = normal();

	public static final InlineRules pedantic = pedantic();

	public static final InlineRules gfm = gfm();

	public static final InlineRules breaks = breaks();

	public final RegExp escape;
	public final RegExp autolink;
	public final RegExp url;
	public final RegExp tag;
	public final RegExp link;
	public final RegExp reflink;
	public final RegExp nolink;
	public final RegExp strong;
	public final RegExp em;
	public final RegExp code;
	public final RegExp br;
	public final RegExp del;
	public final RegExp text;

	public InlineRules(RegExp escape, RegExp autolink, RegExp url, RegExp tag, RegExp link, RegExp reflink,
			RegExp nolink, RegExp strong, RegExp em, RegExp code, RegExp br, RegExp del, RegExp text) {
		this.escape = escape;
		this.autolink = autolink;
		this.url = url;
		this.tag = tag;
		this.link = link;
		this.reflink = reflink;
		this.nolink = nolink;
		this.strong = strong;
		this.em = em;
		this.code = code;
		this.br = br;
		this.del = del;
		this.text = text;
	}

	private static InlineRules inline() {
		RegExp escape = new RegExp("^\\\\([\\\\`*{}\\[\\]()#+\\-.!_>])");
		RegExp autolink = new RegExp("^<([^ >]+(@|:\\/)[^ >]+)>");
		RegExp url = RegExp.noop();
		RegExp tag = new RegExp("^<!--[\\s\\S]*?-->|^<\\/?\\w+(?:\"[^\"]*\"|'[^']*'|[^'\">])*?>");
		RegExp link = new RegExp("^!?\\[(inside)\\]\\(href\\)");
		RegExp reflink = new RegExp("^!?\\[(inside)\\]\\s*\\[([^\\]]*)\\]");
		RegExp nolink = new RegExp("^!?\\[((?:\\[[^\\]]*\\]|[^\\[\\]])*)\\]");
		RegExp strong = new RegExp("^__([\\s\\S]+?)__(?!_)|^\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)");
		RegExp em = new RegExp("^\\b_((?:[^_]|__)+?)_\\b|^\\*((?:\\*\\*|[\\s\\S])+?)\\*(?!\\*)");
		RegExp code = new RegExp("^(`+)\\s*([\\s\\S]*?[^`])\\s*\\1(?!`)");
		RegExp br = new RegExp("^ {2,}\\n(?!\\s*$)");
		RegExp del = RegExp.noop();
		RegExp text = new RegExp("^[\\s\\S]+?(?=[\\\\<!\\[_*`]| {2,}\\n|$)");
		// Replacement
		link.replace("inside", INLINE_INSIDE).replace("href", INLINE_HREF);
		reflink.replace("inside", INLINE_INSIDE);
		return new InlineRules(escape, autolink, url, tag, link, reflink, nolink, strong, em, code, br, del, text);
	}

	private static InlineRules normal() {
		InlineRules normal = inline();
		return normal;
	}

	private static InlineRules pedantic() {
		InlineRules pedantic = normal();
		pedantic.strong.source = "^__(?=\\S)([\\s\\S]*?\\S)__(?!_)|^\\*\\*(?=\\S)([\\s\\S]*?\\S)\\*\\*(?!\\*)";
		pedantic.em.source = "^_(?=\\S)([\\s\\S]*?\\S)_(?!_)|^\\*(?=\\S)([\\s\\S]*?\\S)\\*(?!\\*)";
		return pedantic;
	}

	private static InlineRules gfm() {
		InlineRules gmf = normal();
		gmf.escape.replace("\\]\\)", "~|])");
		gmf.url.source = "^(https?:\\/\\/[^\\s<]+[^<.,:;\"')\\]\\s])";
		gmf.del.source = "^~~(?=\\S)([\\s\\S]*?\\S)~~";
		gmf.text.replace("\\]\\|", "~]|").replace("\\|", "|https?://|");
		return gmf;
	}

	private static InlineRules breaks() {
		InlineRules breaks = gfm();
		breaks.br.replace("{2,}", "*");
		breaks.text.replace("{2,}", "*");
		return breaks;
	}
}
