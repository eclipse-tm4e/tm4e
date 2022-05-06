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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.annotation.Nullable;

/**
 * TextMate Resource.
 */
public class TMResource implements ITMResource {

	private static final String PLATFORM_PLUGIN = "platform:/plugin/"; //$NON-NLS-1$

	@Nullable
	private String path;

	@Nullable
	private String pluginId;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public TMResource() {

	}

	/**
	 * Constructor for extension point.
	 *
	 * @param path
	 */
	public TMResource(final String path) {
		this.path = path;
	}

	public TMResource(final IConfigurationElement ce) {
		this(ce.getAttribute(XMLConstants.PATH_ATTR));
		this.pluginId = ce.getNamespaceIdentifier();
	}

	public TMResource(@Nullable final String path, @Nullable final String pluginId) {
		this.path = path;
		this.pluginId = pluginId;
	}

	@Nullable
	@Override
	public String getPath() {
		return path;
	}

	@Nullable
	@Override
	public String getPluginId() {
		return pluginId;
	}

	@Nullable
	@Override
	public InputStream getInputStream() throws IOException {
		if (path == null || "".equals(path)) {
			return null;
		}
		if (pluginId != null) {
			final URL url = new URL(PLATFORM_PLUGIN + pluginId + "/" + path);
			return url.openStream();
		}
		return new FileInputStream(new File(path));
	}

	@Nullable
	protected String getResourceContent() {
		try (InputStream in = this.getInputStream()) {
			if (in == null) {
				return null;
			}
			return convertStreamToString(in);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String convertStreamToString(final InputStream is) {
		try (Scanner s = new Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}
}
