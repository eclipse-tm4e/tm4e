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
package org.eclipse.tm4e.core.internal.theme.css.util;

import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;

/**
 * @see <a href=
 *      "https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/dom/CSSValueImpl.java">github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/dom/CSSValueImpl.java</a>
 */
public abstract class AbstractCSSValue implements CSSPrimitiveValue {

	private static final String NOT_YET_IMPLEMENTED = "NOT YET IMPLEMENTED";

	// W3C CSSValue API methods

	@Override
	public String getCssText() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public short getCssValueType() {
		return CSS_PRIMITIVE_VALUE;
	}

	@Override
	public void setCssText(final @Nullable String cssText) throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	// W3C CSSPrimitiveValue API methods

	@Override
	public short getPrimitiveType() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public Counter getCounterValue() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "COUNTER_ERROR");
	}

	@Override
	public RGBColor getRGBColorValue() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "RGBCOLOR_ERROR");
	}

	@Override
	public Rect getRectValue() throws DOMException {
		throw new DOMException(DOMException.INVALID_ACCESS_ERR, "RECT_ERROR");
	}

	@Override
	public String getStringValue() throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public void setFloatValue(final short arg0, final float arg1) throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public void setStringValue(final short arg0, final @Nullable String arg1) throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	// Additional methods

	@Override
	public float getFloatValue(final short valueType) throws DOMException {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}
}
