/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.model;

import org.eclipse.tm4e.core.internal.utils.StringUtils;

/**
 * Describes folding rules for a language.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/languageConfiguration.ts#L139">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts</a>
 */
public final class FoldingRules {

	/**
	 * Describes language specific folding markers such as '#region' and '#endregion'.
	 * The start and end regexes will be tested against the contents of all lines and must be designed efficiently:
	 * - the regex should start with '^'
	 * - regexp flags (i, g) are ignored
	 */
	public static final class FoldingMarkers {
		public final RegExPattern start;
		public final RegExPattern end;

		public FoldingMarkers(final RegExPattern start, final RegExPattern end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return StringUtils.toString(this, sb -> sb
					.append("start=").append(start).append(", ")
					.append("end=").append(end));
		}
	}

	/**
	 * Used by the indentation based strategy to decide whether empty lines belong to the previous or the next block.
	 * A language adheres to the off-side rule if blocks in that language are expressed by their indentation.
	 * See [wikipedia](https://en.wikipedia.org/wiki/Off-side_rule) for more information.
	 * <p>
	 * If not set, `false` is used and empty lines belong to the previous block.
	 */
	public final boolean offSide;
	public final FoldingMarkers markers;

	public FoldingRules(final boolean offSide, final RegExPattern markersStart, final RegExPattern markersEnd) {
		this.offSide = offSide;
		this.markers = new FoldingMarkers(markersStart, markersEnd);
	}

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> sb
				.append("markers=").append(", ")
				.append("offSide=").append(offSide));
	}
}
