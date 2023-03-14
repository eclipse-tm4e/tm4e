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
package org.eclipse.tm4e.ui.snippets;

import org.eclipse.tm4e.registry.ITMResource;

/**
 *
 * Snippet API.
 *
 */
public interface ISnippet extends ITMResource {

	/**
	 * Returns the name of the snippet.
	 *
	 * @return the name of the snippet.
	 */
	String getName();

	/**
	 * Returns the scope name of the snippet.
	 *
	 * @return the scope name of the snippet.
	 */
	String getScopeName();

	/**
	 * Returns the content of the snippet.
	 *
	 * @return the content of the snippet.
	 */
	String getContent();
}
