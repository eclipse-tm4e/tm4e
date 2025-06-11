/**
 * Copyright (c) 2023 Sebastian Thomschke and contributors.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * based on https://github.com/sebthom/extra-syntax-highlighting-eclipse-plugin/blob/main/plugin/updater
 */
package updater.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.FileLoader;

/**
 * @author Sebastian Thomschke
 */
public abstract class Strings {

	private static final PebbleEngine PEBBLE = new PebbleEngine.Builder().loader(new FileLoader() {
		// using FileLoader is a workaround because ClasspathLoader for some reason does not find templates on GHA/Linux
		{
			setPrefix("src/main/resources");
		}
	}).build();

	public static String indent(final int spaces, final String input) {
		final String indentation = " ".repeat(spaces);
		final String[] lines = input.split("\\n");
		for (int i = 0; i < lines.length; i++) {
			if (!lines[i].isBlank()) {
				lines[i] = indentation + lines[i];
			}
		}
		return String.join("\n", lines);
	}

	public static String stripToEmpty(final String input) {
		if (input == null)
			return "";
		return input.strip();
	}

	public static String join(final Collection<?> coll, final String separator) {
		return join(coll.stream(), separator);
	}

	public static String join(final Stream<?> stream, final String separator) {
		return stream.map(Objects::toString).collect(Collectors.joining(separator));
	}

	public static String normalizeNewlines(final String input) {
		if (input == null || input.isEmpty())
			return input;
		final var out = input.replace("\r\n", "\n");
		if ("\n".equals(System.lineSeparator()))
			return out;
		return out.replace("\n", "\r\n");
	}

	public static String render(final String template, final Map<String, Object> variables) {
		final var t = PEBBLE.getTemplate(template);
		final var writer = new StringWriter();
		try {
			t.evaluate(writer, variables);
		} catch (final IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return writer.toString();
	}

	public static String replaceSubstringBetween(final String searchIn, final String startTag, final String endTag,
			final String replaceWith) {
		return Pattern //
				.compile(Pattern.quote(startTag) + ".*" + Pattern.quote(endTag), Pattern.DOTALL) //
				.matcher(searchIn) //
				.replaceAll(Matcher.quoteReplacement(startTag + replaceWith + endTag));
	}

	public static String removeLeadingDot(final String input) {
		if (input.startsWith("."))
			return input.substring(1);
		return input;
	}

	public static String sanitizeFilename(final String input) {
		if (input.isEmpty() || ".".equals(input) || "..".equals(input))
			return "_";

		return input.replaceAll("[<>:\"/\\\\|?*\\p{Cntrl}\\s]", "_");
	}

	/**
	 * Splits the command string using shell-like syntax.
	 *
	 * <li>Inspired by https://docs.python.org/3/library/shlex.html#shlex.split
	 * <li>Based on https://gist.github.com/raymyers/8077031
	 */
	public static List<String> splitLikeShell(final CharSequence command) {
		final List<String> args = new ArrayList<>();
		boolean isNextCharEscaped = false;
		char quoteChar = 0;
		boolean isQuoting = false;
		int lastClosingQuoteIdx = Integer.MIN_VALUE;
		final var arg = new StringBuilder();
		char prevChar = ' ';
		for (int i = 0; i < command.length(); i++) {
			final char ch = command.charAt(i);
			if (isNextCharEscaped) {
				arg.append(ch);
				isNextCharEscaped = false;
			} else if (ch == '\\' && !(isQuoting && quoteChar == '\'')) {
				isNextCharEscaped = true;
			} else if (isQuoting && ch == quoteChar) {
				isQuoting = false;
				lastClosingQuoteIdx = i;
			} else if (!isQuoting && ch == '#' && Character.isWhitespace(prevChar)) {
				// ignore trailing comment
				break;
			} else if (!isQuoting && (ch == '\'' || ch == '"')) {
				isQuoting = true;
				quoteChar = ch;
			} else if (!isQuoting && Character.isWhitespace(ch)) {
				if (lastClosingQuoteIdx == i - 1 || arg.length() > 0) {
					args.add(arg.toString());
					arg.setLength(0);
				}
			} else {
				arg.append(ch);
			}
			prevChar = ch;
		}
		if (arg.length() > 0 || lastClosingQuoteIdx == command.length() - 1) {
			args.add(arg.toString());
		}
		return args;
	}
}
