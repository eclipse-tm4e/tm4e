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

public final class OnigCaptureIndex {

	private final int index;
	private final int start;
	private final int end;

	OnigCaptureIndex(int index, int start, int end) {
		this.index = index;
		this.start = start >= 0 ? start : 0;
		this.end = end >= 0 ? end : 0;
	}

	public int getIndex() {
		return index;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getLength() {
		return end - start;
	}

	@Override
	public String toString() {
		return "{" +
				"\"index\": " + index +
				", \"start\": " + start +
				", \"end\": " + end +
				", \"length\": " + getLength() +
				"}";
	}
}
