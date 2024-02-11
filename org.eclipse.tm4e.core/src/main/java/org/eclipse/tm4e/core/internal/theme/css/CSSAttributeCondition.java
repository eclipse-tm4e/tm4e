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

import org.eclipse.jdt.annotation.Nullable;

class CSSAttributeCondition extends AbstractAttributeCondition {

	/**
	 * The attribute's local name.
	 */
	private final @Nullable String localName;

	/**
	 * The attribute's namespace URI.
	 */
	private final @Nullable String namespaceURI;

	/**
	 * Whether this condition applies to specified attributes.
	 */
	private final boolean specified;

	protected CSSAttributeCondition(final @Nullable String localName, final @Nullable String namespaceURI, final boolean specified,
			final String value) {
		super(value);
		this.localName = localName;
		this.namespaceURI = namespaceURI;
		this.specified = specified;
	}

	@Nullable
	@Override
	public String getLocalName() {
		return localName;
	}

	@Nullable
	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public boolean getSpecified() {
		return specified;
	}

	@Override
	public short getConditionType() {
		return SAC_ATTRIBUTE_CONDITION;
	}

	@Override
	public int nbClass() {
		return 0;
	}

	@Override
	public int nbMatch(final String... names) {
		// TODO
		// String val = getValue();
		// if (val == null) {
		// return !e.getAttribute(getLocalName()).equals("");
		// }
		// return e.getAttribute(getLocalName()).equals(val);
		return 0;
	}

	@Override
	public String toString() {
		return "CSSAttr(" + getLocalName() + ")=='" + getValue() + "'";
	}
}
