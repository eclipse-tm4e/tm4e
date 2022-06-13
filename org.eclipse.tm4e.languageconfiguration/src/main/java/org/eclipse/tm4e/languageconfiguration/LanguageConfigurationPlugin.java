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
package org.eclipse.tm4e.languageconfiguration;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * OSGi Activator for Language Configuration (VSCode language-configuration.json) Eclipse bundle.
 */
public final class LanguageConfigurationPlugin extends AbstractUIPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.tm4e.languageconfiguration"; //$NON-NLS-1$

	/** The shared instance */
	@Nullable
	private static volatile LanguageConfigurationPlugin plugin;

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	@Nullable
	public static LanguageConfigurationPlugin getDefault() {
		return plugin;
	}

	public static void log(final IStatus status) {
		final var plugin = LanguageConfigurationPlugin.plugin;
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}

	@Override
	public void start(@Nullable final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(@Nullable final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
}
