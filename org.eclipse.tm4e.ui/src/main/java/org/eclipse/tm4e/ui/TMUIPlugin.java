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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
	private static final String TRACE_ID = PLUGIN_ID + "/trace"; //$NON-NLS-1$

	// The shared instance
	private static TMUIPlugin plugin;

	/**
	 * The constructor
	 */
	public TMUIPlugin() {
	}

	public void trace(String message) {
		if (Boolean.parseBoolean(Platform.getDebugOption(TRACE_ID))) {
			getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
		}
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		boolean isDebugOn = Boolean.parseBoolean(Platform.getDebugOption(TRACE_ID));
		if (isDebugOn) {
			Logger tm4eCoreLogger = Logger.getLogger("org.eclipse.tm4e");
			tm4eCoreLogger.setLevel(Level.FINEST);
			tm4eCoreLogger.addHandler(new Handler() {

				@Override public void publish(LogRecord record) {
					TMUIPlugin.getDefault().getLog().log(new Status(
						toSeverity(record.getLevel()),
						"org.eclipse.tm4e.core",
						record.getMessage()
					));
				}

				private int toSeverity(Level level) {
					if (level.intValue() >= Level.SEVERE.intValue()) {
						return IStatus.ERROR;
					}
					if (level.intValue() >= Level.WARNING.intValue()) {
						return IStatus.WARNING;
					}
					return IStatus.INFO;
				}

				@Override public void flush() {
					// nothing to do
				}

				@Override public void close() throws SecurityException {
					// nothing to do
				}
			});
		}
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
