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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.ui.utils.PreferenceUtils;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class ColorManager {

	private static final ColorManager INSTANCE = new ColorManager();

	public static ColorManager getInstance() {
		return INSTANCE;
	}

	private final Map<RGB, Color> fColorTable;

	private ColorManager() {
		fColorTable = new HashMap<>(10);
	}

	public Color getColor(RGB rgb) {
		Color color = fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb.red, rgb.green, rgb.blue);
			fColorTable.put(rgb, color);
		}
		return color;
	}

	public void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext()) {
			e.next().dispose();
		}
	}

	/**
	 * Get the color from preferences store using a token.
	 *
	 * @param tokenId
	 *            name of the token
	 * @return Color matching token
	 */
	public Color getPreferenceEditorColor(String tokenId) {
		return getColor(stringToRGB(PreferenceUtils.getEditorsPreferenceStore().get(tokenId, "")));
	}

	/**
	 * The method verifies that a color is defined in a preferences store using a token.
	 *
	 * @param tokenId
	 *            name of the token
	 * @return color is user defined or not
	 */
	public boolean isColorUserDefined(String tokenId) {
		String systemDefaultToken = getSystemDefaultToken(tokenId);

		return "".equals(systemDefaultToken) || // returns true if system default token doesn't exists
				!PreferenceUtils.getEditorsPreferenceStore().getBoolean(systemDefaultToken, true);
	}

	/**
	 * Get high priority color in text editors.
	 * See Issue #176
	 * Priority: User defined > TM defined > Eclipse color
	 *
	 * @param themeColor
	 *            color defined in TM theme
	 * @param tokenId
	 *            name of the token for preferences store
	 * @return Highest priority color
	 */
	public Color getPriorityColor(Color themeColor, String tokenId) {
		Color prefColor = getPreferenceEditorColor(tokenId);

		if (isColorUserDefined(tokenId)) {
			return prefColor;
		}

		return themeColor != null ? themeColor : null;
	}

	/**
	 * Returns a token for the system default value of the given token.
	 *
	 * @param tokenId
	 *            name of the token
	 * @return system default token or empty string if doesn't exist
	 */
	private String getSystemDefaultToken(String tokenId) {
		switch (tokenId) {
		case AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND:
			return AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT;
		case AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND:
			return AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;
		case AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND:
			return AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT;
		case AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND:
			return AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT;
		default:
			return "";
		}
	}

	/**
	 * Convert String to RGB.
	 *
	 * @param value
	 *            string value of rgb
	 * @return RGB value
	 */
	private RGB stringToRGB(String value) {
		String[] rgbValues = value.split(",");
		return rgbValues.length == 3
				? new RGB(Integer.parseInt(rgbValues[0]), Integer.parseInt(rgbValues[1]), Integer.parseInt(rgbValues[2]))
				: new RGB(255, 255, 255);
	}

}
