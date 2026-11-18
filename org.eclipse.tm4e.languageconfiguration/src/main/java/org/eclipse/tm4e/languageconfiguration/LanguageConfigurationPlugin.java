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

import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.tm4e.ui.internal.utils.FileLanguageSelection;
import org.eclipse.tm4e.ui.text.TMPartitions;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IEvaluationService;
import org.osgi.framework.BundleContext;

/**
 * OSGi Activator for Language Configuration (VSCode language-configuration.json) Eclipse bundle.
 * Refreshes language-dependent command enablement when a file's language changes.
 */
public final class LanguageConfigurationPlugin extends AbstractUIPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.tm4e.languageconfiguration"; //$NON-NLS-1$

	/** The shared instance */
	private static volatile @Nullable LanguageConfigurationPlugin plugin;

	private final Consumer<IDocument> languageChangeListener = document -> {
		if (PlatformUI.isWorkbenchRunning()) {
			final var evaluationService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
			if (evaluationService != null) {
				// Command enablement is cached separately from the editor's language configuration.
				evaluationService.requestEvaluation(PLUGIN_ID + ".hasLanguageConfiguration");
			}
		}
	};

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static @Nullable LanguageConfigurationPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the word pattern of the language at the given offset of a document. The word pattern is the
	 * {@code wordPattern} of the language-configuration.json file, converted to a Java pattern. Editor features use it to
	 * find the word at a position, as VS Code does.
	 * <p>
	 * The language at the offset can be a language that is embedded in the language of the document, for example
	 * JavaScript in HTML. Otherwise the language is selected in the same way as for the other language configuration
	 * features: a language that the user selected for the file comes first, then the workspace grammar bindings.
	 *
	 * @param document the document
	 * @param offset the offset in the document
	 *
	 * @return the word pattern, or {@code null} if the language has no language configuration, the configuration has no
	 *         word pattern, or Java cannot compile the word pattern. The caller can then use a default definition of a
	 *         word.
	 */
	public static @Nullable Pattern getWordPattern(final IDocument document, final int offset) {
		final ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
		if (info == null) {
			return null;
		}
		final var partitionContentTypes = TMPartitions.getContentTypesForOffset(document, offset, info.getContentTypes());
		if (partitionContentTypes.length == 0) {
			return getWordPattern(info);
		}
		final var registry = LanguageConfigurationRegistryManager.getInstance();
		for (final IContentType contentType : partitionContentTypes) {
			final var wordPattern = registry.getWordPattern(contentType);
			if (wordPattern != null) {
				return wordPattern;
			}
		}
		return null;
	}

	/**
	 * Returns the word pattern of the language of the given document. See {@link #getWordPattern(IDocument, int)}. This
	 * method does not look for embedded languages.
	 *
	 * @param document the document
	 *
	 * @return the word pattern, or {@code null} if the language has no language configuration, the configuration has no
	 *         word pattern, or Java cannot compile the word pattern
	 */
	public static @Nullable Pattern getWordPattern(final IDocument document) {
		final ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
		return info == null ? null : getWordPattern(info);
	}

	/**
	 * Returns the word pattern of the language of the given content types. See {@link #getWordPattern(IDocument, int)}.
	 * <p>
	 * The workspace grammar bindings select the language from the content types.
	 *
	 * @param contentTypes ordered content type candidates
	 *
	 * @return the word pattern, or {@code null} if the language has no language configuration, the configuration has no
	 *         word pattern, or Java cannot compile the word pattern
	 */
	public static @Nullable Pattern getWordPattern(final IContentType... contentTypes) {
		return LanguageConfigurationRegistryManager.getInstance().getWordPatternFor(contentTypes);
	}

	private static @Nullable Pattern getWordPattern(final ContentTypeInfo info) {
		final var registry = LanguageConfigurationRegistryManager.getInstance();
		// Reapplying a parent type's workspace binding here would undo a file's explicit language choice.
		return info.getExplicitGrammar() == null
				? registry.getWordPatternFor(info.getContentTypes())
				: registry.getWordPatternForResolvedTypes(info.getContentTypes());
	}

	public static void log(final IStatus status) {
		final var p = plugin;
		if (p != null) {
			p.getLog().log(status);
		} else {
			System.out.println(status);
		}
	}

	public static void logError(final Throwable ex) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, null, ex));
	}

	public static void logError(final String message, final @Nullable Throwable ex) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, message, ex));
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		FileLanguageSelection.addChangeListener(languageChangeListener);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		FileLanguageSelection.removeChangeListener(languageChangeListener);
		plugin = null;
		super.stop(context);
	}
}
