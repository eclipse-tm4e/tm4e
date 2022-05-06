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
package org.eclipse.tm4e.languageconfiguration.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * OSGi Activator for Language Configuration (VSCode
 * language-configuration.json) Eclipse bundle.
 *
 */
public final class LanguageConfigurationPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.tm4e.languageconfiguration"; //$NON-NLS-1$

	@Nullable
	private static LanguageConfigurationPlugin INSTANCE = null;

	@Nullable
	static LanguageConfigurationPlugin getInstance() {
		return INSTANCE;
	}

	public static void log(IStatus status) {
		final var plugin = LanguageConfigurationPlugin.INSTANCE;
		if (plugin != null) {
			plugin.getLog().log(status);
		}
	}

	@Override
	public void start(@Nullable final BundleContext context) throws Exception {
		super.start(context);
		INSTANCE = this;
	}

	@Override
	public void stop(@Nullable final BundleContext context) throws Exception {
		INSTANCE = null;
		super.stop(context);
	}
}