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

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm4e.registry.internal.GrammarRegistryManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi Activator for TextMate Eclipse registry bundle.
 *
 */
public class TMEclipseRegistryPlugin implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.tm4e.registry";

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		TMEclipseRegistryPlugin.context = bundleContext;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		TMEclipseRegistryPlugin.context = null;
	}

	/**
	 * Returns the TextMate grammar manager.
	 * 
	 * @return the TextMate grammar manager.
	 */
	public static IGrammarRegistryManager getGrammarRegistryManager() {
		return GrammarRegistryManager.getInstance();
	}

	/**
	 * Returns true if the debug option is enabled and false otherwise.
	 * 
	 * @param option
	 *            the option name
	 * @return true if the debug option is enabled and false otherwise.
	 */
	public static boolean isDebugOptionEnabled(String option) {
		String enabled = Platform.getDebugOption(option);
		return enabled != null && new Boolean(enabled).booleanValue();
	}
}