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
package org.eclipse.tm4e.ui.samples;

import org.eclipse.tm4e.registry.ITMResource;

/**
 * Sample API.
 */
public interface ISample extends ITMResource {

	/**
	 * Returns the name of the sample.
	 *
	 * @return the name of the sample.
	 */
	String getName();

	/**
	 * Returns the scope name of the sample.
	 *
	 * @return the scope name of the sample.
	 */
	String getScopeName();

	/**
	 * Returns the content of the sample.
	 *
	 * @return the content of the sample.
	 */
	String getContent();
}
