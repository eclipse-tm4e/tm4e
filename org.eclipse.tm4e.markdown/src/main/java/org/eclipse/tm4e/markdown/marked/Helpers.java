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

public class Helpers {

	public static String escape(String html) {
		return escape(html, false);
	}

	public static String escape(String html, boolean encode) {
		return html
			    .replaceAll(!encode ? "&(?!#?\\w+;)" : "&", "&amp;")
			    .replaceAll("<", "&lt;")
			    .replaceAll(">", "&gt;")
			    .replaceAll("\"", "&quot;")
			    .replaceAll("'", "&#39;");
	}

	public static boolean isEmpty(String s) {
		return s == null || s.length() < 1;
	}

}
