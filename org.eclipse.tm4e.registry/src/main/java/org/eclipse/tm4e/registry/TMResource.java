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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.annotation.Nullable;

/**
 * TextMate Resource.
 */
public abstract class TMResource implements ITMResource {

	private static final String PLATFORM_PLUGIN = "platform:/plugin/"; //$NON-NLS-1$

	private String path;
	private @Nullable String pluginId;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	protected TMResource() {
		path = "<set-by-gson>";
	}

	protected TMResource(final IConfigurationElement ce) {
		this(ce.getAttribute(XMLConstants.PATH_ATTR));
		this.pluginId = ce.getNamespaceIdentifier();
	}

	protected TMResource(final String path) {
		this.path = path;
	}

	protected TMResource(final String path, @Nullable final String pluginId) {
		this.path = path;
		this.pluginId = pluginId;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public @Nullable String getPluginId() {
		return pluginId;
	}

	@Override
	@SuppressWarnings("resource")
	public InputStream getInputStream() throws IOException {
		return new BufferedInputStream(pluginId != null
				? new URL(PLATFORM_PLUGIN + pluginId + "/" + path).openStream()
				: new FileInputStream(new File(path)));
	}

	protected @Nullable String getResourceContent() {
		try (InputStream in = this.getInputStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (final Exception ex) {
			TMEclipseRegistryPlugin.logError(ex);
		}
		return null;
	}
}
