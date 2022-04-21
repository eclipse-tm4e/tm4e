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

public final class OnigNextMatchResult {

	private final int index;

	private final OnigCaptureIndex[] captureIndices;

	OnigNextMatchResult(OnigResult result, OnigString source) {
		this.index = result.getIndex();
		this.captureIndices = captureIndicesForMatch(result, source);
	}

	public int getIndex() {
		return index;
	}

	public OnigCaptureIndex[] getCaptureIndices() {
		return captureIndices;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder("{\n");
		result.append("  \"index\": ");
		result.append(getIndex());
		result.append(",\n");
		result.append("  \"captureIndices\": [\n");
		int i = 0;
		for (OnigCaptureIndex captureIndex : getCaptureIndices()) {
			if (i > 0) {
				result.append(",\n");
			}
			result.append("    ");
			result.append(captureIndex);
			i++;
		}
		result.append("\n");
		result.append("  ]\n");
		result.append("}");
		return result.toString();
	}

	private static OnigCaptureIndex[] captureIndicesForMatch(OnigResult result, OnigString source) {
		int resultCount = result.count();
		OnigCaptureIndex[] captures = new OnigCaptureIndex[resultCount];
		for (int index = 0; index < resultCount; index++) {
			int captureStart = source.convertUtf8OffsetToUtf16(result.locationAt(index));
			int captureEnd = source.convertUtf8OffsetToUtf16(result.locationAt(index) + result.lengthAt(index));
			captures[index] = new OnigCaptureIndex(index, captureStart, captureEnd);
		}
		return captures;
	}
}
