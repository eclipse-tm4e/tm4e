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
package org.eclipse.tm4e.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * TextMate Resource.
 */
public class TMResource implements ITMResource {

	private static final String PLATFORM_PLUGIN = "platform:/plugin/"; //$NON-NLS-1$

	private String path;
	private String pluginId;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public TMResource() {

	}

	/**
	 * Constructor for extension point.
	 *
	 * @param element
	 */
	public TMResource(String path) {
		this.path = path;
	}

	public TMResource(IConfigurationElement ce) {
		this(ce.getAttribute(XMLConstants.PATH_ATTR));
		this.pluginId = ce.getNamespaceIdentifier();
	}

	public TMResource(String path, String pluginId) {
		this.path = path;
		this.pluginId = pluginId;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getPluginId() {
		return pluginId;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (path == null || path.length() < 0) {
			return null;
		}
		if (pluginId != null) {
			URL url = new URL(new StringBuilder(PLATFORM_PLUGIN).append(pluginId).append("/").append(path).toString());
			return url.openStream();
		}
		return new FileInputStream(new File(path));
	}

	protected String getResourceContent() {
		try {
			InputStream in = this.getInputStream();
			if (in == null) {
				return null;
			}
			return convertStreamToString(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String convertStreamToString(InputStream is) {
		Scanner s = null;
		try {
			s = new Scanner(is);
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}
}
