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
import org.w3c.css.sac.AttributeCondition;

/**
 * @see <a href=
 *      "https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/sac/AbstractAttributeCondition.java">github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/sac/AbstractAttributeCondition.java</a>
 */
abstract class AbstractAttributeCondition implements AttributeCondition, ExtendedCondition {

	/**
	 * The attribute's local name.
	 */
	protected final @Nullable String localName;

	/**
	 * The attribute's namespace URI.
	 */
	protected final @Nullable String namespaceURI;

	/** The attribute value */
	private final String value;

	/**
	 * Creates a new AbstractAttributeCondition object.
	 */
	AbstractAttributeCondition(final @Nullable String localName, final @Nullable String namespaceURI, final String value) {
		this.localName = localName;
		this.namespaceURI = namespaceURI;
		this.value = value;
	}

	@Override
	public @Nullable String getLocalName() {
		return localName;
	}

	@Override
	public @Nullable String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public String getValue() {
		return value;
	}
}
