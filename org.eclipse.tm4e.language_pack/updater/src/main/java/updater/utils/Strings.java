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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sebastian Thomschke
 */
public abstract class Strings {

	public static String indent(final int spaces, final String input) {
		final String indentation = " ".repeat(spaces);
		final String[] lines = input.split("\\n");
		for (int i = 0; i < lines.length; i++) {
			if (!lines[i].isEmpty()) {
				lines[i] = indentation + lines[i];
			}
		}
		return String.join("\n", lines);
	}

	public static String join(final Collection<?> coll, final String separator) {
		return join(coll.stream(), separator);
	}

	public static String join(final Stream<?> stream, final String separator) {
		return stream.map(Objects::toString).collect(Collectors.joining(separator));
	}

	public static String render(String template, final Map<String, Object> variables) {
		for (final Map.Entry<String, Object> entry : variables.entrySet()) {
			final String key = "{" + entry.getKey() + "}";
			final String value = Objects.toString(entry.getValue());
			template = template.replace(key, value);
		}
		return template;
	}

	public static String render(final String template, final Consumer<Map<String, Object>> variablesProvider) {
		final var variables = new HashMap<String, Object>();
		variablesProvider.accept(variables);
		return render(template, variables);
	}

	public static String replaceSubstringBetween(final String searchIn, final String startTag, final String endTag,
			final String replaceWith) {
		return Pattern //
				.compile(Pattern.quote(startTag) + ".*" + Pattern.quote(endTag), Pattern.DOTALL) //
				.matcher(searchIn) //
				.replaceAll(startTag + replaceWith + endTag);
	}

	public static String removeLeadingDot(final String input) {
		if (input.startsWith("."))
			return input.substring(1);
		return input;
	}

}
