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

import java.io.InputStream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.tm4e.core.internal.utils.StringUtils;
import org.eclipse.tm4e.registry.TMResource;
import org.eclipse.tm4e.registry.XMLConstants;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.ui.themes.css.CSSTokenProvider;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * {@link ITheme} implementation.
 *
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
	public Theme(final String id, final String path, final String name, final boolean dark, final boolean isDefault) {
		super(path);
		this.id = id;
		this.name = name;
		this.dark = dark;
		this.isDefault = isDefault;
	}

	/**
	 * Constructor for extension point.
	 */
	public Theme(final IConfigurationElement ce) {
		super(ce);
		id = ce.getAttribute(XMLConstants.ID_ATTR);
		name = ce.getAttribute(XMLConstants.NAME_ATTR);
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
	public IToken getToken(final String type) {
		final ITokenProvider provider = getTokenProvider();
		return provider == null ? ITokenProvider.DEFAULT_TOKEN : provider.getToken(type);
	}

	@Nullable
	@Override
	public Color getEditorForeground() {
		final ITokenProvider provider = getTokenProvider();
		final Color themeColor = provider != null ? provider.getEditorForeground() : null;
		return ColorManager.getInstance()
				.getPriorityColor(themeColor, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND);
	}

	@Nullable
	@Override
	public Color getEditorBackground() {
		final ITokenProvider provider = getTokenProvider();
		final Color themeColor = provider != null ? provider.getEditorBackground() : null;
		return ColorManager.getInstance()
				.getPriorityColor(themeColor, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
	}

	@Nullable
	@Override
	public Color getEditorSelectionForeground() {
		final ITokenProvider provider = getTokenProvider();
		final Color themeColor = provider != null ? provider.getEditorSelectionForeground() : null;
		return ColorManager.getInstance()
				.getPriorityColor(themeColor, AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND);
	}

	@Nullable
	@Override
	public Color getEditorSelectionBackground() {
		final ITokenProvider provider = getTokenProvider();
		final Color themeColor = provider != null ? provider.getEditorSelectionBackground() : null;
		return ColorManager.getInstance()
				.getPriorityColor(themeColor, AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND);
	}

	@Nullable
	@Override
	public Color getEditorCurrentLineHighlight() {
		final ITokenProvider provider = getTokenProvider();
		final Color themeColor = provider != null ? provider.getEditorCurrentLineHighlight() : null;
		final ColorManager manager = ColorManager.getInstance();
		return manager.isColorUserDefined(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)
				? manager.getPreferenceEditorColor(PreferenceConstants.EDITOR_CURRENTLINE_HIGHLIGHT)
				: themeColor;
	}

	@Nullable
	private ITokenProvider getTokenProvider() {
		if (tokenProvider == null) {
			try (InputStream in = super.getInputStream()) {
				tokenProvider = new CSSTokenProvider(in);
			} catch (final Exception ex) {
				TMUIPlugin.logError(ex);
				return null;
			}
		}
		return tokenProvider;
	}

	@Nullable
	@Override
	public String toCSSStyleSheet() {
		return super.getResourceContent();
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
