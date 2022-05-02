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
package org.eclipse.tm4e.core.internal.theme.css;

import org.w3c.css.sac.ElementSelector;

public abstract class AbstractElementSelector implements ElementSelector, ExtendedSelector {

	/**
	 * The namespace URI.
	 */
	private final String namespaceURI;

	/**
	 * The local name.
	 */
	private final String localName;

	/**
	 * Creates a new ElementSelector object.
	 */
	protected AbstractElementSelector(final String uri, final String name) {
		namespaceURI = uri;
		localName = name;
	}

	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

}
