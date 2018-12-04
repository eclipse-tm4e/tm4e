/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/atom/node-oniguruma
 * Initial copyright Copyright (c) 2013 GitHub Inc.
 * Initial license: MIT
 *
 * Contributors:
 *  - GitHub Inc.: Initial code, written in JavaScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 *  - Fabio Zadrozny <fabiofz@gmail.com> - Convert uniqueId to Object (for identity compare)
 *  - Fabio Zadrozny <fabiofz@gmail.com> - Fix recursion error on creation of OnigRegExp with unicode chars
 */

package org.eclipse.tm4e.core.internal.oniguruma;

import java.nio.charset.StandardCharsets;

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
		byte[] pattern = source.getBytes(StandardCharsets.UTF_8);
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
