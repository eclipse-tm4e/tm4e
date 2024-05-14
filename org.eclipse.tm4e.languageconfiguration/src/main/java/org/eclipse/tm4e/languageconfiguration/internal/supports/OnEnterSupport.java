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
 * Sebastian Thomschke (Vegard IT) - add previousLineText support
 */
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.model.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.OnEnterRule;
import org.eclipse.tm4e.languageconfiguration.internal.utils.Strings;

/**
 * On enter support.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/supports/onEnter.ts">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/onEnter.ts</a>
 */
public class OnEnterSupport {

	private static final class ProcessedBracketPair {

		final Pattern openRegExp;
		final Pattern closeRegExp;

		ProcessedBracketPair(final Pattern openRegExp, final Pattern closeRegExp) {
			this.openRegExp = openRegExp;
			this.closeRegExp = closeRegExp;
		}

		boolean matchOpen(final String beforeEnterText) {
			return openRegExp.matcher(beforeEnterText).find();
		}

		boolean matchClose(final String afterEnterText) {
			return closeRegExp.matcher(afterEnterText).find();
		}
	}

	private static final List<CharacterPair> DEFAULT_BRACKETS = List.of(
			new CharacterPair("(", ")"),
			new CharacterPair("{", "}"),
			new CharacterPair("[", "]"));

	private final List<ProcessedBracketPair> brackets = new ArrayList<>();
	private final List<OnEnterRule> regExpRules;

	public OnEnterSupport(final @Nullable List<CharacterPair> brackets, final @Nullable List<OnEnterRule> regExpRules) {
		for (final var charPair : brackets != null ? brackets : DEFAULT_BRACKETS) {
			final var openRegExp = createOpenBracketRegExp(charPair.open);
			final var closeRegExp = createCloseBracketRegExp(charPair.close);
			if (openRegExp != null && closeRegExp != null)
				this.brackets.add(new ProcessedBracketPair(openRegExp, closeRegExp));
		}
		this.regExpRules = regExpRules != null ? regExpRules : Collections.emptyList();
	}

	public @Nullable EnterAction onEnter(
			// TODO autoIndent: EditorAutoIndentStrategy,
			final String previousLineText,
			final String beforeEnterText,
			final String afterEnterText) {
		// (1): `regExpRules`
		// if (autoIndent >= EditorAutoIndentStrategy.Advanced) {
		for (final OnEnterRule rule : regExpRules) {
			if (!rule.beforeText.matchesPartially(beforeEnterText))
				continue;

			final var afterTextPattern = rule.afterText;
			if (afterTextPattern != null && !afterTextPattern.matchesPartially(afterEnterText))
				continue;

			final var previousLinePattern = rule.previousLineText;
			if (previousLinePattern != null && !previousLinePattern.matchesPartially(previousLineText))
				continue;

			return rule.action;
		}

		// (2): Special indent-outdent
		// if (autoIndent >= EditorAutoIndentStrategy.Brackets) {
		if (!beforeEnterText.isEmpty() && !afterEnterText.isEmpty()) {
			for (final ProcessedBracketPair bracket : brackets) {
				if (bracket.matchOpen(beforeEnterText) && bracket.matchClose(afterEnterText)) {
					return new EnterAction(IndentAction.IndentOutdent);
				}
			}
		}

		// (3): Open bracket based logic
		// if (autoIndent >= EditorAutoIndentStrategy.Brackets) {
		if (!beforeEnterText.isEmpty()) {
			for (final ProcessedBracketPair bracket : brackets) {
				if (bracket.matchOpen(beforeEnterText)) {
					return new EnterAction(IndentAction.Indent);
				}
			}
		}

		return null;
	}

	private static final Pattern B_REGEXP = Pattern.compile("\\B");

	private static @Nullable Pattern createOpenBracketRegExp(final String bracket) {
		final var str = new StringBuilder(Strings.escapeRegExpCharacters(bracket));
		final var c = String.valueOf(str.charAt(0));
		if (!B_REGEXP.matcher(c).find()) {
			str.insert(0, "\\b");
		}
		str.append("\\s*$");
		return safeRegExp(str);
	}

	private static @Nullable Pattern createCloseBracketRegExp(final String bracket) {
		final var str = new StringBuilder(Strings.escapeRegExpCharacters(bracket));
		final var c = String.valueOf(str.charAt(str.length() - 1));
		if (!B_REGEXP.matcher(c).find()) {
			str.append("\\b");
		}
		str.insert(0, "^\\s*");
		return safeRegExp(str);
	}

	private static @Nullable Pattern safeRegExp(final CharSequence regex) {
		try {
			return Pattern.compile(regex.toString());
		} catch (final Exception ex) {
			LanguageConfigurationPlugin.logError("Failed to parse pattern: " + regex, ex);
			return null;
		}
	}
}
