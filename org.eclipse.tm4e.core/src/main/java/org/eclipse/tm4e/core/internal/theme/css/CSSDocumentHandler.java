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
package org.eclipse.tm4e.core.internal.theme.css;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tm4e.core.theme.IStyle;
import org.eclipse.tm4e.core.theme.RGB;
import org.eclipse.tm4e.core.theme.css.CSSStyle;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;

public final class CSSDocumentHandler implements DocumentHandler {

	private final List<IStyle> list = new ArrayList<>();
	private CSSStyle currentStyle;

	@Override
	public void comment(final String arg0) throws CSSException {

	}

	@Override
	public void endDocument(final InputSource arg0) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endFontFace() throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endMedia(final SACMediaList arg0) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endPage(final String arg0, final String arg1) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endSelector(final SelectorList selector) throws CSSException {
		currentStyle = null;
	}

	@Override
	public void ignorableAtRule(final String arg0) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void importStyle(final String arg0, final SACMediaList arg1, final String arg2) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void namespaceDeclaration(final String arg0, final String arg1) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void property(final String name, final LexicalUnit value, final boolean arg2) throws CSSException {
		if (currentStyle != null && name != null) {
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

	private RGB createRGB(final LexicalUnit value) {
		final RGBColor rgbColor = new RGBColorImpl(value);
		final int green = ((int) rgbColor.getGreen().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
		final int red = ((int) rgbColor.getRed().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
		final int blue = ((int) rgbColor.getBlue().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
		return new RGB(red, green, blue);
	}

	@Override
	public void startDocument(final InputSource arg0) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startFontFace() throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startMedia(final SACMediaList arg0) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startPage(final String arg0, final String arg1) throws CSSException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startSelector(final SelectorList selector) throws CSSException {
		currentStyle = new CSSStyle(selector);
		list.add(currentStyle);
	}

	public List<IStyle> getList() {
		return list;
	}
}
