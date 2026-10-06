/**
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.theme.css;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.theme.RGB;

/**
 * Parses the subset of CSS used by TextMate themes into styles.
 * Unknown properties, unknown at-rules, malformed declarations and rules with a malformed selector are skipped,
 * unsupported selectors and values reject the stylesheet with an {@link IllegalArgumentException}.
 */
public final class CSSStyleSheetParser {

	public static List<CSSStyle> parse(final String css) {
		return new CSSStyleSheetParser(css).parseRules();
	}

	private static boolean isIdentChar(final char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '-' || c == '_' || c >= 0x80;
	}

	private final String css;
	private final List<CSSStyle> styles = new ArrayList<>();
	private int pos;
	private boolean malformedSelector;

	private CSSStyleSheetParser(final String css) {
		this.css = css;
	}

	private char peek() {
		return pos < css.length() ? css.charAt(pos) : '\0';
	}

	private boolean atEnd() {
		return pos >= css.length();
	}

	private boolean startsWith(final String s) {
		return css.startsWith(s, pos);
	}

	private IllegalArgumentException error(final String message) {
		return new IllegalArgumentException(message + " at offset " + pos);
	}

	private void skipComments() {
		while (startsWith("/*")) {
			final int end = css.indexOf("*/", pos + 2);
			pos = end < 0 ? css.length() : end + 2;
		}
	}

	/** Skips whitespace and comments. */
	private void skipWhitespace() {
		for (;;) {
			skipComments();
			if (atEnd() || !Character.isWhitespace(peek())) {
				return;
			}
			pos++;
		}
	}

	private String readIdent() {
		final var ident = new StringBuilder();
		while (!atEnd()) {
			final char c = peek();
			if (c == '\\' && pos + 1 < css.length()) {
				ident.append(css.charAt(pos + 1));
				pos += 2;
			} else if (isIdentChar(c)) {
				ident.append(c);
				pos++;
			} else {
				break;
			}
		}
		return ident.toString();
	}

	private void skipString() {
		final char quote = css.charAt(pos++);
		while (!atEnd() && peek() != quote) {
			pos += peek() == '\\' ? 2 : 1;
		}
		pos = Math.min(pos + 1, css.length());
	}

	/** Skips to the end of a block whose opening brace was already consumed. */
	private void skipBlock() {
		int depth = 1;
		while (!atEnd() && depth > 0) {
			final char c = peek();
			if (c == '"' || c == '\'') {
				skipString();
			} else if (startsWith("/*")) {
				skipComments();
			} else {
				depth += c == '{' ? 1 : c == '}' ? -1 : 0;
				pos++;
			}
		}
	}

	private List<CSSStyle> parseRules() {
		for (;;) {
			skipWhitespace();
			if (atEnd()) {
				return styles;
			}
			if (startsWith("<!--")) {
				pos += 4;
			} else if (startsWith("-->")) {
				pos += 3;
			} else if (peek() == ';' || peek() == '}') {
				pos++;
			} else if (peek() == '@') {
				parseAtRule();
			} else {
				parseRule();
			}
		}
	}

	private void parseAtRule() {
		pos++;
		final String name = readIdent();
		while (!atEnd() && peek() != ';' && peek() != '{') {
			if (peek() == '"' || peek() == '\'') {
				skipString();
			} else if (startsWith("/*")) {
				skipComments();
			} else {
				pos++;
			}
		}
		if (atEnd()) {
			return;
		}
		if (peek() == ';') {
			pos++;
			return;
		}
		pos++;
		if (!"media".equalsIgnoreCase(name)) {
			skipBlock();
		}
	}

	private void parseRule() {
		malformedSelector = false;
		final var selectors = new ArrayList<List<String>>();
		for (;;) {
			final List<String> classes = parseSelector();
			if (classes != null) {
				selectors.add(classes);
			}
			if (peek() != ',') {
				break;
			}
			pos++;
		}
		if (malformedSelector) {
			if (peek() == '{') {
				pos++;
				skipBlock();
			}
			return;
		}
		final var style = new CSSStyle(selectors);
		styles.add(style);
		if (atEnd()) {
			return;
		}
		if (peek() != '{') {
			throw error("Unsupported selector syntax '" + peek() + "'");
		}
		pos++;
		parseDeclarations(style);
	}

	/** Returns the class names of a selector, an empty list for an element selector or null if there is none. */
	private @Nullable List<String> parseSelector() {
		final var classes = new ArrayList<String>();
		skipWhitespace();
		boolean found = false;
		if (peek() == '*') {
			pos++;
			found = true;
		} else if (!readIdent().isEmpty()) {
			found = true;
		}
		for (;;) {
			skipComments();
			if (peek() != '.') {
				break;
			}
			pos++;
			final String name = readIdent();
			if (name.isEmpty()) {
				malformedSelector = true;
			}
			classes.add(name);
			found = true;
		}
		skipWhitespace();
		final char c = peek();
		if (c != ',' && c != '{' && !atEnd()) {
			throw error("Unsupported selector syntax '" + c + "'");
		}
		return found ? classes : null;
	}

	private void parseDeclarations(final CSSStyle style) {
		for (;;) {
			skipWhitespace();
			if (atEnd()) {
				return;
			}
			final char c = peek();
			if (c == '}') {
				pos++;
				return;
			}
			if (c == '{') {
				pos++;
				skipBlock();
			} else if (c == ';') {
				pos++;
			} else {
				parseDeclaration(style);
			}
		}
	}

	private void parseDeclaration(final CSSStyle style) {
		final String name = readIdent();
		skipWhitespace();
		if (name.isEmpty() || peek() != ':') {
			readValue();
			return;
		}
		pos++;
		apply(style, name, readValue().trim());
	}

	/** Reads up to the next top-level semicolon or closing brace, dropping comments and nested blocks. */
	private String readValue() {
		final var value = new StringBuilder();
		int parens = 0;
		while (!atEnd()) {
			final char c = peek();
			if (c == '}' || parens == 0 && c == ';') {
				break;
			}
			if (c == '"' || c == '\'') {
				final int start = pos;
				skipString();
				value.append(css, start, pos);
			} else if (startsWith("/*")) {
				skipComments();
			} else if (c == '{') {
				pos++;
				skipBlock();
			} else {
				parens += c == '(' ? 1 : c == ')' && parens > 0 ? -1 : 0;
				value.append(c);
				pos++;
			}
		}
		return value.toString();
	}

	private void apply(final CSSStyle style, final String property, final String value) {
		switch (property) {
			case "color" -> {
				final RGB color = parseColor(value);
				if (color != null) {
					style.setColor(color);
				}
			}
			case "background-color" -> {
				final RGB color = parseColor(value);
				if (color != null) {
					style.setBackgroundColor(color);
				}
			}
			case "font-weight" -> {
				final String text = keywordOf(value);
				if (text != null) {
					style.setBold(text.contains("BOLD"));
				}
			}
			case "font-style" -> {
				final String text = keywordOf(value);
				if (text != null) {
					style.setItalic(text.contains("ITALIC"));
				}
			}
			case "text-decoration" -> {
				final String text = keywordOf(value);
				if (text != null && text.contains("UNDERLINE")) {
					style.setUnderline(true);
				}
				if (text != null && text.contains("LINE-THROUGH")) {
					style.setStrikeThrough(true);
				}
			}
			default -> {
			}
		}
	}

	/** Returns the upper-cased first identifier or string of the value, or null if there is none. */
	private static @Nullable String keywordOf(final String value) {
		if (value.isEmpty()) {
			return null;
		}
		final char first = value.charAt(0);
		if (first == '"' || first == '\'') {
			final int end = value.indexOf(first, 1);
			return value.substring(1, end < 0 ? value.length() : end).toUpperCase();
		}
		int end = 0;
		while (end < value.length() && isIdentChar(value.charAt(end))) {
			end++;
		}
		if (end == 0 || end < value.length() && value.charAt(end) == '(') {
			return null;
		}
		if (first >= '0' && first <= '9') {
			throw new IllegalArgumentException("Unsupported CSS value '" + value + "'");
		}
		return value.substring(0, end).toUpperCase();
	}

	private static @Nullable RGB parseColor(final String value) {
		if (value.isEmpty()) {
			return null;
		}
		if (value.charAt(0) == '#') {
			return parseHexColor(value);
		}
		if (value.charAt(0) == '-') {
			return null;
		}
		int end = 0;
		while (end < value.length() && isIdentChar(value.charAt(end))) {
			end++;
		}
		final String ident = value.substring(0, end);
		if (end < value.length() && value.charAt(end) == '(') {
			if (!"rgb".equalsIgnoreCase(ident) && !"rgba".equalsIgnoreCase(ident)) {
				return null;
			}
			final int close = value.indexOf(')', end);
			return close < 0 ? null : parseRgbArguments(value.substring(end + 1, close));
		}
		final char first = value.charAt(0);
		if (ident.isEmpty() && first != '"' && first != '\'') {
			return null;
		}
		final RGB color = ident.isEmpty() ? null : CSSColors.getByName(ident);
		if (color == null) {
			throw new IllegalArgumentException("Unknown CSS color '" + value + "'");
		}
		return color;
	}

	private static @Nullable RGB parseHexColor(final String value) {
		int end = 1;
		while (end < value.length() && Character.digit(value.charAt(end), 16) >= 0) {
			end++;
		}
		if (end < value.length() && isIdentChar(value.charAt(end))) {
			return null;
		}
		final String hex = value.substring(1, end);
		if (hex.length() == 3) {
			return new RGB(Character.digit(hex.charAt(0), 16) * 17, Character.digit(hex.charAt(1), 16) * 17,
					Character.digit(hex.charAt(2), 16) * 17);
		}
		if (hex.length() == 6) {
			return new RGB(hexByte(hex, 0), hexByte(hex, 2), hexByte(hex, 4));
		}
		return null;
	}

	private static int hexByte(final String hex, final int index) {
		return Character.digit(hex.charAt(index), 16) * 16 + Character.digit(hex.charAt(index + 1), 16);
	}

	/** Returns null for a syntactically malformed argument list and throws for an unsupported one. */
	private static @Nullable RGB parseRgbArguments(final String arguments) {
		final String[] parts = arguments.split(",", -1);
		for (final String part : parts) {
			if (part.isBlank()) {
				return null;
			}
		}
		if (parts.length < 3) {
			throw new IllegalArgumentException("Expected at least three color components in 'rgb(" + arguments + ")'");
		}
		final Integer red = parseChannel(parts[0]);
		final Integer green = parseChannel(parts[1]);
		final Integer blue = parseChannel(parts[2]);
		return red == null || green == null || blue == null ? null : new RGB(red, green, blue);
	}

	/**
	 * Parses the leading number of a color component and ignores a trailing unit such as {@code %}. Returns null for a
	 * malformed number and throws if the component is not a number.
	 */
	private static @Nullable Integer parseChannel(final String text) {
		final String s = text.trim();
		int end = 0;
		if (end < s.length() && (s.charAt(end) == '+' || s.charAt(end) == '-')) {
			end++;
		}
		final int digitsStart = end;
		while (end < s.length() && (Character.isDigit(s.charAt(end)) || s.charAt(end) == '.')) {
			end++;
		}
		if (end == digitsStart) {
			if (end > 0) {
				return null;
			}
			throw new IllegalArgumentException("Invalid color component '" + s + "'");
		}
		try {
			return (int) Float.parseFloat(s.substring(0, end));
		} catch (final NumberFormatException ex) {
			return null;
		}
	}
}
