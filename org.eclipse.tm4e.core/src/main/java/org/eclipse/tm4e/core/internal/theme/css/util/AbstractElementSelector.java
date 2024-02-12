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
package org.eclipse.tm4e.core.internal.theme.css.util;

import org.eclipse.jdt.annotation.Nullable;
import org.w3c.css.sac.ElementSelector;

/**
 * @see <a href=
 *      "https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/sac/AbstractElementSelector.java">github.com/eclipse-platform/eclipse.platform.ui/blob/master/bundles/org.eclipse.e4.ui.css.core/src/org/eclipse/e4/ui/css/core/impl/sac/AbstractElementSelector.java</a>
 */
public abstract class AbstractElementSelector implements ElementSelector {

	/** The namespace URI */
	private final @Nullable String namespaceURI;

	/** The local name */
	private final @Nullable String localName;

	/**
	 * Creates a new ElementSelector object.
	 */
	protected AbstractElementSelector(final @Nullable String uri, final @Nullable String name) {
		namespaceURI = uri;
		localName = name;
	}

	@Override
	public @Nullable String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public @Nullable String getLocalName() {
		return localName;
	}
}
