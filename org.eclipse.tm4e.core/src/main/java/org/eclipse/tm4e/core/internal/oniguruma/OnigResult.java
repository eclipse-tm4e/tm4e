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

import org.joni.Region;

public class OnigResult {

	private int indexInScanner;
	private final Region region;

	public OnigResult(Region region, int indexInScanner) {
		this.region = region;
		this.indexInScanner = indexInScanner;
	}

	public int getIndex() {
		return indexInScanner;
	}

	public void setIndex(int index) {
		this.indexInScanner = index;
	}

	public int locationAt(int index) {
		int bytes = region.beg[index];
		if (bytes > 0) {
			return bytes;
		}
		return 0;
	}

	public int count() {
		return region.numRegs;
	}

	public int lengthAt(int index) {
		int bytes = region.end[index] - region.beg[index];
		if (bytes > 0) {
			return bytes;
		}
		return 0;
	}
}
