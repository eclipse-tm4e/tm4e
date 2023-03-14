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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.ui.internal.utils.PreferenceUtils;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.google.common.base.Splitter;

public final class ColorManager {

	private static final Splitter BY_COMMA_SPLITTER = Splitter.on(',');

	private static final ColorManager INSTANCE = new ColorManager();

	public static ColorManager getInstance() {
		return INSTANCE;
	}

	private final Map<RGB, @Nullable Color> fColorTable = new HashMap<>(10);

	private ColorManager() {
	}

	public Color getColor(final RGB rgb) {
		Color color = fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb.red, rgb.green, rgb.blue);
			fColorTable.put(rgb, color);
		}
		return color;
	}

	public void dispose() {
		for (final var c : fColorTable.values()) {
			if (c != null)
				c.dispose();
		}
	}

	/**
	 * Get the color from preferences store using a token.
	 *
	 * @param tokenId name of the token
	 *
	 * @return Color matching token
	 */
	@Nullable
	public Color getPreferenceEditorColor(final String tokenId) {
		final var prefStore = PreferenceUtils.getEditorsPreferenceStore();
		if (prefStore == null)
			return null;
		return getColor(stringToRGB(prefStore.get(tokenId, "")));
	}

	/**
	 * The method verifies that a color is defined in a preferences store using a token.
	 *
	 * @param tokenId name of the token
	 *
	 * @return color is user defined or not
	 */
	public boolean isColorUserDefined(final String tokenId) {
		final var prefStore = PreferenceUtils.getEditorsPreferenceStore();
		if (prefStore == null)
			return false;

		final String systemDefaultToken = getSystemDefaultToken(tokenId);

		return "".equals(systemDefaultToken) || // returns true if system default token doesn't exists
				!prefStore.getBoolean(systemDefaultToken, true);
	}

	/**
	 * Get high priority color in text editors.
	 * See Issue #176
	 * Priority: User defined > TM defined > Eclipse color
	 *
	 * @param themeColor color defined in TM theme
	 * @param tokenId name of the token for preferences store
	 *
	 * @return Highest priority color
	 */
	@Nullable
	public Color getPriorityColor(@Nullable final Color themeColor, final String tokenId) {
		if (isColorUserDefined(tokenId)) {
			final Color prefColor = getPreferenceEditorColor(tokenId);
			return prefColor;
		}

		return themeColor != null ? themeColor : null;
	}

	/**
	 * Returns a token for the system default value of the given token.
	 *
	 * @param tokenId name of the token
	 *
	 * @return system default token or empty string if doesn't exist
	 */
	private String getSystemDefaultToken(final String tokenId) {
		return switch (tokenId) {
			case AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND -> AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT;
			case AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND -> AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;
			case AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND -> AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT;
			case AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND -> AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT;
			default -> "";
		};
	}

	/**
	 * Convert String to RGB.
	 *
	 * @param value string value of rgb
	 *
	 * @return RGB value
	 */
	private RGB stringToRGB(final String value) {
		final String[] rgbValues = BY_COMMA_SPLITTER.splitToStream(value).toArray(String[]::new);
		return rgbValues.length == 3
				? new RGB(Integer.parseInt(rgbValues[0]), Integer.parseInt(rgbValues[1]),
						Integer.parseInt(rgbValues[2]))
				: new RGB(255, 255, 255);
	}
}
