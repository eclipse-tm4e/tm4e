/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm4e.core.internal.theme.css;

import org.eclipse.tm4e.core.internal.theme.css.util.AbstractCSSValue;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;

/**
 * <a href=
 * "https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/dom/Measure.java">github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/dom/Measure.java</a>
 */
final class Measure extends AbstractCSSValue {

	private final LexicalUnit value;

	Measure(final LexicalUnit value) {
		this.value = value;
	}

	/**
	 * Return a float representation of the receiver's value.
	 *
	 * @param valueType a short representing the value type, see {@link CSSValue#getCssValueType()}
	 */
	@Override
	public float getFloatValue(final short valueType) throws DOMException {
		// If it's actually a SAC_INTEGER return the integer value, callers tend to expect and cast
		// There is no getIntegerFloat(short)
		// TODO Not sure the purpose of arg valyeType, its not referenced in this method
		if (value.getLexicalUnitType() == LexicalUnit.SAC_INTEGER)
			return value.getIntegerValue();

		// TODO not sure what to do if it's not one of the lexical unit types that are specified in LexicalUnit#getFloatValue()
		// ie. SAC_DEGREE, SAC_GRADIAN, SAC_RADIAN, SAC_MILLISECOND, SAC_SECOND, SAC_HERTZ or SAC_KILOHERTZ
		return value.getFloatValue();
	}

	@Override
	public String getStringValue() throws DOMException {
		final short lexicalUnit = value.getLexicalUnitType();
		if (lexicalUnit == LexicalUnit.SAC_IDENT
				|| lexicalUnit == LexicalUnit.SAC_STRING_VALUE
				|| lexicalUnit == LexicalUnit.SAC_URI)
			return value.getStringValue();

		// TODO There are more cases to catch of getLexicalUnitType()
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public short getPrimitiveType() {
		return switch (value.getLexicalUnitType()) {
			case LexicalUnit.SAC_IDENT -> CSS_IDENT;
			case LexicalUnit.SAC_INTEGER, LexicalUnit.SAC_REAL -> CSS_NUMBER;
			case LexicalUnit.SAC_URI -> CSS_URI;
			case LexicalUnit.SAC_PERCENTAGE -> CSS_PERCENTAGE;
			case LexicalUnit.SAC_PIXEL -> CSS_PX;
			case LexicalUnit.SAC_CENTIMETER -> CSS_CM;
			case LexicalUnit.SAC_EM -> CSS_EMS;
			case LexicalUnit.SAC_EX -> CSS_EXS;
			case LexicalUnit.SAC_INCH -> CSS_IN;
			case LexicalUnit.SAC_STRING_VALUE -> CSS_STRING;
			case LexicalUnit.SAC_DIMENSION -> CSS_DIMENSION;
			case LexicalUnit.SAC_INHERIT -> CSS_INHERIT;

			// TODO don't think this is right, see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=278139
			case LexicalUnit.SAC_OPERATOR_COMMA -> CSS_CUSTOM;

			default -> throw new UnsupportedOperationException("NOT YET IMPLEMENTED - LexicalUnit type: " + value.getLexicalUnitType());
		};
	}

	@Override
	public String getCssText() {
		// TODO: All LexicalUnit.SAC_OPERATOR_* except for COMMA left undone for
		// now as it's not even clear whether they should be treated as measures
		// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=278139
		return switch (value.getLexicalUnitType()) {
			case LexicalUnit.SAC_INTEGER -> String.valueOf(value.getIntegerValue());
			case LexicalUnit.SAC_REAL -> String.valueOf(value.getFloatValue());
			case LexicalUnit.SAC_PERCENTAGE, LexicalUnit.SAC_PIXEL, LexicalUnit.SAC_CENTIMETER, LexicalUnit.SAC_EM, LexicalUnit.SAC_EX, //
					LexicalUnit.SAC_PICA, LexicalUnit.SAC_POINT, LexicalUnit.SAC_INCH, LexicalUnit.SAC_DEGREE //
					-> String.valueOf(value.getFloatValue()) + value.getDimensionUnitText();
			case LexicalUnit.SAC_URI -> "url(" + value.getStringValue() + ")";
			case LexicalUnit.SAC_OPERATOR_COMMA -> ",";
			case LexicalUnit.SAC_INHERIT -> "inherit";
			default -> value.getStringValue();
		};
	}
}
