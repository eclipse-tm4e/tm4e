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

public class BlockRules {

	private static final String _tag = "(?!(?:" + "a|em|strong|small|s|cite|q|dfn|abbr|data|time|code"
			+ "|var|samp|kbd|sub|sup|i|b|u|mark|ruby|rt|rp|bdi|bdo"
			+ "|span|br|wbr|ins|del|img)\\b)\\w+(?!:\\/|[^\\w\\s@]*@)\\b";

	public static BlockRules normal = normal();
	public static BlockRules gfm = gfm();
	public static BlockRules tables = tables();

	public final RegExp newline;
	public final RegExp code;
	public final RegExp fences;
	public final RegExp hr;
	public final RegExp heading;
	public final RegExp nptable;
	public final RegExp lheading;
	public final RegExp blockquote;
	public final RegExp list;
	public final RegExp html;
	public final RegExp def;
	public final RegExp table;
	public final RegExp paragraph;
	public final RegExp text;
	public final RegExp bullet;
	public final RegExp item;

	public BlockRules(final RegExp newline, final RegExp code, final RegExp fences, final RegExp hr, final RegExp heading, final RegExp nptable,
			final RegExp lheading, final RegExp blockquote, final RegExp list, final RegExp html, final RegExp def, final RegExp table, final RegExp paragraph,
			final RegExp text, final RegExp bullet, final RegExp item) {
		this.newline = newline;
		this.code = code;
		this.fences = fences;
		this.hr = hr;
		this.heading = heading;
		this.nptable = nptable;
		this.lheading = lheading;
		this.blockquote = blockquote;
		this.list = list;
		this.html = html;
		this.def = def;
		this.table = table;
		this.paragraph = paragraph;
		this.text = text;
		this.bullet = bullet;
		this.item = item;
	}

	private static BlockRules block() {
		final RegExp newline = new RegExp("^\\n+");
		final RegExp code = new RegExp("^( {4}[^\\n]+\\n*)+/");
		final RegExp fences = new RegExp("");
		final RegExp hr = new RegExp("^( *[-*_]){3,} *(?:\\n+|$)");
		final RegExp heading = new RegExp("^ *(#{1,6}) *([^\\n]+?) *#* *(?:\\n+|$)");
		final RegExp nptable = new RegExp("");
		final RegExp lheading = new RegExp("^([^\\n]+)\\n *(=|-){2,} *(?:\\n+|$)");
		final RegExp blockquote = new RegExp("^( *>[^\\n]+(\\n(?!def)[^\\n]+)*\\n*)+");
		final RegExp list = new RegExp("^( *)(bull) [\\s\\S]+?(?:hr|def|\\n{2,}(?! )(?!\\1bull )\\n*|\\s*$)");
		final RegExp html = new RegExp("^ *(?:comment *(?:\\n|\\s*$)|closed *(?:\\n{2,}|\\s*$)|closing *(?:\\n{2,}|\\s*$))");
		final RegExp def = new RegExp("^ *\\[([^\\]]+)\\]: *<?([^\\s>]+)>?(?: +[\"(]([^\\n]+)[\")])? *(?:\\n+|$)");
		final RegExp table = RegExp.noop();
		final RegExp paragraph = new RegExp("^((?:[^\\n]+\\n?(?!hr|heading|lheading|blockquote|tag|def))+)\\n*");
		final RegExp text = new RegExp("^[^\\n]+");
		final RegExp bullet = new RegExp("(?:[*+-]|\\d+\\.)");
		final RegExp item = new RegExp("^( *)(bull) [^\\n]*(?:\\n(?!\\1bull )[^\\n]*)*");

		item.replaceAll("bull", bullet);
		list.replaceAll("bull", bullet).replace("hr", "\\n+(?=\\1?(?:[-*_] *){3,}(?:\\n+|$))").replace("def",
				"\\n+(?=" + def.source + ")");
		blockquote.replace("def", def);
		paragraph.replace("hr", hr).replace("heading", heading).replace("lheading", lheading)
				.replace("blockquote", blockquote).replace("tag", "<" + _tag).replace("def", def);
		return new BlockRules(newline, code, fences, hr, heading, nptable, lheading, blockquote, list, html, def, table,
				paragraph, text, bullet, item);
	}

	private static BlockRules normal() {
		return block();
	}

	private static BlockRules gfm() {
		final BlockRules gfm = normal();
		gfm.fences.source = "^ *(`{3,}|~{3,})[ \\.]*(\\S+)? *\\n([\\s\\S]*?)\\s*\\1 *(?:\\n+|$)";
		// gfm.paragraph.source = "^";
		gfm.heading.source = "^ *(#{1,6}) +([^\\n]+?) *#* *(?:\\n+|$)";
		final String pattern = "(?!" + gfm.fences.source.replaceFirst("\\\\1", "\\\\2") + "|"
				+ gfm.list.source.replaceFirst("\\\\1", "\\\\3") + "|";
		//pattern = pattern.replaceAll("\\\"", "\\\\\"");
		//pattern = pattern.replaceAll("[$]", "\\\\\\$");
		gfm.paragraph.replace("\\(\\?\\!", pattern);
		return gfm;
	}

	private static BlockRules tables() {
		final BlockRules tables = gfm();

		return tables;
	}
}
