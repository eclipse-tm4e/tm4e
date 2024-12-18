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
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

public class RegExp {

	protected @Nullable String source;
	private @Nullable Pattern pattern;

	public RegExp(final @Nullable String source) {
		this.source = source;
	}

	public @Nullable Matcher exec(final String s) {
		final var source = this.source;
		if (source == null) {
			return null;
		}

		Pattern pattern = this.pattern;
		if (pattern == null) {
			pattern = this.pattern = Pattern.compile(source);
		}

		final Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			return matcher;
		}
		return null;
	}

	public RegExp replace(final String name, final RegExp val) {
		return replace(name, val.source);
	}

	public RegExp replace(final @Nullable String name, @Nullable String val) {
		if (name == null)
			return new RegExp(source);

		final var source = this.source;
		if (source != null && val != null) {
			val = val.replaceAll("(^|[^\\[])\\^", "$1");
			this.source = source.replaceFirst(name, Matcher.quoteReplacement(val));
		}
		return this;
	}

	public RegExp replaceAll(final String name, final RegExp val) {
		return replaceAll(name, val.source);
	}

	public RegExp replaceAll(final @Nullable String name, @Nullable String val) {
		if (name == null)
			return new RegExp(source);

		final var source = this.source;
		if (source != null && val != null) {
			val = val.replaceAll("(^|[^\\[])\\^", "$1");
			this.source = source.replaceAll(name, Matcher.quoteReplacement(val));
		}
		return this;
	}

	public static final RegExp noop() {
		return new RegExp(null);
	}

}
