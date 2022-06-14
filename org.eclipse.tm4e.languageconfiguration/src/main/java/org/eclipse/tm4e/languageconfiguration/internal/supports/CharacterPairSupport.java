/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.model.ILanguageConfiguration;

/**
 * The "character pair" support.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/characterPair.ts">
 *      https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/characterPair.ts</a>
 */
public final class CharacterPairSupport {

	public final List<AutoClosingPairConditional> autoClosingPairs;
	public final List<AutoClosingPair> surroundingPairs;
	// TODO public final String autoCloseBefore;

	@SuppressWarnings("unchecked")
	public CharacterPairSupport(ILanguageConfiguration config) {
		final var autoClosingPairs = config.getAutoClosingPairs();
		final var brackets = config.getBrackets();

		if (autoClosingPairs != null) {
			this.autoClosingPairs = autoClosingPairs.stream().filter(Objects::nonNull)
					.map(el -> new AutoClosingPairConditional(el.open, el.close, el.notIn))
					.collect(Collectors.toList());
		} else if (brackets != null) {
			this.autoClosingPairs = brackets.stream().filter(Objects::nonNull)
					.map(el -> new AutoClosingPairConditional(el.open, el.close, Collections.emptyList()))
					.collect(Collectors.toList());
		} else {
			this.autoClosingPairs = Collections.emptyList();
		}

		final var surroundingPairs = config.getSurroundingPairs();
		this.surroundingPairs = surroundingPairs != null
				? surroundingPairs.stream().filter(Objects::nonNull).collect(Collectors.toList())
				: (List<AutoClosingPair>) (List<?>) this.autoClosingPairs;
	}

	/**
	 * TODO not in upstream project
	 */
	@Nullable
	public AutoClosingPairConditional getAutoClosePair(final String text, final int offset,
			final String newCharacter/* : string, context: ScopedLineTokens, column: number */) {
		if (newCharacter.isEmpty()) {
			return null;
		}
		for (final AutoClosingPairConditional autoClosingPair : autoClosingPairs) {
			final String opening = autoClosingPair.open;
			if (!opening.endsWith(newCharacter)) {
				continue;
			}
			if (opening.length() > 1) {
				final String offsetPrefix = text.substring(0, offset);
				if (!offsetPrefix.endsWith(opening.substring(0, opening.length() - 1))) {
					continue;
				}
			}
			return autoClosingPair;
		}
		return null;
	}
}
