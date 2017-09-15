/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial code from https://github.com/atom/node-oniguruma
 * Initial copyright Copyright (c) 2013 GitHub Inc.
 * Initial license: MIT
 *
 * Contributors:
 *  - GitHub Inc.: Initial code, written in JavaScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 *  - Fabio Zadrozny <fabiofz@gmail.com> - Convert uniqueId to Object (for identity compare)
 *  - Fabio Zadrozny <fabiofz@gmail.com> - Fix recursion error on OnigRegExp with unicode chars
 */

package org.eclipse.tm4e.core.internal.oniguruma;

import java.io.UnsupportedEncodingException;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.joni.Syntax;
import org.joni.WarnCallback;

/**
 *
 * @see https://github.com/atom/node-oniguruma/blob/master/src/onig-reg-exp.cc
 *
 */
public class OnigRegExp {

	private Object lastSearchStrUniqueId;
	private int lastSearchPosition;
	private OnigResult lastSearchResult;
	private Regex regex;

	public OnigRegExp(String source) {
		lastSearchStrUniqueId = null;
		lastSearchPosition = -1;
		lastSearchResult = null;
		byte[] pattern;
		try {
			pattern = source.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		this.regex = new Regex(pattern, 0, pattern.length, Option.CAPTURE_GROUP, UTF8Encoding.INSTANCE, Syntax.DEFAULT,
				WarnCallback.DEFAULT);
	}

	public OnigResult Search(OnigString str, int position) {
		if (lastSearchStrUniqueId == str.uniqueId() && lastSearchPosition <= position) {
			if (lastSearchResult == null || lastSearchResult.LocationAt(0) >= position) {
				return lastSearchResult;
			}
		}

		lastSearchStrUniqueId = str.uniqueId();
		lastSearchPosition = position;
		lastSearchResult = Search(str.utf8_value(), position, str.utf8_length());
		return lastSearchResult;
	}

	private OnigResult Search(byte[] data, int position, int end) {
		Matcher matcher = regex.matcher(data);
		int status = matcher.search(position, end, Option.DEFAULT);
		if (status != Matcher.FAILED) {
			Region region = matcher.getEagerRegion();
			return new OnigResult(region, -1);
		}
		return null;
	}
}
