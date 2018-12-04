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
 */
 
package org.eclipse.tm4e.core.internal.oniguruma;

import java.util.ArrayList;
import java.util.List;

public class OnigSearcher {

	private final List<OnigRegExp> regExps;

	public OnigSearcher(String[] regexps) {
		this.regExps = new ArrayList<OnigRegExp>();
		for (int i = 0; i < regexps.length; i++) {
			this.regExps.add(new OnigRegExp(regexps[i]));
		}
	}

	public OnigResult search(OnigString source, int charOffset) {
		int byteOffset = source.convertUtf16OffsetToUtf8(charOffset);

		int bestLocation = 0;
		OnigResult bestResult = null;
		int index = 0;

		for (OnigRegExp regExp : regExps) {
			OnigResult result = regExp.Search(source, byteOffset);
			if (result != null && result.count() > 0) {
				int location = result.LocationAt(0);
				
				if (bestResult == null || location < bestLocation) {
					bestLocation = location;
					bestResult = result;
					bestResult.setIndex(index);
				}

				if (location == byteOffset) {
					break;
				}
			}
			index++;
		}
		return bestResult;
	}

}
