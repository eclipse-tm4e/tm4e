/**
 * Copyright (c) 2015-2022 Angelo ZERR.
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
 */
package org.eclipse.tm4e.ui.text;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.model.IModelTokensChangedListener;
import org.eclipse.tm4e.core.model.ITMModel;
import org.eclipse.tm4e.core.model.ModelTokensChangedEvent;
import org.eclipse.tm4e.core.model.Range;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.model.TMDocumentModel;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.ui.internal.text.TMPresentationReconcilerTestGenerator;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.internal.utils.ClassHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;
import org.eclipse.tm4e.ui.internal.utils.MarkerUtils;
import org.eclipse.tm4e.ui.internal.utils.PreferenceUtils;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ITokenProvider;
import org.eclipse.ui.IEditorPart;

/**
 * TextMate presentation reconciler which must be initialized with:
 *
 * <ol>
 * <li>a TextMate grammar {@link IGrammar} used to initialize the {@link TMDocumentModel}.</li>
 * <li>a token provider {@link ITokenProvider} to retrieve the {@link IToken} from a {@link TMToken} type .</li>
 * </ol>
 */
public class TMPresentationReconciler implements IPresentationReconciler {

	/** The default text attribute if none is returned as data by the current token. */
	private final Token defaultToken;

	/** The target viewer. */
	@Nullable
	private ITextViewer viewer;

	/** The internal listener. */
	private final InternalListener internalListener;

	@Nullable
	private IGrammar grammar;
	private boolean forcedGrammar;

	@Nullable
	private ITokenProvider tokenProvider;

	private final TextAttribute fDefaultTextAttribute;

	@Nullable
	private IPreferenceChangeListener themeChangeListener;

	private final List<ITMPresentationReconcilerListener> listeners = new ArrayList<>();

	private boolean initializeViewerColors;

	private boolean updateTextDecorations;

	/** true if the presentation reconciler is enabled (grammar and theme are available) and false otherwise. */
	private boolean enabled;

	/** true if a {@link TMException} should be thrown if grammar or theme cannot be found and false otherwise. */
	private boolean throwError;

	public TMPresentationReconciler() {
		this.defaultToken = new Token(null);
		this.internalListener = new InternalListener();
		this.fDefaultTextAttribute = new TextAttribute(null);
		if (PreferenceUtils.isDebugGenerateTest()) {
			addTMPresentationReconcilerListener(new TMPresentationReconcilerTestGenerator());
		}
		setThrowError(PreferenceUtils.isDebugThrowError());
	}

	/**
	 * Listener to recolorize editors when E4 Theme from General / Appearance
	 * preferences changed or TextMate theme changed..
	 */
	private final class ThemeChangeListener implements IPreferenceChangeListener {

		@Override
		public void preferenceChange(@Nullable final PreferenceChangeEvent event) {
			if (event == null)
				return;
			final IThemeManager themeManager = TMUIPlugin.getThemeManager();
			switch (event.getKey()) {
				case PreferenceConstants.E4_THEME_ID:
					preferenceThemeChange((String) event.getNewValue(), themeManager);
					break;
				case PreferenceConstants.THEME_ASSOCIATIONS:
					preferenceThemeChange(PreferenceUtils.getE4PreferenceCSSThemeId(), themeManager);
					break;
			}
		}

		void preferenceThemeChange(@Nullable final String eclipseThemeId, final IThemeManager themeManager) {
			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer == null) {
				return;
			}

			final IDocument document = viewer.getDocument();
			if (document == null) {
				return;
			}

			final var grammar = TMPresentationReconciler.this.grammar;
			if (grammar == null) {
				return;
			}
			// Select the well TextMate theme from the given E4 theme id.
			final boolean dark = themeManager.isDarkEclipseTheme(eclipseThemeId);
			final ITokenProvider newTheme = themeManager.getThemeForScope(grammar.getScopeName(), dark);
			setTheme(newTheme);
		}
	}

	/**
	 * Internal listener class.
	 */
	private final class InternalListener implements ITextInputListener, IModelTokensChangedListener, ITextListener {

		void fireInstall(final ITextViewer viewer, final IDocument document) {
			synchronized (listeners) {
				for (final ITMPresentationReconcilerListener listener : listeners) {
					listener.install(viewer, document);
				}
			}
		}

		void fireUninstall() {
			synchronized (listeners) {
				for (final ITMPresentationReconcilerListener listener : listeners) {
					listener.uninstall();
				}
			}
		}

		@Override
		public void inputDocumentAboutToBeChanged(@Nullable final IDocument oldDoc, @Nullable final IDocument newDoc) {
			if (oldDoc == null)
				return;

			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer != null) {
				viewer.removeTextListener(this);
			}
			TMModelManager.INSTANCE.disconnect(oldDoc);
			fireUninstall();
		}

		@Override
		public void inputDocumentChanged(@Nullable final IDocument oldDoc, @Nullable final IDocument newDoc) {
			if (newDoc == null) {
				return;
			}

			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer == null)
				return;

			fireInstall(viewer, newDoc);
			try {
				viewer.addTextListener(this);
				// Update the grammar
				final IGrammar localGrammar = findGrammar(newDoc);

				if (localGrammar != null) {
					TMPresentationReconciler.this.grammar = localGrammar;
				} else if (isThrowError()) {
					throw new TMException("Cannot find TextMate grammar for the given document");
				}

				// Update the theme
				if (localGrammar != null) {
					final String scopeName = localGrammar.getScopeName();
					if (tokenProvider == null) {
						tokenProvider = TMUIPlugin.getThemeManager().getThemeForScope(scopeName,
								viewer.getTextWidget().getBackground().getRGB());
					}
					if (tokenProvider != null) {
						applyThemeEditor();
					} else if (isThrowError()) {
						throw new TMException("Cannot find Theme for the given grammar '" + scopeName + "'");
					}
				}

				if (localGrammar != null) {
					final var enable = TMPresentationReconciler.this.enabled = tokenProvider != null;
					if (enable) {
						// Connect a TextModel to the new document.
						final var docModel = TMModelManager.INSTANCE.connect(newDoc);
						docModel.setGrammar(localGrammar);

						// Add model listener
						docModel.addModelTokensChangedListener(this);
					}
				} else {
					TMPresentationReconciler.this.enabled = false;
				}
			} catch (final CoreException e) {
				Platform.getLog(Platform.getBundle(TMEclipseRegistryPlugin.PLUGIN_ID)).log(
						new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID, "Error while initializing TextMate model.", e));
			}
		}

		/**
		 * Finds a grammar for the given document.
		 *
		 * @throws CoreException
		 */
		@Nullable
		IGrammar findGrammar(final IDocument newDoc) throws CoreException {
			final IGrammar localGrammar = forcedGrammar ? TMPresentationReconciler.this.grammar : null;
			if (localGrammar != null) {
				return localGrammar;
			}
			final ContentTypeInfo info = ContentTypeHelper.findContentTypes(newDoc);
			if (info == null) {
				return null;
			}
			return findGrammar(info);
		}

		@Override
		public void textChanged(final @Nullable TextEvent event) {
			if (event == null || !event.getViewerRedrawState()) {
				return;
			}

			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer == null)
				return;

			// changed text: propagate previous style, which will be overridden later asynchronously by TM
			if (event.getDocumentEvent() != null) {
				final int diff = event.getText().length() - event.getLength();
				if (diff == 0 || event.getOffset() <= 0) {
					return;
				}
				final StyleRange range = viewer.getTextWidget().getStyleRangeAtOffset(event.getOffset() - 1);
				if (range == null) {
					return;
				}
				range.length = Math.max(0, range.length + diff);
				viewer.getTextWidget().setStyleRange(range);
				return;
			}

			// TextViewer#invalidateTextPresentation is called (because of validation, folding, etc)
			// case 2), do the colorization.
			final IDocument doc = viewer.getDocument();
			if (doc == null) {
				return;
			}
			final IRegion region = computeRegionToRedraw(event, doc);
			if (enabled) {
				// case where there is grammar & theme -> update text presentation with the grammar tokens
				final var docModel = TMModelManager.INSTANCE.connect(doc);

				// It's possible that there are two or more SourceViewers opened for the same document,
				// so when one of them is closed the existing TMModel is also "closed" and its TokenizerThread
				// is interrupted and terminated.
				// In this case, in order to let the others Source Viewers to continue working a new
				// TMModel object is to be created for the document, so it should be initialized
				// with the existing grammar as well as new ModelTokenListener is to be added
				final var grammar = TMPresentationReconciler.this.grammar;
				if (grammar != null) {
					docModel.setGrammar(grammar);
					docModel.addModelTokensChangedListener(this);
				}

				try {
					TMPresentationReconciler.this.colorize(region, docModel);
				} catch (final BadLocationException ex) {
					TMUIPlugin.logError(ex);
				}
			} else {
				// case where there is no grammar & theme -> update text presentation with the
				// default styles (ex: to support highlighting with GenericEditor)
				final TextPresentation presentation = new TextPresentation(region, 100);
				presentation.setDefaultStyleRange(new StyleRange(region.getOffset(), region.getLength(), null, null));
				applyTextRegionCollection(presentation);
			}
		}

		IRegion computeRegionToRedraw(final TextEvent event, final IDocument doc) {
			final IRegion region = event.getOffset() == 0 && event.getLength() == 0 && event.getText() == null
					? new Region(0, doc.getLength()) // redraw state change, damage the whole document
					: widgetRegion2ModelRegion(event);
			if (region == null || region.getLength() == 0) {
				return new Region(0, 0);
			}
			return region;
		}

		/**
		 * Translates the given text event into the corresponding range of the viewer's document.
		 *
		 * @param event
		 *            the text event
		 *
		 * @return the widget region corresponding the region of the given event or <code>null</code> if none
		 *
		 * @since 2.1
		 */
		@Nullable
		IRegion widgetRegion2ModelRegion(final TextEvent event) {
			final var text = event.getText();
			final int length = text == null ? 0 : text.length();
			final var viewer = castNonNull(TMPresentationReconciler.this.viewer);
			if (viewer instanceof final ITextViewerExtension5 viewerExt5) {
				return viewerExt5.widgetRange2ModelRange(new Region(event.getOffset(), length));
			}
			return new Region(event.getOffset() + viewer.getVisibleRegion().getOffset(), length);
		}

		@Override
		public void modelTokensChanged(final ModelTokensChangedEvent event) {
			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer != null) {
				final Control control = viewer.getTextWidget();
				if (control != null) {
					control.getDisplay().asyncExec(() -> colorize(event));
				}
			}

			MarkerUtils.updateTextMarkers(event);
		}

		void colorize(final ModelTokensChangedEvent event) {
			final var viewer = TMPresentationReconciler.this.viewer;
			if (viewer == null)
				return;

			final IDocument document = viewer.getDocument();
			if (document == null) {
				return;
			}
			final ITMModel model = event.model;
			if (model instanceof final TMDocumentModel docModel) {
				for (final Range range : event.ranges) {
					try {
						final int length = document.getLineOffset(range.toLineNumber - 1)
								+ document.getLineLength(range.toLineNumber - 1)
								- document.getLineOffset(range.fromLineNumber - 1);
						final var region = new Region(document.getLineOffset(range.fromLineNumber - 1), length);
						TMPresentationReconciler.this.colorize(region, docModel);
					} catch (final BadLocationException ex) {
						TMUIPlugin.logError(ex);
					}
				}
			}
		}

		@Nullable
		IGrammar findGrammar(@Nullable final ContentTypeInfo info) {
			if (info == null) {
				return null;
			}
			final IContentType[] contentTypes = info.getContentTypes();
			// Discover the well grammar from the contentTypes
			IGrammar res = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(contentTypes);
			if (res == null) {
				// Discover the well grammar from the filetype
				final String fileName = info.getFileName();
				if (fileName.indexOf('.') > -1) {
					final String fileType = new Path(fileName).getFileExtension();
					res = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarForFileType(fileType);
				}
			}
			return res;
		}
	}

	public void setGrammar(@Nullable final IGrammar grammar) {
		var viewer = this.viewer;
		final boolean changed = viewer != null && (this.grammar == null || !Objects.equals(grammar, this.grammar));
		this.grammar = grammar;
		this.forcedGrammar = true;
		if (changed) {
			// Grammar has changed, recreate the TextMate model
			viewer = castNonNull(viewer);
			final IDocument document = viewer.getDocument();
			if (document == null) {
				return;
			}
			internalListener.inputDocumentAboutToBeChanged(viewer.getDocument(), null);
			internalListener.inputDocumentChanged(null, document);
		}
	}

	@Nullable
	public IGrammar getGrammar() {
		return grammar;
	}

	@Nullable
	public ITokenProvider getTokenProvider() {
		return tokenProvider;
	}

	public void setTheme(final ITokenProvider newTheme) {
		final ITokenProvider oldTheme = this.tokenProvider;
		if (!Objects.equals(oldTheme, newTheme) && grammar != null) {
			this.tokenProvider = newTheme;
			applyThemeEditor();
			final var viewer = this.viewer;
			if (viewer == null)
				return;
			final IDocument document = viewer.getDocument();
			final var docModel = TMModelManager.INSTANCE.connect(document);
			try {
				colorize(new Region(0, document.getLength()), docModel);
			} catch (final BadLocationException ex) {
				TMUIPlugin.logError(ex);
			}
		}
	}

	@Override
	public void install(@Nullable ITextViewer viewer) {
		viewer = this.viewer = castNonNull(viewer);
		viewer.addTextInputListener(internalListener);

		final IDocument document = viewer.getDocument();
		if (document != null) {
			internalListener.inputDocumentChanged(null, document);
		}
		final var themeChangeListener = this.themeChangeListener = new ThemeChangeListener();
		ThemeManager.getInstance().addPreferenceChangeListener(themeChangeListener);
	}

	@Override
	public void uninstall() {
		final var viewer = castNonNull(this.viewer);
		viewer.removeTextInputListener(internalListener);
		// Ensure we uninstall all listeners
		internalListener.inputDocumentAboutToBeChanged(viewer.getDocument(), null);
		final var themeChangeListener = this.themeChangeListener;
		if (themeChangeListener != null) {
			ThemeManager.getInstance().removePreferenceChangeListener(themeChangeListener);
		}
		this.themeChangeListener = null;
	}

	@Nullable
	@Override
	public IPresentationDamager getDamager(@Nullable final String contentType) {
		return null;
	}

	@Nullable
	@Override
	public IPresentationRepairer getRepairer(@Nullable final String contentType) {
		return null;
	}

	private void colorize(final IRegion damage, final TMDocumentModel model) throws BadLocationException {
		final IDocument doc = model.getDocument();
		final int fromLineIndex = doc.getLineOfOffset(damage.getOffset());
		final int toLineIndex = doc.getLineOfOffset(damage.getOffset() + damage.getLength());
		applyThemeEditorIfNeeded();
		// Refresh the UI Presentation
		if (TMUIPlugin.isLogTraceEnabled())
			TMUIPlugin.logTrace("Render from: " + fromLineIndex + " to: " + toLineIndex);
		final var presentation = new TextPresentation(damage, 1000);
		Exception error = null;
		try {
			int lastStart = presentation.getExtent().getOffset();
			int length = 0;
			boolean firstToken = true;
			IToken lastToken = Token.UNDEFINED;
			TextAttribute lastAttribute = getTokenTextAttribute(lastToken);

			List<TMToken> tokens = null;
			for (int lineIndex = fromLineIndex; lineIndex <= toLineIndex; lineIndex++) {
				tokens = model.getLineTokens(lineIndex);
				if (tokens == null) {
					// TextMate tokens was not computed for this line. This happens when the viewer is invalidated
					// (by validation for instance) and textChanged is called.
					// see https://github.com/eclipse/tm4e/issues/78
					if (TMUIPlugin.isLogTraceEnabled())
						TMUIPlugin.logTrace("TextMate tokens not available for line " + lineIndex);
					break;
				}
				final int startLineOffset = doc.getLineOffset(lineIndex);
				for (int i = 0; i < tokens.size(); i++) {
					final TMToken currentToken = tokens.get(i);
					final TMToken nextToken = (i + 1 < tokens.size()) ? tokens.get(i + 1) : null;
					int tokenStartIndex = currentToken.startIndex;

					if (isBeforeRegion(currentToken, startLineOffset, damage)) {
						// The token is before the damage region
						if (nextToken != null) {
							if (isBeforeRegion(nextToken, startLineOffset, damage)) {
								continue; // ignore it
							}
							tokenStartIndex = damage.getOffset() - startLineOffset;
						} else {
							tokenStartIndex = damage.getOffset() - startLineOffset;
							final IToken token = toToken(currentToken);
							lastAttribute = getTokenTextAttribute(token);
							length += getTokenLengh(tokenStartIndex, nextToken, lineIndex, doc);
							firstToken = false;
							// ignore it
							continue;
						}
					} else if (isAfterRegion(currentToken, startLineOffset, damage)) {
						// The token is after the damage region, stop the colorization process
						break;
					}

					final IToken token = toToken(currentToken);
					final TextAttribute attribute = getTokenTextAttribute(token);
					if (lastAttribute.equals(attribute)) {
						length += getTokenLengh(tokenStartIndex, nextToken, lineIndex, doc);
						firstToken = false;
					} else {
						if (!firstToken) {
							addRange(presentation, lastStart, length, lastAttribute);
						}
						firstToken = false;
						lastToken = token;
						lastAttribute = attribute;
						lastStart = tokenStartIndex + startLineOffset;
						length = getTokenLengh(tokenStartIndex, nextToken, lineIndex, doc);
					}
				}
			}
			// adjust the length
			length = Math.min(length, damage.getOffset() + damage.getLength() - lastStart);
			addRange(presentation, lastStart, length, lastAttribute);
			applyTextRegionCollection(presentation);
		} catch (final Exception ex) {
			error = ex;
			TMUIPlugin.logError(ex);
		} finally {
			fireColorize(presentation, error);
		}
	}

	/**
	 * @return true if the given token is before the given region and false otherwise.
	 */
	private boolean isBeforeRegion(final TMToken token, final int startLineOffset, final IRegion damage) {
		return token.startIndex + startLineOffset < damage.getOffset();
	}

	/**
	 * @return true if the given token is after the given region and false otherwise.
	 */
	private boolean isAfterRegion(final TMToken token, final int startLineOffset, final IRegion damage) {
		return token.startIndex + startLineOffset >= damage.getOffset() + damage.getLength();
	}

	private IToken toToken(final TMToken token) {
		final var tokenProvider = this.tokenProvider;
		if (tokenProvider != null) {
			final IToken result = tokenProvider.getToken(token.type);
			if (result != null) {
				return result;
			}
		}
		return defaultToken;
	}

	private int getTokenLengh(final int tokenStartIndex, @Nullable final TMToken nextToken, final int line,
			final IDocument document) throws BadLocationException {
		if (nextToken != null) {
			return nextToken.startIndex - tokenStartIndex;
		}
		return document.getLineLength(line) - tokenStartIndex;
	}

	/**
	 * Returns a text attribute encoded in the given token. If the token's data is not <code>null</code> and a text
	 * attribute it is assumed that it is the encoded text attribute. It returns the default text attribute if there
	 * is no encoded text attribute found.
	 *
	 * @param token
	 *            the token whose text attribute is to be determined
	 *
	 * @return the token's text attribute
	 */
	protected TextAttribute getTokenTextAttribute(final IToken token) {
		final Object data = token.getData();
		if (data instanceof final TextAttribute textAttribute) {
			return textAttribute;
		}
		return fDefaultTextAttribute;
	}

	/**
	 * Adds style information to the given text presentation.
	 *
	 * @param presentation
	 *            the text presentation to be extended
	 * @param offset
	 *            the offset of the range to be styled
	 * @param length
	 *            the length of the range to be styled
	 * @param attr
	 *            the attribute describing the style of the range to be styled
	 * @param lastLineStyleRanges
	 */
	protected void addRange(final TextPresentation presentation, final int offset, final int length,
			@Nullable final TextAttribute attr) {
		if (attr != null) {
			final int style = attr.getStyle();
			final int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
			final var styleRange = new StyleRange(offset, length, attr.getForeground(), attr.getBackground(),
					fontStyle);
			styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
			styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
			styleRange.font = attr.getFont();
			presentation.addStyleRange(styleRange);
		}
	}

	/**
	 * Applies the given text presentation to the text viewer the presentation reconciler is installed on.
	 *
	 * @param presentation
	 *            the text presentation to be applied to the text viewer
	 */
	private void applyTextRegionCollection(final TextPresentation presentation) {
		final var viewer = this.viewer;
		if (viewer != null) {
			viewer.changeTextPresentation(presentation, false);
		}
	}

	/**
	 * Add a TextMate presentation reconciler listener.
	 *
	 * @param listener
	 *            the TextMate presentation reconciler listener to add.
	 */
	public void addTMPresentationReconcilerListener(final ITMPresentationReconcilerListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	/**
	 * Remove a TextMate presentation reconciler listener.
	 *
	 * @param listener
	 *            the TextMate presentation reconciler listener to remove.
	 */
	public void removeTMPresentationReconcilerListener(final ITMPresentationReconcilerListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Fire colorize.
	 */
	private void fireColorize(final TextPresentation presentation, @Nullable final Throwable error) {
		synchronized (listeners) {
			for (final ITMPresentationReconcilerListener listener : listeners) {
				listener.colorize(presentation, error);
			}
		}
	}

	@Nullable
	public static TMPresentationReconciler getTMPresentationReconciler(@Nullable final IEditorPart editorPart) {
		if (editorPart == null) {
			return null;
		}
		@Nullable
		final ITextOperationTarget target = editorPart.getAdapter(ITextOperationTarget.class);
		if (target instanceof final ITextViewer textViewer) {
			return TMPresentationReconciler.getTMPresentationReconciler(textViewer);
		}
		return null;
	}

	/**
	 * Returns the {@link TMPresentationReconciler} of the given text viewer and null otherwise.
	 *
	 * @return the {@link TMPresentationReconciler} of the given text viewer and null otherwise.
	 */
	@Nullable
	public static TMPresentationReconciler getTMPresentationReconciler(final ITextViewer textViewer) {
		try {
			final Field field = SourceViewer.class.getDeclaredField("fPresentationReconciler");
			if (field != null) {
				field.trySetAccessible();
				final Object presentationReconciler = field.get(textViewer);
				// field is IPresentationRecounciler, looking for TMPresentationReconciler implementation
				return presentationReconciler instanceof final TMPresentationReconciler tmPresentationReconciler
						? tmPresentationReconciler
						: null;
			}
		} catch (SecurityException | NoSuchFieldException ex) {
			// if SourceViewer class no longer has fPresentationReconciler or changes access level
			TMUIPlugin.logError(ex);
		} catch (IllegalArgumentException | IllegalAccessException | NullPointerException
				| ExceptionInInitializerError iae) {
			// This should not be logged as an error. This is an expected possible outcome of field.get(textViewer).
			// The method assumes ITextViewer is actually ISourceViewer, and specifically the SourceViewer
			// implementation
			// that was available at the current build. This code also works with any implementation that follows the
			// internal structure if also an ITextViewer.
			// If these assumptions are false, the method should return null. Logging causes repeat noise.
		}
		return null;
	}

	/**
	 * Initialize foreground, background color, current line highlight from the current theme.
	 */
	private void applyThemeEditor() {
		this.initializeViewerColors = false;
		this.updateTextDecorations = false;
		applyThemeEditorIfNeeded();
	}

	/**
	 * Initialize foreground, background color, current line highlight from the current theme if needed.
	 */
	private void applyThemeEditorIfNeeded() {
		final var viewer = castNonNull(this.viewer);
		final var tokenProvider = castNonNull(this.tokenProvider);

		if (!initializeViewerColors) {
			final StyledText styledText = viewer.getTextWidget();
			((ITheme) tokenProvider).initializeViewerColors(styledText);
			initializeViewerColors = true;
		}
		if (updateTextDecorations) {
			return;
		}
		try {
			// Ugly code to update "current line highlight" :
			// - get the PaintManager from the ITextViewer with reflection.
			// - get the list of IPainter of PaintManager with reflection
			// - loop for IPainter to retrieve CursorLinePainter which manages "current line
			// highlight".
			final PaintManager paintManager = ClassHelper.getFieldValue(viewer, "fPaintManager", TextViewer.class);
			if (paintManager == null) {
				return;
			}
			final List<IPainter> painters = ClassHelper.getFieldValue(paintManager, "fPainters", PaintManager.class);
			if (painters == null) {
				return;
			}
			for (final IPainter painter : painters) {
				if (painter instanceof final CursorLinePainter cursorLinePainter) {
					// Update current line highlight
					final Color background = tokenProvider.getEditorCurrentLineHighlight();
					if (background != null) {
						cursorLinePainter.setHighlightColor(background);
					}
					updateTextDecorations = true;
				}
			}
		} catch (final Exception ex) {
			TMUIPlugin.logError(ex);
		}
	}

	/**
	 * Set true if a {@link TMException} should be thrown if grammar or theme cannot be found and false otherwise.
	 *
	 * @param throwError
	 */
	public void setThrowError(final boolean throwError) {
		this.throwError = throwError;
	}

	/**
	 * @return true if a {@link TMException} should be thrown if grammar or theme cannot be found and false otherwise.
	 */
	public boolean isThrowError() {
		return throwError;
	}

	/**
	 * @return true if the presentation reconciler is enabled (grammar and theme are available) and false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}
}
