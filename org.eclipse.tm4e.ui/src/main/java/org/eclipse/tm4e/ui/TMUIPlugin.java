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
 * Dietrich Travkin (SOLUNAR GmbH) - Additions for custom code templates
 */
package org.eclipse.tm4e.ui;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.text.templates.ContextTypeRegistry;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.utils.NullSafetyHelper;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.samples.SampleManager;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.internal.utils.CodeTemplateContextTypeUtils;
import org.eclipse.tm4e.ui.model.ITMModelManager;
import org.eclipse.tm4e.ui.samples.ISampleManager;
import org.eclipse.tm4e.ui.templates.CommentTemplateContextType;
import org.eclipse.tm4e.ui.templates.DefaultTMTemplateContextType;
import org.eclipse.tm4e.ui.templates.DocumentationCommentTemplateContextType;
import org.eclipse.tm4e.ui.templates.TMLanguageTemplateContextType;
import org.eclipse.tm4e.ui.themes.ColorManager;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TMUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.tm4e.ui"; //$NON-NLS-1$
	private static final String TRACE_ID = PLUGIN_ID + "/trace"; //$NON-NLS-1$

	// IDs for custom code templates
	private static final String CUSTOM_TEMPLATES_KEY = PLUGIN_ID + ".text.templates.custom"; //$NON-NLS-1$
	private static final String TEMPLATES_REGISTRY_ID = PLUGIN_ID + ".templates"; //$NON-NLS-1$

	// The shared instance
	private static volatile @Nullable TMUIPlugin plugin;

	// registry and store for custom code templates
	private @Nullable ContributionContextTypeRegistry contextTypeRegistry = null;
	private @Nullable TemplateStore templateStore = null;

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

		TMImages.initalize(getImageRegistry());
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		if (templateStore != null) {
			templateStore.stopListeningForPreferenceChanges();
		}
		ColorManager.getInstance().dispose();
		plugin = null;
		super.stop(context);
	}

	public ContextTypeRegistry getTemplateContextRegistry() {
		@NonNull
		ContributionContextTypeRegistry result;

		if (contextTypeRegistry == null) {
			result = new ContributionContextTypeRegistry(TEMPLATES_REGISTRY_ID);
			contextTypeRegistry = result;

			result.addContextType(DefaultTMTemplateContextType.CONTEXT_ID);
			result.addContextType(CommentTemplateContextType.CONTEXT_ID);
			result.addContextType(DocumentationCommentTemplateContextType.CONTEXT_ID);

			// Add language-specific context types
			// TODO Skip certain grammars? Some grammars have no name or are only used for highlighting code snippets, e.g. in Markdown
			final IGrammarDefinition[] grammarDefinitions = TMEclipseRegistryPlugin.getGrammarRegistryManager().getDefinitions();
			for (final IGrammarDefinition definition : grammarDefinitions) {
				final ITMScope languageScope = definition.getScope();
				final IGrammar languageGrammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarForScope(languageScope);
				if (languageGrammar != null) {
					final String contextTypeName = CodeTemplateContextTypeUtils.toContextTypeName(languageGrammar);
					final TMLanguageTemplateContextType languageContextType = new TMLanguageTemplateContextType(
							contextTypeName, languageScope);
					result.addContextType(languageContextType);
				}
			}

		} else {
			result = NullSafetyHelper.castNonNull(contextTypeRegistry);
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	private static class ContextTypeRegistryWrapper extends org.eclipse.jface.text.templates.ContextTypeRegistry {

		private final ContextTypeRegistry delegate;

		public ContextTypeRegistryWrapper(final ContextTypeRegistry registry) {
			this.delegate = registry;
		}

		// TODO How can this null-safety check be handled correctly?
		@SuppressWarnings("null")
		@Override
		public Iterator<@Nullable TemplateContextType> contextTypes() {
			return delegate.contextTypes();
		}

		@Override
		public void addContextType(final @Nullable TemplateContextType contextType) {
			delegate.addContextType(contextType);
		}

		@Override
		public @Nullable TemplateContextType getContextType(final @Nullable String id) {
			return delegate.getContextType(id);
		}

	}

	public static ContextTypeRegistryWrapper from(final ContextTypeRegistry registry) {
		return new ContextTypeRegistryWrapper(registry);
	}

	public TemplateStore getTemplateStore() {
		@NonNull
		TemplateStore result;

		if (templateStore == null) {
			result = new ContributionTemplateStore(from(getTemplateContextRegistry()), getPreferenceStore(),
					CUSTOM_TEMPLATES_KEY);
			templateStore = result;

			try {
				result.load();
			} catch (final IOException e) {
				Platform.getLog(this.getClass()).error(e.getMessage(), e);
			}

			result.startListeningForPreferenceChanges();
		} else {
			result = NullSafetyHelper.castNonNull(templateStore);
		}

		return result;
	}

}
