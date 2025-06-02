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
package org.eclipse.tm4e.registry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * TextMate resource definition API.
 *
 */
public interface ITMResource extends ITMDefinition {

	/**
	 * Returns the TextMate resource path.
	 *
	 * @return the TextMate resource path.
	 */
	String getPath();

	URI getURI();

	/**
	 * Returns the stream of the TextMate resource.
	 *
	 * @return the stream of the TextMate resource.
	 */
	InputStream getInputStream() throws IOException;

	long getLastModified();
}
