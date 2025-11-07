/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * Copyright (c) 2021-2025 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Pierre-Yves B. - Issue #220 Switch to theme only works once for open editor
 * IBM Corporation Gerald Mitchell <gerald.mitchell@ibm.com> - bug fix
 * Sebastian Thomschke (Vegard IT GmbH) - code cleanup, bug fixes, performance improvements
 */
package org.eclipse.tm4e.ui.text;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.model.ModelTokensChangedEvent;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.ui.internal.text.TMPresentationReconcilerTestGenerator;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.internal.utils.GrammarUtils;
import org.eclipse.tm4e.ui.internal.utils.MarkerUtils;
import org.eclipse.tm4e.ui.internal.utils.PreferenceUtils;
import org.eclipse.tm4e.ui.model.ITMDocumentModel;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.ITokenProvider;
import org.eclipse.ui.IEditorPart;

/**
 * TextMate presentation reconciler which must be initialized with:
 *
 * <ol>
 * <li>a TextMate grammar {@link IGrammar} used to initialize the {@link ITMDocumentModel}</li>
 * <li>a token provider {@link ITokenProvider} that maps {@link TMToken}s to JFace's {@link IToken}s</li>
 * </ol>
 */
public class TMPresentationReconciler implements IPresentationReconciler {

	public static @Nullable TMPresentationReconciler getTMPresentationReconciler(final @Nullable IEditorPart editorPart) {
		if (editorPart == null)
			return null;

		return editorPart.getAdapter(ITextOperationTarget.class) instanceof final ITextViewer textViewer
				? TMPresentationReconciler.getTMPresentationReconciler(textViewer)
				: null;
	}

	/**
	 * @return the {@link TMPresentationReconciler} of the given text viewer and null otherwise.
	 */
	public static @Nullable TMPresentationReconciler getTMPresentationReconciler(final ITextViewer textViewer) {
		try {
			final Field presentationReconcilerField = SourceViewer.class.getDeclaredField("fPresentationReconciler");
			presentationReconcilerField.trySetAccessible();
			return presentationReconcilerField.get(textViewer) instanceof final TMPresentationReconciler tmPresentationReconciler
					? tmPresentationReconciler
					: null;
		} catch (SecurityException | NoSuchFieldException ex) {
			// in case the SourceViewer class no longer has the fPresentationReconciler field or changed access level
			TMUIPlugin.logError(ex);
		} catch (IllegalArgumentException | IllegalAccessException | NullPointerException | ExceptionInInitializerError ex) {
			// This should not be logged as an error. This is an expected possible outcome of field.get(textViewer).
			// The method assumes ITextViewer is actually ISourceViewer, and specifically the SourceViewer implementation
			// that was available at the current build. This code also works with any implementation that follows the
			// internal structure if also an ITextViewer.
			// If these assumptions are false, the method should return null. Logging just causes repeated noise.
		}
		return null;
	}

	private final ModelTokensChangedEvent.Listener modelsTokensChangedListener = (final ModelTokensChangedEvent event) -> {
		final var colorizer = TMPresentationReconciler.this.colorizer;
		if (colorizer != null) {
			final Control control = colorizer.getTextViewer().getTextWidget();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().asyncExec(() -> colorizer.colorize(event));
			}
		}
		MarkerUtils.updateTextMarkers(event);
	};

	/**
	 * Listener to recolorize editors when E4 Theme from "General > Appearance" preferences changed or TextMate theme changed.
	 */
	private final IPreferenceChangeListener themeChangeListener = (final PreferenceChangeEvent event) -> {
		switch (event.getKey()) {
			case PreferenceUtils.E4_THEME_ID, //
					PreferenceConstants.THEME_ASSOCIATIONS, //
					PreferenceConstants.DEFAULT_DARK_THEME, //
					PreferenceConstants.DEFAULT_LIGHT_THEME:
				final IDocument doc = getViewerDocument();
				if (doc == null)
					return;

				final var grammar = this.grammar;
				if (grammar == null)
					return;

				// select the best TextMate theme from the current Eclipse UI theme
				final ITheme newTheme = TMUIPlugin.getThemeManager().getThemeForScope(grammar.getScopeName());
				setTheme(newTheme);
				break;
		}
	};

	/** The target viewer */
	private @Nullable ITextViewer viewer;
	private final TextViewerListener viewerListener = new TextViewerListener();

	private volatile @Nullable Colorizer colorizer;
	private @Nullable IGrammar grammar;
	private boolean isForcedGrammar;
	private @Nullable ITokenProvider theme;

	private final Set<ITMPresentationReconcilerListener> listeners = new CopyOnWriteArraySet<>();

	public TMPresentationReconciler() {
		if (PreferenceUtils.isDebugGenerateTest()) {
			addListener(new TMPresentationReconcilerTestGenerator());
		}
	}

	private final class TextViewerListener implements ITextInputListener, ITextListener {
		private static final Region EMPTY_REGION = new Region(0, 0);

		@Override
		public void inputDocumentAboutToBeChanged(final @Nullable IDocument oldDoc, final @Nullable IDocument newDoc) {
			if (oldDoc == null)
				return;

			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer != null) {
				viewer.removeTextListener(viewerListener);
			}
			TMModelManager.INSTANCE.disconnect(oldDoc);
			listeners.forEach(ITMPresentationReconcilerListener::onUninstalled);
		}

		@Override
		public void inputDocumentChanged(final @Nullable IDocument oldDoc, final @Nullable IDocument newDoc) {
			if (newDoc == null)
				return;

			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer == null)
				return;

			listeners.forEach(l -> l.onInstalled(viewer, newDoc));

			viewer.addTextListener(viewerListener);

			// update the grammar
			IGrammar newDocGrammar;
			if (isForcedGrammar) {
				newDocGrammar = grammar;
			} else {
				newDocGrammar = findGrammar(newDoc);
				if (newDocGrammar == null) {
					newDocGrammar = grammar;
				} else {
					grammar = newDocGrammar;
				}
			}

			if (newDocGrammar == null) {
				colorizer = null;
				grammar = null;
				if (PreferenceUtils.isDebugThrowError())
					throw new TMException("Cannot find TextMate grammar for the given document!");
				return;
			}

			// update the theme
			final String scopeName = newDocGrammar.getScopeName();
			var theme = TMPresentationReconciler.this.theme;
			if (theme == null) {
				theme = TMPresentationReconciler.this.theme = TMUIPlugin.getThemeManager().getThemeForScope(scopeName,
						viewer.getTextWidget().getBackground().getRGB());
			}

			final var colorizer = TMPresentationReconciler.this.colorizer = new Colorizer(viewer, theme, listeners);
			final var isModelReused = TMModelManager.INSTANCE.isConnected(newDoc);

			// connect a TextMate model to the new document
			final var docModel = TMModelManager.INSTANCE.connect(newDoc);
			docModel.setGrammar(newDocGrammar);
			docModel.addModelTokensChangedListener(modelsTokensChangedListener);

			// For new models the colorizer will be invoked after tokenization. For reused
			// models (e.g. when splitting an editor) this must be done explicitly.
			if (isModelReused) {
				try {
					colorizer.colorize(new Region(0, newDoc.getLength()), docModel);
				} catch (final BadLocationException ex) {
					TMUIPlugin.logError(ex);
				}
			}
		}

		@Override
		public void textChanged(final TextEvent event) {
			if (!event.getViewerRedrawState())
				return;

			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer == null)
				return;

			// case 1) changed text: propagate previous style (which will be overridden later asynchronously by TMModel.TokenizerThread)
			if (event.getDocumentEvent() != null) {
				final int diff = event.getText().length() - event.getLength();
				if (diff == 0 || event.getOffset() <= 0)
					return;

				final StyledText widget = viewer.getTextWidget();
				if (widget.isDisposed())
					return;
				final StyleRange range = widget.getStyleRangeAtOffset(event.getOffset() - 1);
				if (range == null)
					return;

				range.length = Math.max(0, range.length + diff);
				widget.setStyleRange(range);
				return;
			}

			// case 2) TextViewer#invalidateTextPresentation is called (because of validation, folding, AnnotationPainter, etc.)
			final IDocument doc = viewer.getDocument();
			if (doc == null)
				return;

			final IRegion region = computeRegionToRedraw(event, doc);
			if (region.getLength() == 0)
				return;

			final var colorizer = TMPresentationReconciler.this.colorizer;
			if (colorizer != null) {
				// case where there is grammar & theme -> update text presentation with the grammar tokens

				// It's possible that there are two or more SourceViewers opened for the same document,
				// so when one of them is closed the existing TMModel is also "closed" and its TokenizerThread
				// is interrupted and terminated.
				// In this case, in order to let the others Source Viewers to continue working a new
				// TMModel object is to be created for the document, so it should be initialized
				// with the existing grammar as well as new ModelTokensChangedListener is to be added.
				final var docModel = TMModelManager.INSTANCE.connect(doc);
				docModel.setGrammar(castNonNull(grammar));
				docModel.addModelTokensChangedListener(modelsTokensChangedListener);

				try {
					colorizer.colorize(region, docModel);
				} catch (final BadLocationException ex) {
					TMUIPlugin.logError(ex);
				}
			} else {
				// case where there is no grammar & theme -> update text presentation with the default styles
				// (i.e. to support highlighting with GenericEditor)
				final var presentation = new TextPresentation(region, 100);
				presentation.setDefaultStyleRange(new StyleRange(region.getOffset(), region.getLength(), null, null));
				viewer.changeTextPresentation(presentation, false);
			}
		}

		IRegion computeRegionToRedraw(final TextEvent event, final IDocument doc) {
			// Fast path: check for redraw state change first
			if (event.getOffset() == 0 && event.getLength() == 0 && event.getText() == null) {
				// redraw state change, damage the whole document
				final int docLength = doc.getLength();
				return docLength > 0 ? new Region(0, docLength) : EMPTY_REGION;
			}

			// Normal text event processing
			final IRegion region = getRegionOfTextEvent(event);
			return region == null || region.getLength() == 0
					? EMPTY_REGION
					: region;
		}

		/**
		 * Translates the given text event into the corresponding range of the viewer's document.
		 */
		@Nullable
		IRegion getRegionOfTextEvent(final TextEvent event) {
			final var text = event.getText();
			final int length = text == null ? 0 : text.length();
			final var viewer = castNonNull(TMPresentationReconciler.this.viewer);
			return viewer instanceof final ITextViewerExtension5 viewerExt5
					? viewerExt5.widgetRange2ModelRange(new Region(event.getOffset(), length))
					: new Region(event.getOffset() + viewer.getVisibleRegion().getOffset(), length);
		}

		/**
		 * Finds a grammar for the given document.
		 */
		private @Nullable IGrammar findGrammar(final IDocument doc) {
			final IGrammar currentGrammar = isForcedGrammar ? grammar : null;
			if (currentGrammar != null)
				return currentGrammar;

			return GrammarUtils.findGrammar(doc);
		}
	}

	public @Nullable IGrammar getGrammar() {
		return grammar;
	}

	public void setGrammar(final @Nullable IGrammar newGrammar) {
		if (newGrammar == null && grammar == null)
			return;

		isForcedGrammar = newGrammar != null;
		if (Objects.equals(newGrammar, grammar))
			return;

		if (newGrammar == null) {
			colorizer = null;
		}

		grammar = newGrammar;

		final IDocument doc = getViewerDocument();
		if (doc == null)
			return;

		// since Grammar has changed, recreate the TextMate model
		viewerListener.inputDocumentAboutToBeChanged(doc, null);
		viewerListener.inputDocumentChanged(null, doc);
	}

	public @Nullable ITokenProvider getTokenProvider() {
		return theme;
	}

	public void setTheme(final ITokenProvider newTheme) {
		if (!Objects.equals(theme, newTheme)) {
			theme = newTheme;

			final var viewer = this.viewer;
			if (grammar == null || viewer == null)
				return;

			final var colorizer = TMPresentationReconciler.this.colorizer = new Colorizer(viewer, newTheme, listeners);

			final IDocument doc = getViewerDocument();
			if (doc == null)
				return;

			final var docModel = TMModelManager.INSTANCE.connect(doc);
			try {
				colorizer.colorize(new Region(0, doc.getLength()), docModel);
			} catch (final BadLocationException ex) {
				TMUIPlugin.logError(ex);
			}
		}
	}

	@Override
	public void install(final ITextViewer viewer) {
		this.viewer = viewer;
		viewer.addTextInputListener(viewerListener);

		final IDocument doc = viewer.getDocument();
		if (doc != null) {
			viewerListener.inputDocumentChanged(null, doc);
		}
		ThemeManager.addPreferenceChangeListener(themeChangeListener);
	}

	@Override
	public void uninstall() {
		final var viewer = this.viewer;
		if (viewer != null) {
			viewer.removeTextInputListener(viewerListener);

			viewerListener.inputDocumentAboutToBeChanged(viewer.getDocument(), null);
			ThemeManager.removePreferenceChangeListener(themeChangeListener);
			this.viewer = null;
		}
	}

	@Override
	public @Nullable IPresentationDamager getDamager(final @Nullable String contentType) {
		return null;
	}

	@Override
	public @Nullable IPresentationRepairer getRepairer(final @Nullable String contentType) {
		return null;
	}

	private @Nullable IDocument getViewerDocument() {
		final var viewer = this.viewer;
		if (viewer == null)
			return null;

		return viewer.getDocument();
	}

	public boolean addListener(final ITMPresentationReconcilerListener listener) {
		return listeners.add(listener);
	}

	public boolean removeListener(final ITMPresentationReconcilerListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * @return true if the presentation reconciler is enabled (grammar and theme are available) and false otherwise.
	 */
	public boolean isEnabled() {
		return colorizer != null;
	}
}
