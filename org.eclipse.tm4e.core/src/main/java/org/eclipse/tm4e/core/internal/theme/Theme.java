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
package org.eclipse.tm4e.core.internal.theme;

import static org.eclipse.tm4e.core.internal.utils.MoreCollections.*;
import static org.eclipse.tm4e.core.internal.utils.StringUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.grammar.ScopeStack;
import org.eclipse.tm4e.core.internal.theme.raw.IRawTheme;
import org.eclipse.tm4e.core.internal.theme.raw.IRawThemeSetting;
import org.eclipse.tm4e.core.internal.utils.StringUtils;

/**
 * TextMate theme.
 *
 * Based on <a href="https://github.com/microsoft/vscode-textmate/blob/167bbbd509356cc4617f250c0d754aef670ab14a/src/theme.ts#L7">
 * github.com/microsoft/vscode-textmate/blob/main/src/theme.ts#Theme</a>.
 * <p>
 * See also <a href=
 * "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/supports/tokenization.ts#L192">
 * github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/supports/tokenization.ts#TokenTheme</a>
 */
public final class Theme {

	public static Theme createFromRawTheme(final @Nullable IRawTheme source, final @Nullable List<String> colorMap) {
		final var theme = createFromParsedTheme(parseTheme(source), colorMap);

		// custom tm4e code, not from upstream
		if (source != null) {
			theme.editorColors = source.getEditorColors();
		}

		return theme;
	}

	public static Theme createFromParsedTheme(final List<ParsedThemeRule> source, final @Nullable List<String> colorMap) {
		return resolveParsedThemeRules(source, colorMap);
	}

	private final ConcurrentMap<String /* scopeName */, List<ThemeTrieElementRule>> _cachedMatchRoot = new ConcurrentHashMap<>();

	private final ColorMap _colorMap;
	private final StyleAttributes _defaults;
	private final ThemeTrieElement _root;
	private Map<String, String> editorColors = Collections.emptyMap(); // custom tm4e code, not from upstream

	public Theme(final ColorMap colorMap, final StyleAttributes defaults, final ThemeTrieElement root) {
		this._colorMap = colorMap;
		this._root = root;
		this._defaults = defaults;
	}

	public List<String> getColorMap() {
		return this._colorMap.getColorMap();
	}

	public StyleAttributes getDefaults() {
		return this._defaults;
	}

	public Map<String, String> getEditorColors() { // custom tm4e code, not from upstream
		return editorColors;
	}

	public @Nullable StyleAttributes match(final @Nullable ScopeStack scopePath) {
		if (scopePath == null) {
			return this._defaults;
		}
		final var scopeName = scopePath.scopeName;
		final var matchingTrieElements = this._cachedMatchRoot.computeIfAbsent(scopeName, _root::match);

		final var effectiveRule = findFirstMatching(matchingTrieElements,
				v -> _scopePathMatchesParentScopes(scopePath.parent, v.parentScopes));
		if (effectiveRule == null) {
			return null;
		}

		return StyleAttributes.of(
				effectiveRule.fontStyle,
				effectiveRule.foreground,
				effectiveRule.background);
	}

	private boolean _scopePathMatchesParentScopes(@Nullable ScopeStack scopePath, final List<String> parentScopeNames) {
		if (parentScopeNames.isEmpty()) {
			return true;
		}

		// Starting with the deepest parent scope, look for a match in the scope path.
		final var parentScopeNamesLen = parentScopeNames.size();
		for (int index = 0; index < parentScopeNamesLen; index++) {
			var scopePattern = parentScopeNames.get(index);
			boolean scopeMustMatch = false;

			// Check for a child combinator (a parent-child relationship)
			if (">".equals(scopePattern)) {
				if (index == parentScopeNamesLen - 1) {
					// Invalid use of child combinator
					return false;
				}
				scopePattern = parentScopeNames.get(++index);
				scopeMustMatch = true;
			}

			while (scopePath != null) {
				if (_matchesScope(scopePath.scopeName, scopePattern)) {
					break;
				}
				if (scopeMustMatch) {
					// If a child combinator was used, the parent scope must match.
					return false;
				}
				scopePath = scopePath.parent;
			}

			if (scopePath == null) {
				// No more potential matches
				return false;
			}
			scopePath = scopePath.parent;
		}

		// All parent scopes were matched.
		return true;
	}

	private boolean _matchesScope(final String scopeName, final String scopeNamePattern) {
		return scopeNamePattern.equals(scopeName)
				|| scopeName.startsWith(scopeNamePattern) && scopeName.charAt(scopeNamePattern.length()) == '.';
	}

	/**
	 * Parse a raw theme into rules.
	 */
	public static List<ParsedThemeRule> parseTheme(final @Nullable IRawTheme source) {
		if (source == null)
			return Collections.emptyList();

		final var settings = source.getSettings();
		if (settings == null)
			return Collections.emptyList();

		final var result = new ArrayList<ParsedThemeRule>();
		int i = -1;
		for (final IRawThemeSetting entry : settings) {
			final var entrySetting = entry.getSetting();
			if (entrySetting == null) {
				continue;
			}

			i++;

			final Object settingScope = entry.getScope();
			final List<String> scopes;
			if (settingScope instanceof String _scope) {
				// remove leading commas
				_scope = _scope.replaceAll("^,+", "");

				// remove trailing commas
				_scope = _scope.replaceAll(",+$", "");

				scopes = StringUtils.splitToList(_scope, ',');
			} else if (settingScope instanceof List) {
				@SuppressWarnings("unchecked")
				final var settingScopes = (List<String>) settingScope;
				scopes = settingScopes;
			} else {
				scopes = List.of("");
			}

			int fontStyle = FontStyle.NotSet;
			final String settingsFontStyle = entrySetting.getFontStyle();
			if (settingsFontStyle != null) {
				fontStyle = FontStyle.None;

				final var segments = StringUtils.splitToArray(settingsFontStyle, ' ');
				for (final var segment : segments) {
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
			if (settingsForeground instanceof final String stringSettingsForeground
					&& isValidHexColor(stringSettingsForeground)) {
				foreground = stringSettingsForeground;
			}

			String background = null;
			final Object settingsBackground = entrySetting.getBackground();
			if (settingsBackground instanceof final String stringSettingsBackground
					&& isValidHexColor(stringSettingsBackground)) {
				background = stringSettingsBackground;
			}

			for (int j = 0, lenJ = scopes.size(); j < lenJ; j++) {
				final var _scope = scopes.get(j).trim();

				final var segments = StringUtils.splitToList(_scope, ' ');

				final var scope = getLastElement(segments);
				List<String> parentScopes = null;
				if (segments.size() > 1) {
					parentScopes = new ArrayList<>(segments.subList(0, segments.size() - 1));
					Collections.reverse(parentScopes);
				}

				result.add(new ParsedThemeRule(
						scope,
						parentScopes,
						i,
						fontStyle,
						foreground,
						background));
			}
		}

		return result;
	}

	/**
	 * Resolve rules (i.e. inheritance).
	 */
	public static Theme resolveParsedThemeRules(final List<ParsedThemeRule> _parsedThemeRules, final @Nullable List<String> _colorMap) {

		// copy the list since we cannot be sure the given list is mutable
		final var parsedThemeRules = new ArrayList<>(_parsedThemeRules);

		// Sort rules lexicographically, and then by index if necessary
		parsedThemeRules.sort((a, b) -> {
			int r = strcmp(a.scope, b.scope);
			if (r != 0) {
				return r;
			}
			r = strArrCmp(a.parentScopes, b.parentScopes);
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
		final var defaults = StyleAttributes.of(defaultFontStyle, colorMap.getId(defaultForeground), colorMap.getId(defaultBackground));

		final var root = new ThemeTrieElement(new ThemeTrieElementRule(0, null, FontStyle.NotSet, 0, 0), Collections.emptyList());
		for (final ParsedThemeRule rule : parsedThemeRules) {
			root.insert(0, rule.scope, rule.parentScopes, rule.fontStyle, colorMap.getId(rule.foreground), colorMap.getId(rule.background));
		}

		return new Theme(colorMap, defaults, root);
	}

	@Override
	public int hashCode() {
		int result = 31 + _colorMap.hashCode();
		result = 31 * result + _defaults.hashCode();
		return 31 * result + _root.hashCode();
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof final Theme other)
			return Objects.equals(_colorMap, other._colorMap)
					&& Objects.equals(_defaults, other._defaults)
					&& Objects.equals(_root, other._root);
		return false;
	}
}
