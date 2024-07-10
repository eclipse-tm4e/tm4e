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
package org.eclipse.tm4e.ui.internal.snippets;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tm4e.registry.TMResource;
import org.eclipse.tm4e.registry.XMLConstants;
import org.eclipse.tm4e.ui.snippets.ISnippet;

final class Snippet extends TMResource implements ISnippet {

	private final String scopeName;
	private final String name;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	Snippet() {
		scopeName = "<set-by-gson>";
		name = "<set-by-gson>";
	}

	Snippet(final IConfigurationElement ce) {
		super(ce);
		this.scopeName = castNonNull(ce.getAttribute(XMLConstants.SCOPE_NAME_ATTR));
		this.name = castNonNull(ce.getAttribute(XMLConstants.NAME_ATTR));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getContent() {
		final var content = getResourceContent();
		return content == null ? "" : content;
	}

	@Override
	public String getScopeName() {
		return scopeName;
	}
}
