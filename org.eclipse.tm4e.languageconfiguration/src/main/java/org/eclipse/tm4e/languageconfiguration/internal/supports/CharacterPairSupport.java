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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The "character pair" support.
 */
public final class CharacterPairSupport {

	private List<CharacterPair> autoClosingPairs;
	private final List<CharacterPair> surroundingPairs;

	public CharacterPairSupport(@Nullable final List<CharacterPair> brackets,
			@Nullable final List<AutoClosingPairConditional> autoClosingPairs,
			@Nullable final List<CharacterPair> surroundingPairs) {
		if (autoClosingPairs != null) {
			this.autoClosingPairs = autoClosingPairs.stream().filter(Objects::nonNull)
					.map(el -> new AutoClosingPairConditional(el.getKey(), el.getValue(), el.getNotIn()))
					.collect(Collectors.toList());
		} else if (brackets != null) {
			this.autoClosingPairs = brackets.stream().filter(Objects::nonNull)
					.map(el -> new AutoClosingPairConditional(el.getKey(), el.getValue(), null))
					.collect(Collectors.toList());
		} else {
			this.autoClosingPairs = new ArrayList<>();
		}

		this.surroundingPairs = surroundingPairs != null
				? surroundingPairs.stream().filter(Objects::nonNull).collect(Collectors.toList())
				: this.autoClosingPairs;
	}

	@Nullable
	public CharacterPair getAutoClosePair(final String text, final int offset,
			final String newCharacter/* : string, context: ScopedLineTokens, column: number */) {
		if (newCharacter.isEmpty()) {
			return null;
		}
		for (final CharacterPair autoClosingPair : autoClosingPairs) {
			final String opening = autoClosingPair.getKey();
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

	public List<CharacterPair> getAutoClosingPairs() {
		return autoClosingPairs;
	}

	public List<CharacterPair> getSurroundingPairs() {
		return surroundingPairs;
	}
}
