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
package org.eclipse.tm4e.ui.themes;

import java.io.InputStream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.tm4e.registry.TMResource;
import org.eclipse.tm4e.registry.XMLConstants;
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

	private ITokenProvider tokenProvider;

	private String id;
	private String name;
	private boolean dark;
	private boolean isDefault;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public Theme() {
		super();
	}

	/**
	 * Constructor for extension point.
	 * 
	 * @param element
	 */
	public Theme(String id, String path, String name, boolean dark, boolean isDefault) {
		super(path);
		this.id = id;
		this.name = name;
		this.dark = dark;
		this.isDefault = isDefault;
	}

	public Theme(IConfigurationElement ce) {
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
	public IToken getToken(String type) {
		ITokenProvider provider = getTokenProvider();
		return provider != null ? provider.getToken(type) : null;
	}

	@Override
	public Color getEditorForeground() {
		ITokenProvider provider = getTokenProvider();
		Color themeColor = provider != null ? provider.getEditorForeground() : null;
		return ColorManager.getInstance()
				.getPriorityColor(themeColor, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND);
	}

	@Override
	public Color getEditorBackground() {
		ITokenProvider provider = getTokenProvider();
		Color themeColor = provider != null ? provider.getEditorBackground() : null;
		return ColorManager.getInstance()
				.getPriorityColor(themeColor, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
	}

	@Override
	public Color getEditorSelectionForeground() {
		ITokenProvider provider = getTokenProvider();
		Color themeColor = provider != null ? provider.getEditorSelectionForeground() : null;
		return ColorManager.getInstance()
				.getPriorityColor(themeColor, AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND);
	}

	@Override
	public Color getEditorSelectionBackground() {
		ITokenProvider provider = getTokenProvider();
		Color themeColor = provider != null ? provider.getEditorSelectionBackground() : null;
		return ColorManager.getInstance()
				.getPriorityColor(themeColor, AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND);
	}

	@Override
	public Color getEditorCurrentLineHighlight() {
		ITokenProvider provider = getTokenProvider();
		Color themeColor = provider != null ? provider.getEditorCurrentLineHighlight() : null;
		ColorManager manager = ColorManager.getInstance();
		return manager.isColorUserDefined(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)
				? manager.getPreferenceEditorColor(PreferenceConstants.EDITOR_CURRENTLINE_HIGHLIGHT)
				: themeColor;
	}

	private ITokenProvider getTokenProvider() {
		if (tokenProvider == null) {
			try {
				InputStream in = super.getInputStream();
				if (in == null) {
					return null;
				}
				tokenProvider = new CSSTokenProvider(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tokenProvider;
	}

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
	public void initializeViewerColors(StyledText styledText) {
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
}
