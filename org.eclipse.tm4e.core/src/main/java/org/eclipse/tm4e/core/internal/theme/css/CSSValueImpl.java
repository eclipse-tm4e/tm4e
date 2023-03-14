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

import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;

abstract class CSSValueImpl implements CSSPrimitiveValue {

	// W3C CSSValue API methods

	@Override
	public String getCssText() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public short getCssValueType() {
		return CSS_PRIMITIVE_VALUE;
	}

	@Override
	public void setCssText(@Nullable final String cssText) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	// W3C CSSPrimitiveValue API methods

	@Override
	public short getPrimitiveType() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public void setFloatValue(final short arg0, final float arg1) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public void setStringValue(final short arg0, @Nullable final String arg1) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	// Additional methods

	@Override
	public float getFloatValue(final short valueType) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}
}
