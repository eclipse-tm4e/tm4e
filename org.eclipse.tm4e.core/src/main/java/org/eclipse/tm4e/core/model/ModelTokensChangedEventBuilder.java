/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
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
 *  - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 *  - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.model;

import static org.eclipse.tm4e.core.internal.utils.MoreCollections.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

final class ModelTokensChangedEventBuilder {

	private final ITMModel model;
	private final List<Range> ranges = new ArrayList<>();

	public ModelTokensChangedEventBuilder(final ITMModel model) {
		this.model = model;
	}

	public void registerChangedTokens(final int lineNumber) {
		final Range previousRange = findLastElement(ranges);

		if (previousRange != null && previousRange.toLineNumber == lineNumber - 1) {
			// extend previous range
			previousRange.toLineNumber++;
		} else {
			// insert new range
			ranges.add(new Range(lineNumber));
		}
	}

	@Nullable
	public ModelTokensChangedEvent build() {
		if (this.ranges.isEmpty()) {
			return null;
		}
		return new ModelTokensChangedEvent(ranges, model);
	}
}
