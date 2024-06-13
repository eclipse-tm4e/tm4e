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
package org.eclipse.tm4e.ui.themes;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.assertNonNull;

import java.io.InputStream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.tm4e.core.internal.utils.StringUtils;
import org.eclipse.tm4e.core.registry.IThemeSource.ContentType;
import org.eclipse.tm4e.registry.TMResource;
import org.eclipse.tm4e.registry.XMLConstants;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.themes.TMThemeTokenProvider;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.themes.css.CSSTokenProvider;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * {@link ITheme} implementation.
 */
public class Theme extends TMResource implements ITheme {

	private static final String DARK_ATTR = "dark";
	private static final String DEFAULT_ATTR = "default";

	private @Nullable ITokenProvider tokenProvider;

	private final String id;
	private final String name;
	private boolean dark;
	private boolean isDefault;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public Theme() {
		name = "<set-by-gson>";
		id = "<set-by-gson>";
	}

	/**
	 * Constructor for manually registered themes
	 */
	public Theme(final String id, final String path, final String name, final boolean dark) {
		super(path);
		this.id = id;
		this.name = name;
		this.dark = dark;
		this.isDefault = false;
	}

	/**
	 * Constructor for extension point.
	 */
	public Theme(final IConfigurationElement ce) {
		super(ce);
		id = assertNonNull(ce.getAttribute(XMLConstants.ID_ATTR));
		name = assertNonNull(ce.getAttribute(XMLConstants.NAME_ATTR));
		dark = Boolean.parseBoolean(ce.getAttribute(DARK_ATTR));
		isDefault = Boolean.parseBoolean(ce.getAttribute(DEFAULT_ATTR));
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IToken getToken(final String textMateTokenType) {
		final ITokenProvider provider = getTokenProvider();
		return provider == null ? ITokenProvider.DEFAULT_TOKEN : provider.getToken(textMateTokenType);
	}

	private @Nullable Color getPriorityColor(final @Nullable Color themeColor, final String prefStoreKey) {
		// if the theme is light but Eclipse is in dark mode (or vice versa) we cannot use the pref settings and always use the theme colors
		return UI.isDarkEclipseTheme() == isDark()
				? ColorManager.getInstance().getPriorityColor(themeColor, prefStoreKey)
				: themeColor;
	}

	@Nullable
	@Override
	public Color getEditorForeground() {
		final ITokenProvider provider = getTokenProvider();
		return getPriorityColor(
				provider != null ? provider.getEditorForeground() : null,
				AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND);
	}

	@Nullable
	@Override
	public Color getEditorBackground() {
		final ITokenProvider provider = getTokenProvider();
		return getPriorityColor(
				provider != null ? provider.getEditorBackground() : null,
				AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
	}

	@Nullable
	@Override
	public Color getEditorSelectionForeground() {
		final ITokenProvider provider = getTokenProvider();
		return getPriorityColor(
				provider != null ? provider.getEditorSelectionForeground() : null,
				AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND);
	}

	@Nullable
	@Override
	public Color getEditorSelectionBackground() {
		final ITokenProvider provider = getTokenProvider();
		return getPriorityColor(
				provider != null ? provider.getEditorSelectionBackground() : null,
				AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND);
	}

	@Nullable
	@Override
	public Color getEditorCurrentLineHighlight() {
		final ITokenProvider provider = getTokenProvider();
		final Color themeColor = provider != null ? provider.getEditorCurrentLineHighlight() : null;
		final ColorManager manager = ColorManager.getInstance();
		return manager.isColorUserDefined(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)
				? manager.getPreferenceEditorColor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR)
				: themeColor;
	}

	@Nullable
	private ITokenProvider getTokenProvider() {
		if (tokenProvider == null || isModified()) {
			try (InputStream in = getInputStream()) {
				final var path = getPath();
				final String extension = path.substring(path.lastIndexOf('.') + 1).trim().toLowerCase();

				tokenProvider = switch (extension) {
					case "css" -> new CSSTokenProvider(in);
					case "json" -> new TMThemeTokenProvider(ContentType.JSON, in);
					case "yaml", "yaml-tmtheme", "yml" -> new TMThemeTokenProvider(ContentType.YAML, in);
					case "plist", "tmtheme", "xml" -> new TMThemeTokenProvider(ContentType.XML, in);
					default -> throw new IllegalArgumentException("Unsupported file type: " + path);
				};
			} catch (final Exception ex) {
				TMUIPlugin.logError(ex);
				return null;
			}
		}
		return tokenProvider;
	}

	private long lastModified = -1;
	private long lastModifiedRecheck = -1;

	private boolean isModified() {
		final long now = System.currentTimeMillis();
		if (now > lastModifiedRecheck) {
			lastModifiedRecheck = now + 5_000;
			final long oldModified = lastModified;
			lastModified = getLastModified();
			return lastModified != oldModified;
		}
		return false;
	}

	@Override
	public boolean isDark() {
		return dark;
	}

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public void initializeViewerColors(final StyledText styledText) {
		Color color = getEditorBackground();
		if (color != null) {
			styledText.setBackground(color);
		}

		color = getEditorForeground();
		if (color != null) {
			styledText.setForeground(color);
		}

		color = getEditorSelectionBackground();
		if (color != null) {
			styledText.setSelectionBackground(color);
		}

		color = getEditorSelectionForeground();
		if (color != null) {
			styledText.setSelectionForeground(color);
		}
	}

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> {
			sb.append("id=").append(getId());
			sb.append(",name=").append(getName());
			sb.append(",dark=").append(isDark());
			sb.append(",default=").append(isDefault());
		});
	}
}
