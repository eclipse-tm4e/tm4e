/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.theme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.tm4e.core.internal.utils.CompareUtils;

/**
 * TextMate theme.
 *
 */
public class Theme {

	private static final Pattern rrggbb = Pattern.compile("^#[0-9a-f]{6}", Pattern.CASE_INSENSITIVE);
	private static final Pattern rrggbbaa = Pattern.compile("^#[0-9a-f]{8}", Pattern.CASE_INSENSITIVE);
	private static final Pattern rgb = Pattern.compile("^#[0-9a-f]{3}", Pattern.CASE_INSENSITIVE);
	private static final Pattern rgba = Pattern.compile("^#[0-9a-f]{4}", Pattern.CASE_INSENSITIVE);

	private final ColorMap _colorMap;
	private final ThemeTrieElement _root;
	private final ThemeTrieElementRule _defaults;
	private final Map<String /* scopeName */, List<ThemeTrieElementRule>> _cache;

	public static Theme createFromRawTheme(IRawTheme source) {
		return createFromParsedTheme(parseTheme(source));
	}

	public static List<ParsedThemeRule> parseTheme(IRawTheme source) {
		if (source == null || source.getSettings() == null) {
			return Collections.emptyList();
		}
		// if (!source.settings || !Array.isArray(source.settings)) {
		// return [];
		// }
		Collection<IRawThemeSetting> settings = source.getSettings();
		List<ParsedThemeRule> result = new ArrayList<>();
		int i = 0;
		for (IRawThemeSetting entry : settings) {

			if (entry.getSetting() == null) {
				continue;
			}

			Object settingScope = entry.getScope();
			List<String> scopes = new ArrayList<>();
			if (settingScope instanceof String) {
				String _scope = (String) settingScope;

				// remove leading commas
				_scope = _scope.replaceAll("^[,]+", "");

				// remove trailing commans
				_scope = _scope.replaceAll("[,]+$", "");

				scopes = Arrays.asList(_scope.split(","));
			} else if (settingScope instanceof List) {
				scopes = (List<String>) settingScope;
			} else {
				scopes.add("");
			}

			int fontStyle = FontStyle.NotSet;
			Object settingsFontStyle = entry.getSetting().getFontStyle();
			if (settingsFontStyle instanceof String) {
				fontStyle = FontStyle.None;

				String[] segments = ((String) settingsFontStyle).split(" ");
				for (int j = 0, lenJ = segments.length; j < lenJ; j++) {
					String segment = segments[j];
					if ("italic".equals(segment)) {
						fontStyle = fontStyle | FontStyle.Italic;
					} else if ("bold".equals(segment)) {
						fontStyle = fontStyle | FontStyle.Bold;
					} else if ("underline".equals(segment)) {
						fontStyle = fontStyle | FontStyle.Underline;
					}
				}
			}

			String foreground = null;
			Object settingsForeground = entry.getSetting().getForeground();
			if (settingsForeground instanceof String && isValidHexColor((String) settingsForeground)) {
				foreground = (String) settingsForeground;
			}

			String background = null;
			Object settingsBackground = entry.getSetting().getBackground();
			if (settingsBackground instanceof String && isValidHexColor((String) settingsBackground)) {
				background = (String) settingsBackground;
			}
			for (int j = 0, lenJ = scopes.size(); j < lenJ; j++) {
				String _scope = scopes.get(j).trim();

				List<String> segments = Arrays.asList(_scope.split(" "));

				String scope = segments.get(segments.size() - 1);
				List<String> parentScopes = null;
				if (segments.size() > 1) {
					parentScopes = segments.subList(0, segments.size() - 1);// slice(0,
																			// segments.length
																			// -
																			// 1);
					Collections.reverse(parentScopes); // parentScopes.reverse();
				}

				ParsedThemeRule t = new ParsedThemeRule(scope, parentScopes, i, fontStyle, foreground, background);
				result.add(t);
			}
			i++;
		}

		return result;
	}

	private static boolean isValidHexColor(String hex) {
		if (hex == null || hex.length() < 1) {
			return false;
		}

		if (rrggbb.matcher(hex).matches()) {
			// #rrggbb
			return true;
		}

		if (rrggbbaa.matcher(hex).matches()) {
			// #rrggbbaa
			return true;
		}

		if (rgb.matcher(hex).matches()) {
			// #rgb
			return true;
		}

		if (rgba.matcher(hex).matches()) {
			// #rgba
			return true;
		}

		return false;
	}

	public static Theme createFromParsedTheme(List<ParsedThemeRule> source) {
		return resolveParsedThemeRules(source);
	}

	/**
	 * Resolve rules (i.e. inheritance).
	 */
	public static Theme resolveParsedThemeRules(List<ParsedThemeRule> parsedThemeRules) {
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
		while (parsedThemeRules.size() >= 1 && "".equals(parsedThemeRules.get(0).scope)) {
			ParsedThemeRule incomingDefaults = parsedThemeRules.remove(0); // shift();
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
		ColorMap colorMap = new ColorMap();
		ThemeTrieElementRule defaults = new ThemeTrieElementRule(0, null, defaultFontStyle,
				colorMap.getId(defaultForeground), colorMap.getId(defaultBackground));

		ThemeTrieElement root = new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, 0, 0),
				Collections.emptyList());
		for (int i = 0, len = parsedThemeRules.size(); i < len; i++) {
			ParsedThemeRule rule = parsedThemeRules.get(i);
			root.insert(0, rule.scope, rule.parentScopes, rule.fontStyle, colorMap.getId(rule.foreground),
					colorMap.getId(rule.background));
		}

		return new Theme(colorMap, defaults, root);
	}

	public Theme(ColorMap colorMap, ThemeTrieElementRule defaults, ThemeTrieElement root) {
		this._colorMap = colorMap;
		this._root = root;
		this._defaults = defaults;
		this._cache = new HashMap<>();
	}

	public Set<String> getColorMap() {
		return this._colorMap.getColorMap();
	}

	public String getColor(int id) {
		return this._colorMap.getColor(id);
	}

	public ThemeTrieElementRule getDefaults() {
		return this._defaults;
	}

	public List<ThemeTrieElementRule> match(String scopeName) {
		if (!this._cache.containsKey(scopeName)) {
			this._cache.put(scopeName, this._root.match(scopeName));
		}
		return this._cache.get(scopeName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_cache == null) ? 0 : _cache.hashCode());
		result = prime * result + ((_colorMap == null) ? 0 : _colorMap.hashCode());
		result = prime * result + ((_defaults == null) ? 0 : _defaults.hashCode());
		result = prime * result + ((_root == null) ? 0 : _root.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Theme other = (Theme) obj;
		if (_cache == null) {
			if (other._cache != null)
				return false;
		} else if (!_cache.equals(other._cache))
			return false;
		if (_colorMap == null) {
			if (other._colorMap != null)
				return false;
		} else if (!_colorMap.equals(other._colorMap))
			return false;
		if (_defaults == null) {
			if (other._defaults != null)
				return false;
		} else if (!_defaults.equals(other._defaults))
			return false;
		if (_root == null) {
			if (other._root != null)
				return false;
		} else if (!_root.equals(other._root))
			return false;
		return true;
	}

}
