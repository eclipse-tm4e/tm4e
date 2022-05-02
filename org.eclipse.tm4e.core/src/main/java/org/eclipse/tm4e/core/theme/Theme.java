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
package org.eclipse.tm4e.core.theme;

import static org.eclipse.tm4e.core.internal.utils.MoreCollections.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.utils.CompareUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * TextMate theme.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/9157c7f869219dbaf9a5a5607f099c00fe694a29/src/theme.ts#L8">
 *      github.com/Microsoft/vscode-textmate/blob/master/src/theme.ts</a>
 */
public class Theme {

	private static final Splitter BY_COMMA_SPLITTER = Splitter.on(',');
	private static final Splitter BY_SPACE_SPLITTER = Splitter.on(' ');

	private static final Pattern RRGGBB = Pattern.compile("^#[0-9a-f]{6}", Pattern.CASE_INSENSITIVE);
	private static final Pattern RRGGBBAA = Pattern.compile("^#[0-9a-f]{8}", Pattern.CASE_INSENSITIVE);
	private static final Pattern RGB = Pattern.compile("^#[0-9a-f]{3}", Pattern.CASE_INSENSITIVE);
	private static final Pattern RGBA = Pattern.compile("^#[0-9a-f]{4}", Pattern.CASE_INSENSITIVE);

	public static Theme createFromRawTheme(@Nullable final IRawTheme source, @Nullable List<String> colorMap) {
		return createFromParsedTheme(parseTheme(source), colorMap);
	}

	public static Theme createFromParsedTheme(final List<ParsedThemeRule> source, @Nullable List<String> colorMap) {
		return resolveParsedThemeRules(source, colorMap);
	}

	private final ColorMap colorMap;
	private final ThemeTrieElement root;
	private final ThemeTrieElementRule defaults;
	private final Map<String /* scopeName */, List<ThemeTrieElementRule>> cache = new HashMap<>();

	public Theme(final ColorMap colorMap, final ThemeTrieElementRule defaults, final ThemeTrieElement root) {
		this.colorMap = colorMap;
		this.root = root;
		this.defaults = defaults;
	}

	public Set<String> getColorMap() {
		return this.colorMap.getColorMap();
	}

	public ThemeTrieElementRule getDefaults() {
		return this.defaults;
	}

	public List<ThemeTrieElementRule> match(final String scopeName) {
		if (!this.cache.containsKey(scopeName)) {
			this.cache.put(scopeName, this.root.match(scopeName));
		}
		return this.cache.get(scopeName);
	}

	/**
	 * Parse a raw theme into rules.
	 */
	public static List<ParsedThemeRule> parseTheme(@Nullable final IRawTheme source) {
		if (source == null) {
			return Collections.emptyList();
		}

		final List<IRawThemeSetting> settings = source.getSettings();
		if (settings == null) {
			return Collections.emptyList();
		}

		final List<ParsedThemeRule> result = new ArrayList<>();
		for (int i = 0, len = settings.size(); i < len; i++) {
			final var entry = settings.get(i);

			final var entrySetting = entry.getSetting();
			if (entrySetting == null) {
				continue;
			}

			final Object settingScope = entry.getScope();
			List<String> scopes;
			if (settingScope instanceof String) {
				String scope = (String) settingScope;

				// remove leading commas
				scope = scope.replaceAll("^[,]+", "");

				// remove trailing commas
				scope = scope.replaceAll("[,]+$", "");

				scopes = BY_COMMA_SPLITTER.splitToList(scope);
			} else if (settingScope instanceof List) {
				@SuppressWarnings("unchecked")
				final var settingScopes = (List<String>) settingScope;
				scopes = settingScopes;
			} else {
				scopes = Arrays.asList("");
			}

			int fontStyle = FontStyle.NotSet;
			final Object settingsFontStyle = entrySetting.getFontStyle();
			if (settingsFontStyle instanceof String) {
				fontStyle = FontStyle.None;

				final Iterable<String> segments = BY_SPACE_SPLITTER.split((String) settingsFontStyle);
				for (final String segment : segments) {
					switch (segment) {
					case "italic":
						fontStyle = fontStyle | FontStyle.Italic;
						break;
					case "bold":
						fontStyle = fontStyle | FontStyle.Bold;
						break;
					case "underline":
						fontStyle = fontStyle | FontStyle.Underline;
						break;
					case "strikethrough":
						fontStyle = fontStyle | FontStyle.Strikethrough;
						break;
					}
				}
			}

			String foreground = null;
			final Object settingsForeground = entrySetting.getForeground();
			if (settingsForeground instanceof String && isValidHexColor((String) settingsForeground)) {
				foreground = (String) settingsForeground;
			}

			String background = null;
			final Object settingsBackground = entrySetting.getBackground();
			if (settingsBackground instanceof String && isValidHexColor((String) settingsBackground)) {
				background = (String) settingsBackground;
			}

			for (int j = 0, lenJ = scopes.size(); j < lenJ; j++) {
				final String _scope = scopes.get(j).trim();

				final List<String> segments = BY_SPACE_SPLITTER.splitToList(_scope);

				final String scope = getLastElement(segments);
				List<String> parentScopes = null;
				if (segments.size() > 1) {
					parentScopes = segments.subList(0, segments.size() - 1);
					parentScopes = Lists.reverse(parentScopes);
				}

				result.add(new ParsedThemeRule(scope, parentScopes, i, fontStyle, foreground, background));
			}
		}

		return result;
	}

	/**
	 * Resolve rules (i.e. inheritance).
	 */
	public static Theme resolveParsedThemeRules(final List<ParsedThemeRule> _parsedThemeRules,
			@Nullable List<String> _colorMap) {

		// copy the list since we cannot be sure the given list is mutable
		final var parsedThemeRules = new ArrayList<>(_parsedThemeRules);

		// Sort rules lexicographically, and then by index if necessary
		parsedThemeRules.sort((a, b) -> {
			int r = CompareUtils.strcmp(a.scope, b.scope);
			if (r != 0) {
				return r;
			}
			r = CompareUtils.strArrCmp(a.parentScopes, b.parentScopes);
			if (r != 0) {
				return r;
			}
			return a.index - b.index;
		});

		// Determine defaults
		int defaultFontStyle = FontStyle.None;
		String defaultForeground = "#000000";
		String defaultBackground = "#ffffff";
		while (!parsedThemeRules.isEmpty() && parsedThemeRules.get(0).scope.isEmpty()) {
			final var incomingDefaults = parsedThemeRules.remove(0);
			if (incomingDefaults.fontStyle != FontStyle.NotSet) {
				defaultFontStyle = incomingDefaults.fontStyle;
			}
			if (incomingDefaults.foreground != null) {
				defaultForeground = incomingDefaults.foreground;
			}
			if (incomingDefaults.background != null) {
				defaultBackground = incomingDefaults.background;
			}
		}
		final var colorMap = new ColorMap(_colorMap);
		final var defaults = new ThemeTrieElementRule(0, null, defaultFontStyle, colorMap.getId(defaultForeground),
				colorMap.getId(defaultBackground));

		final var root = new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, 0, 0),
				Collections.emptyList());
		for (final var rule : parsedThemeRules) {
			root.insert(0, rule.scope, rule.parentScopes, rule.fontStyle, colorMap.getId(rule.foreground),
					colorMap.getId(rule.background));
		}

		return new Theme(colorMap, defaults, root);
	}

	private static boolean isValidHexColor(final String hex) {
		if (hex.isEmpty()) {
			return false;
		}

		if (RRGGBB.matcher(hex).matches()) {
			// #rrggbb
			return true;
		}

		if (RRGGBBAA.matcher(hex).matches()) {
			// #rrggbbaa
			return true;
		}

		if (RGB.matcher(hex).matches()) {
			// #rgb
			return true;
		}

		if (RGBA.matcher(hex).matches()) {
			// #rgba
			return true;
		}

		return false;
	}

	@Nullable
	public String getColor(final int id) {
		return this.colorMap.getColor(id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cache.hashCode();
		result = prime * result + colorMap.hashCode();
		result = prime * result + defaults.hashCode();
		result = prime * result + root.hashCode();
		return result;
	}

	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final Theme other = (Theme) obj;
		return Objects.equals(cache, other.cache)
				&& Objects.equals(colorMap, other.colorMap)
				&& Objects.equals(defaults, other.defaults)
				&& Objects.equals(root, other.root);
	}
}
