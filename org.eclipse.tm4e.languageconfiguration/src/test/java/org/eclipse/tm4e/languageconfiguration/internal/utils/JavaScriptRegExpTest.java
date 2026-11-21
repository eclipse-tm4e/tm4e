/**
 * Copyright (c) 2026 Sergii Zolotarov and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.languageconfiguration.internal.utils;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

class JavaScriptRegExpTest {

	/** Returns all matches, as the JavaScript method String.match() with the flag g does. */
	private static List<String> words(final String source, final @Nullable String flags, final String text) {
		final var matcher = JavaScriptRegExp.toPattern(source, flags).matcher(text);
		final var words = new ArrayList<String>();
		while (matcher.find()) {
			words.add(matcher.group());
		}
		return words;
	}

	@Test
	void unicodePropertyNames() {
		assertThat(JavaScriptRegExp.toPattern("\\p{Alphabetic}\\p{Number}\\p{Nonspacing_Mark}", null).pattern())
				.isEqualTo("\\p{IsAlphabetic}\\p{N}\\p{Mn}");
		assertThat(JavaScriptRegExp.toPattern("\\P{Letter}\\p{L}\\p{Lu}\\p{White_Space}", null).pattern())
				.isEqualTo("\\P{L}\\p{L}\\p{Lu}\\p{IsWhite_Space}");
		assertThat(JavaScriptRegExp.toPattern("\\p{General_Category=Uppercase_Letter}\\p{gc=Lu}\\p{Script=Greek}", null)
				.pattern()).isEqualTo("\\p{gc=Lu}\\p{gc=Lu}\\p{Script=Greek}");
		assertThat(words("[\\p{Letter}_]+", "u", "é_ñ 1")).containsExactly("é_ñ");
		assertThat(words("\\p{Script=Greek}+", "u", "abc αβγ")).containsExactly("αβγ");
	}

	@Test
	void whiteSpaceContainsUnicodeSpaces() {
		// JavaScript \s contains the no-break space U+00A0 and the ideographic space U+3000, Java \s does not
		assertThat(words("[^\\s]+", null, "a b　c d")).containsExactly("a", "b", "c", "d");
		assertThat(words("\\S+", null, "a b　c d")).containsExactly("a", "b", "c", "d");
		assertThat(words("\\s", null, "a b")).containsExactly(" ");
	}

	@Test
	void backReferenceToOptionalGroup() {
		// In JavaScript, a back-reference to a group that did not match is empty. Pattern of markdown_latex_combined:
		final String source = "([*_]{1,2})?(\\p{Alphabetic}|\\p{Number}|\\p{Nonspacing_Mark})"
				+ "(((\\p{Alphabetic}|\\p{Number}|\\p{Nonspacing_Mark})|[_])?(\\p{Alphabetic}|\\p{Number}|\\p{Nonspacing_Mark}))*\\1";
		assertThat(words(source, "u", "hello *bold* __it__ x")).containsExactly("hello", "*bold*", "__it__", "x");
		assertThat(words("(?<q>['\"])??x\\k<q>", null, "x 'x' \"x\"")).containsExactly("x", "'x'", "\"x\"");
		// without a back-reference the pattern does not change
		assertThat(JavaScriptRegExp.toPattern("(a)?b", null).pattern()).isEqualTo("(a)?b");
	}

	@Test
	void whiteSpaceInCharacterClass() {
		assertThat(words("[\\s,]+", null, "a,\u00A0b")).containsExactly(",\u00A0");
		assertThat(words("[(\\s]", null, "a(b")).containsExactly("(");
	}

	@Test
	void escapedBackslashIsNotAnEscapeSequence() {
		// \\ is a literal backslash, so the \ before p{Letter} starts a new escape sequence
		assertThat(JavaScriptRegExp.toPattern("\\\\\\p{Letter}", null).pattern()).isEqualTo("\\\\\\p{L}");
		assertThat(words("\\\\\\p{Letter}", null, "\\a")).containsExactly("\\a");
		assertThat(words("\\\\s", null, "\\s s")).containsExactly("\\s");
	}

	@Test
	void flags() {
		assertThat(words("[a-z]+", "i", "ABC")).containsExactly("ABC");
		assertThat(words("[a-z]+", "ug", "ABC")).isEmpty();
		assertThat(words("é", "i", "É")).containsExactly("É");
		assertThatThrownBy(() -> JavaScriptRegExp.toPattern("(", null)).isInstanceOf(PatternSyntaxException.class);
		assertThat(JavaScriptRegExp.toPattern("a", "i").flags() & Pattern.CASE_INSENSITIVE).isNotZero();
	}
}
