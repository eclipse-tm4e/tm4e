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
package org.eclipse.tm4e.ui;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.samples.SampleManager;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.model.ITMModelManager;
import org.eclipse.tm4e.ui.samples.ISampleManager;
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
	private static volatile @Nullable TMUIPlugin plugin;

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static @Nullable TMUIPlugin getDefault() {
		return plugin;
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

	public static void logTrace(final Exception ex) {
		if (isLogTraceEnabled()) {
			log(new Status(IStatus.INFO, PLUGIN_ID, ex.getMessage(), ex));
		}
	}

	public static void logTrace(final String message) {
		if (isLogTraceEnabled()) {
			log(new Status(IStatus.INFO, PLUGIN_ID, message));
		}
	}

	public static boolean isLogTraceEnabled() {
		return Boolean.parseBoolean(Platform.getDebugOption(TRACE_ID));
	}

	public static boolean getPreference(final String key, final boolean defaultValue) {
		return Platform.getPreferencesService().getBoolean(TMUIPlugin.PLUGIN_ID, key, defaultValue,
				null /* = search in all available scopes */);
	}

	public static @Nullable String getPreference(final String key, final @Nullable String defaultValue) {
		return Platform.getPreferencesService().getString(TMUIPlugin.PLUGIN_ID, key, defaultValue,
				null /* = search in all available scopes */);
	}

	public static ITMModelManager getTMModelManager() {
		return TMModelManager.INSTANCE;
	}

	public static IThemeManager getThemeManager() {
		return ThemeManager.getInstance();
	}

	public static ISampleManager getSampleManager() {
		return SampleManager.getInstance();
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		if (isLogTraceEnabled()) {
			// if the trace option is enabled publish all TM4E CORE JDK logging output to the Eclipse Error Log
			final var tm4eCorePluginId = "org.eclipse.tm4e.core";
			final var tm4eCoreLogger = Logger.getLogger(tm4eCorePluginId);
			tm4eCoreLogger.setLevel(Level.FINEST);
			tm4eCoreLogger.addHandler(new Handler() {
				@Override
				public void publish(final @Nullable LogRecord entry) {
					if (entry == null)
						return;

					final var params = entry.getParameters();
					final var msg = entry.getMessage();
					log(new Status(toSeverity(entry.getLevel()), tm4eCorePluginId,
							msg == null || params == null || params.length == 0
									? msg
									: java.text.MessageFormat.format(msg, entry.getParameters())));
				}

				private int toSeverity(final Level level) {
					if (level.intValue() >= Level.SEVERE.intValue()) {
						return IStatus.ERROR;
					}
					if (level.intValue() >= Level.WARNING.intValue()) {
						return IStatus.WARNING;
					}
					return IStatus.INFO;
				}

				@Override
				public void flush() {
					// nothing to do
				}

				@Override
				public void close() throws SecurityException {
					// nothing to do
				}
			});
		}
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		ColorManager.getInstance().dispose();
		plugin = null;
		super.stop(context);
	}
}
