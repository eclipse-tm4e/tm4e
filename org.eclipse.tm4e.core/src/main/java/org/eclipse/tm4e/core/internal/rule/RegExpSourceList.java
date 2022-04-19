/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/Microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tm4e.core.internal.oniguruma.OnigScanner;

/**
 *
 * @see https://github.com/Microsoft/vscode-textmate/blob/master/src/rule.ts
 *
 */
final class RegExpSourceList {

	private static final class RegExpSourceListAnchorCache {

		private ICompiledRule A0_G0;
		private ICompiledRule A0_G1;
		private ICompiledRule A1_G0;
		private ICompiledRule A1_G1;

	}

	private final List<RegExpSource> _items;
	private boolean _hasAnchors;
	private ICompiledRule _cached;
	private final RegExpSourceListAnchorCache _anchorCache;

	RegExpSourceList() {
		this._items = new ArrayList<>();
		this._hasAnchors = false;
		this._cached = null;
		this._anchorCache = new RegExpSourceListAnchorCache();
	}

	void push(RegExpSource item) {
		this._items.add(item);
		this._hasAnchors = this._hasAnchors ? this._hasAnchors : item.hasAnchor();
	}

	void unshift(RegExpSource item) {
		this._items.add(0, item);
		this._hasAnchors = this._hasAnchors ? this._hasAnchors : item.hasAnchor();
	}

	int length() {
		return this._items.size();
	}

	void setSource(int index, String newSource) {
		RegExpSource r = this._items.get(index);
		if (!r.getSource().equals(newSource)) {
			// bust the cache
			this._cached = null;
			this._anchorCache.A0_G0 = null;
			this._anchorCache.A0_G1 = null;
			this._anchorCache.A1_G0 = null;
			this._anchorCache.A1_G1 = null;
			r.setSource(newSource);
		}
	}

	ICompiledRule compile(IRuleRegistry grammar, boolean allowA, boolean allowG) {
		if (!this._hasAnchors) {
			if (this._cached == null) {
				List<String> regexps = new ArrayList<>();
				for (RegExpSource regExpSource : _items) {
					regexps.add(regExpSource.getSource());
				}
				this._cached = new ICompiledRule(createOnigScanner(regexps.toArray(new String[0])), getRules());
			}
			return this._cached;
		}

		if (this._anchorCache.A0_G0 == null) {
			this._anchorCache.A0_G0 = (allowA == false && allowG == false) ? this._resolveAnchors(allowA, allowG)
					: null;
		}
		if (this._anchorCache.A0_G1 == null) {
			this._anchorCache.A0_G1 = (allowA == false && allowG == true) ? this._resolveAnchors(allowA, allowG)
					: null;
		}
		if (this._anchorCache.A1_G0 == null) {
			this._anchorCache.A1_G0 = (allowA == true && allowG == false) ? this._resolveAnchors(allowA, allowG)
					: null;
		}
		if (this._anchorCache.A1_G1 == null) {
			this._anchorCache.A1_G1 = (allowA == true && allowG == true) ? this._resolveAnchors(allowA, allowG)
					: null;
		}

		if (allowA) {
			if (allowG) {
				return this._anchorCache.A1_G1;
			}
			return this._anchorCache.A1_G0;
		}

		if (allowG) {
			return this._anchorCache.A0_G1;
		}
		return this._anchorCache.A0_G0;
	}

	private ICompiledRule _resolveAnchors(boolean allowA, boolean allowG) {
		List<String> regexps = new ArrayList<>();
		for (RegExpSource regExpSource : _items) {
			regexps.add(regExpSource.resolveAnchors(allowA, allowG));
		}
		return new ICompiledRule(createOnigScanner(regexps.toArray(new String[0])), getRules());
	}

	private OnigScanner createOnigScanner(String[] regexps) {
		return new OnigScanner(regexps);
	}

	private Integer[] getRules() {
		Collection<Integer> ruleIds = new ArrayList<>();
		for (RegExpSource item : this._items) {
			ruleIds.add(item.getRuleId());
		}
		return ruleIds.toArray(new Integer[0]);
	}

}
