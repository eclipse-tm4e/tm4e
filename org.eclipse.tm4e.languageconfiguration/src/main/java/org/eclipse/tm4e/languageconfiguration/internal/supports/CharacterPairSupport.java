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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;

/**
 * The "character pair" support.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/supports/characterPair.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/characterPair.ts</a>
 */
public final class CharacterPairSupport {

	public static final String DEFAULT_AUTOCLOSE_BEFORE_LANGUAGE_DEFINED = ";:.,=}])> \r\n\t";
	//public static final String DEFAULT_AUTOCLOSE_BEFORE_LANGUAGE_DEFINED_QUOTES = ";:.,=}])> \n\t";
	//public static final String DEFAULT_AUTOCLOSE_BEFORE_LANGUAGE_DEFINED_BRACKETS = "'\"`;:.,=}])> \n\t";
	public static final String DEFAULT_AUTOCLOSE_BEFORE_WHITESPACE = " \r\n\t";

	public final List<AutoClosingPairConditional> autoClosingPairs;
	public final List<AutoClosingPair> surroundingPairs;
	public final String autoCloseBefore;
	//public final String autoCloseBeforeForBrackets;
	//public final String autoCloseBeforeForQuotes;

	@SuppressWarnings("unchecked")
	public CharacterPairSupport(final LanguageConfiguration config) {
		final var autoClosingPairs = config.getAutoClosingPairs();
		if (!autoClosingPairs.isEmpty()) {
			this.autoClosingPairs = autoClosingPairs;
		} else {
			final var brackets = config.getBrackets();
			if (!brackets.isEmpty()) {
				this.autoClosingPairs = brackets.stream()
						.map(el -> new AutoClosingPairConditional(el.open, el.close, Collections.emptyList()))
						.toList();
			} else {
				this.autoClosingPairs = Collections.emptyList();
			}
		}

		final var autoCloseBefore = config.getAutoCloseBefore();
		this.autoCloseBefore = autoCloseBefore != null
				? autoCloseBefore
				: CharacterPairSupport.DEFAULT_AUTOCLOSE_BEFORE_LANGUAGE_DEFINED;
		//this.autoCloseBeforeForQuotes = autoCloseBefore != null
		//		? autoCloseBefore
		//		: CharacterPairSupport.DEFAULT_AUTOCLOSE_BEFORE_LANGUAGE_DEFINED_QUOTES;
		//this.autoCloseBeforeForBrackets = autoCloseBefore != null
		//		? autoCloseBefore
		//		: CharacterPairSupport.DEFAULT_AUTOCLOSE_BEFORE_LANGUAGE_DEFINED_BRACKETS;

		final var surroundingPairs = config.getSurroundingPairs();
		this.surroundingPairs = !surroundingPairs.isEmpty()
				? surroundingPairs
				: (List<AutoClosingPair>) (List<?>) this.autoClosingPairs;
	}

	/**
	 * TODO not declared in upstream project
	 */
	public @Nullable AutoClosingPairConditional getAutoClosingPair(final String text, final int offset, final String newCharacter) {
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
