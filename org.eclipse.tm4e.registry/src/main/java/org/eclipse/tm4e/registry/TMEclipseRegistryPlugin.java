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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.internal.GrammarRegistryManager;
import org.osgi.framework.BundleContext;

/**
 * OSGi Activator for TextMate Eclipse registry bundle.
 */
public class TMEclipseRegistryPlugin extends Plugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.tm4e.registry";

	/** The shared instance */
	private static volatile @Nullable TMEclipseRegistryPlugin plugin;

	/** @return the shared plugin instance */
	public static @Nullable TMEclipseRegistryPlugin getDefault() {
		return plugin;
	}

	/** @return the TextMate grammar manager */
	public static IGrammarRegistryManager getGrammarRegistryManager() {
		return GrammarRegistryManager.getInstance();
	}

	public static void log(final IStatus status) {
		final var p = plugin;
		if (p != null) {
			p.getLog().log(status);
		} else {
			System.out.println(status);
		}
	}

	public static void logError(final Exception ex) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, ex.getMessage(), ex));
	}

	public static void logError(final String message, @Nullable final Exception ex) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, message, ex));
	}

	@Override
	public void start(@Nullable final BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
	}

	@Override
	public void stop(@Nullable final BundleContext bundleContext) throws Exception {
		plugin = null;
		super.stop(bundleContext);
	}
}
