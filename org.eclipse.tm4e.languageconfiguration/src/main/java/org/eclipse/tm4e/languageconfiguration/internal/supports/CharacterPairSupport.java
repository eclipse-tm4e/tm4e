/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.StandardAutoClosingPairConditional;

/**
 * The "character pair" support.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/characterPair.ts">
 *      https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/characterPair.ts</a>
 */
public final class CharacterPairSupport {

	public final List<StandardAutoClosingPairConditional> autoClosingPairs;
	public final List<CharacterPair> surroundingPairs;

	@SuppressWarnings("unchecked")
	public CharacterPairSupport(@Nullable final List<CharacterPair> brackets,
			@Nullable final List<StandardAutoClosingPairConditional> autoClosingPairs,
			@Nullable final List<CharacterPair> surroundingPairs) {

		if (autoClosingPairs != null) {
			this.autoClosingPairs = autoClosingPairs.stream().filter(Objects::nonNull)
					.map(el -> new StandardAutoClosingPairConditional(el.open, el.close, el.notIn))
					.collect(Collectors.toList());
		} else if (brackets != null) {
			this.autoClosingPairs = brackets.stream().filter(Objects::nonNull)
					.map(el -> new StandardAutoClosingPairConditional(el.open, el.close, Collections.emptyList()))
					.collect(Collectors.toList());
		} else {
			this.autoClosingPairs = Collections.emptyList();
		}

		this.surroundingPairs = surroundingPairs != null
				? surroundingPairs.stream().filter(Objects::nonNull).collect(Collectors.toList())
				: (List<CharacterPair>) (List<?>) this.autoClosingPairs;
	}

	@Nullable
	public StandardAutoClosingPairConditional getAutoClosePair(final String text, final int offset,
			final String newCharacter/* : string, context: ScopedLineTokens, column: number */) {
		if (newCharacter.isEmpty()) {
			return null;
		}
		for (final StandardAutoClosingPairConditional autoClosingPair : autoClosingPairs) {
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
