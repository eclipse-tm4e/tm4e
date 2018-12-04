/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.theme;

import java.util.ArrayList;
import java.util.List;

public class ThemeTrieElementRule {

	// _themeTrieElementRuleBrand: void;

	public int scopeDepth;
	public final List<String> parentScopes;
	public int fontStyle;
	public int foreground;
	public int background;

	public ThemeTrieElementRule(int scopeDepth, List<String> parentScopes, int fontStyle, int foreground,
			int background) {
		this.scopeDepth = scopeDepth;
		this.parentScopes = parentScopes;
		this.fontStyle = fontStyle;
		this.foreground = foreground;
		this.background = background;
	}

	public ThemeTrieElementRule clone() {
		return new ThemeTrieElementRule(this.scopeDepth, this.parentScopes, this.fontStyle, this.foreground,
				this.background);
	}

	public static List<ThemeTrieElementRule> cloneArr(List<ThemeTrieElementRule> arr) {
		List<ThemeTrieElementRule> r = new ArrayList<>();
		for (int i = 0, len = arr.size(); i < len; i++) {
			r.add(arr.get(i).clone());
		}
		return r;
	}

	public void acceptOverwrite(int scopeDepth, int fontStyle, int foreground, int background) {
		if (this.scopeDepth > scopeDepth) {
			// TODO!!!
			// console.log('how did this happen?');
		} else {
			this.scopeDepth = scopeDepth;
		}
		// console.log('TODO -> my depth: ' + this.scopeDepth + ', overwriting
		// depth: ' + scopeDepth);
		if (fontStyle != FontStyle.NotSet) {
			this.fontStyle = fontStyle;
		}
		if (foreground != 0) {
			this.foreground = foreground;
		}
		if (background != 0) {
			this.background = background;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + background;
		result = prime * result + fontStyle;
		result = prime * result + foreground;
		result = prime * result + ((parentScopes == null) ? 0 : parentScopes.hashCode());
		result = prime * result + scopeDepth;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThemeTrieElementRule other = (ThemeTrieElementRule) obj;
		if (background != other.background)
			return false;
		if (fontStyle != other.fontStyle)
			return false;
		if (foreground != other.foreground)
			return false;
		if (parentScopes == null) {
			if (other.parentScopes != null)
				return false;
		} else if (!parentScopes.equals(other.parentScopes))
			return false;
		if (scopeDepth != other.scopeDepth)
			return false;
		return true;
	}
	
	
}
