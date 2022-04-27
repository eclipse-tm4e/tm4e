/**
 * Copyright (c) 2015-2017 Angelo ZERR.
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
 * - GitHub Inc.: Initial code, written in JavaScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.oniguruma;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

public final class OnigScanner {

	private final OnigSearcher searcher;

	public OnigScanner(final Collection<String> regexps) {
		searcher = new OnigSearcher(regexps);
	}

	@Nullable
	public OnigNextMatchResult findNextMatchSync(final OnigString source, final int charOffset) {
		final OnigResult bestResult = searcher.search(source, charOffset);
		if (bestResult != null) {
			return new OnigNextMatchResult(bestResult, source);
		}
		return null;
	}

	@Nullable
	OnigNextMatchResult findNextMatchSync(final String lin, final int pos) {
		return findNextMatchSync(OnigString.of(lin), pos);
	}

}
