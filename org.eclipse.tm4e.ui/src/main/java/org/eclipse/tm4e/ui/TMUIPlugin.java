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
package org.eclipse.tm4e.ui;

import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.snippets.SnippetManager;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.model.ITMModelManager;
import org.eclipse.tm4e.ui.snippets.ISnippetManager;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TMUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.tm4e.ui"; //$NON-NLS-1$

	// The shared instance
	private static TMUIPlugin plugin;

	/**
	 * The constructor
	 */
	public TMUIPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ColorManager.getInstance().dispose();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TMUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the TextMate model manager.
	 * 
	 * @return the TextMate model manager.
	 */
	public static ITMModelManager getTMModelManager() {
		return TMModelManager.getInstance();
	}

	/**
	 * Returns the TextMate themes manager.
	 * 
	 * @return the TextMate themes manager.
	 */
	public static IThemeManager getThemeManager() {
		return ThemeManager.getInstance();
	}

	/**
	 * Returns the Snippet manager.
	 * 
	 * @return the Snippet manager.
	 */
	public static ISnippetManager getSnippetManager() {
		return SnippetManager.getInstance();
	}

}
