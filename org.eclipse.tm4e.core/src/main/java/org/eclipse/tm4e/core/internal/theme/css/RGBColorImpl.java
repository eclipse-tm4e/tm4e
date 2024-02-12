/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.tm4e.core.internal.theme.css;

import org.eclipse.tm4e.core.internal.theme.css.util.AbstractCSSValue;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;

/**
 * <a href=
 * "https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/dom/RGBColorImpl.java">github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/dom/RGBColorImpl.java</a>
 */
final class RGBColorImpl extends AbstractCSSValue implements RGBColor {

	private final CSSPrimitiveValue red;
	private final CSSPrimitiveValue green;
	private final CSSPrimitiveValue blue;

	RGBColorImpl(final LexicalUnit lexicalUnit) {
		LexicalUnit nextUnit = lexicalUnit.getParameters();
		red = new Measure(nextUnit);
		nextUnit = nextUnit.getNextLexicalUnit().getNextLexicalUnit();
		green = new Measure(nextUnit);
		nextUnit = nextUnit.getNextLexicalUnit().getNextLexicalUnit();
		blue = new Measure(nextUnit);
	}

	@Override
	public CSSPrimitiveValue getRed() {
		return red;
	}

	@Override
	public CSSPrimitiveValue getGreen() {
		return green;
	}

	@Override
	public CSSPrimitiveValue getBlue() {
		return blue;
	}

	@Override
	public RGBColor getRGBColorValue() throws DOMException {
		return this;
	}

	@Override
	public short getPrimitiveType() {
		return CSS_RGBCOLOR;
	}

	@Override
	public String getCssText() {
		return "rgb(" + red.getCssText() + ", " + green.getCssText() + ", "
				+ blue.getCssText() + ")";
	}
}
