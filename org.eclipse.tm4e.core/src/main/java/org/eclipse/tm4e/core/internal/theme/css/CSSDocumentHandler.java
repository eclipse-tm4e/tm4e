/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Sebastian Thomschke (Vegard IT) - add support for named CSS colors
 */
package org.eclipse.tm4e.core.internal.theme.css;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.theme.css.util.AbstractDocumentHandler;
import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.RGB;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.css.CSSPrimitiveValue;

@NonNullByDefault({})
public final class CSSDocumentHandler extends AbstractDocumentHandler {

	private static RGB createRGB(final LexicalUnit value) {
		if (value.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			final String colorName = value.getStringValue();
			final RGB color = CSSColors.getByName(value.getStringValue());
			if (color == null)
				throw new IllegalArgumentException("Unkown CSS color '" + colorName + "'");
			return color;
		}
		final var rgbColor = new RGBColorImpl(value);
		final int green = (int) rgbColor.getGreen().getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
		final int red = (int) rgbColor.getRed().getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
		final int blue = (int) rgbColor.getBlue().getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
		return new RGB(red, green, blue);
	}

	private final List<IStyle> styles = new ArrayList<>();
	private @Nullable CSSStyle currentStyle;

	@Override
	public void endSelector(final SelectorList selector) throws CSSException {
		currentStyle = null;
	}

	@Override
	public void property(final String name, final LexicalUnit value, final boolean arg2) throws CSSException {
		final var currentStyle = this.currentStyle;
		if (currentStyle != null && name != null && value != null) {
			switch (name) {
				case "color":
					currentStyle.setColor(createRGB(value));
					break;
				case "background-color":
					currentStyle.setBackgroundColor(createRGB(value));
					break;
				case "font-weight":
					currentStyle.setBold(value.getStringValue().toUpperCase().contains("BOLD"));
					break;
				case "font-style":
					currentStyle.setItalic(value.getStringValue().toUpperCase().contains("ITALIC"));
					break;
				case "text-decoration":
					final String decoration = value.getStringValue().toUpperCase();
					if (decoration.contains("UNDERLINE")) {
						currentStyle.setUnderline(true);
					}
					if (decoration.contains("LINE-THROUGH")) {
						currentStyle.setStrikeThrough(true);
					}
					break;
			}
		}
	}

	@Override
	public void startSelector(final SelectorList selector) throws CSSException {
		currentStyle = new CSSStyle(selector);
		styles.add(currentStyle);
	}

	public List<IStyle> getStyles() {
		return styles;
	}
}
