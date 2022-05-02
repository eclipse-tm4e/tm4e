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
import org.w3c.css.sac.ElementSelector;

public abstract class AbstractElementSelector implements ElementSelector, ExtendedSelector {

	/**
	 * The namespace URI.
	 */
	@Nullable
	private final String namespaceURI;

	/**
	 * The local name.
	 */
	@Nullable
	private final String localName;

	/**
	 * Creates a new ElementSelector object.
	 */
	protected AbstractElementSelector(@Nullable final String uri, @Nullable final String name) {
		namespaceURI = uri;
		localName = name;
	}

	@Nullable
	@Override
	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Nullable
	@Override
	public String getLocalName() {
		return localName;
	}

}
