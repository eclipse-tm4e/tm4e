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
package org.eclipse.tm4e.ui.text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.DocumentEvent;
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
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm4e.core.TMException;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.logger.ILogger;
import org.eclipse.tm4e.core.model.IModelTokensChangedListener;
import org.eclipse.tm4e.core.model.ITMModel;
import org.eclipse.tm4e.core.model.ModelTokensChangedEvent;
import org.eclipse.tm4e.core.model.Range;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.registry.EclipseSystemLogger;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.model.DocumentHelper;
import org.eclipse.tm4e.ui.internal.model.TMDocumentModel;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.ui.internal.text.TMPresentationReconcilerTestGenerator;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.internal.wizards.TextMateGrammarImportWizard;
import org.eclipse.tm4e.ui.model.ITMModelManager;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.ITokenProvider;
import org.eclipse.tm4e.ui.utils.ClassHelper;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.utils.ContentTypeInfo;
import org.eclipse.tm4e.ui.utils.PreferenceUtils;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * TextMate presentation reconciler which must be initialized with:
 * 
 * <ul>
 * <li>a TextMate grammar {@link IGrammar} used to initialize the TextMate model
 * {@link ITMDocumentModel}.</li>
 * <li>a token provider {@link ITokenProvider} to retrieve the {@link IToken}
 * from a TextMate token type .</li>
 * </ul>
 * 
 */
public class TMPresentationReconciler implements IPresentationReconciler {

	private static boolean GENERATE_TEST = TMEclipseRegistryPlugin
			.isDebugOptionEnabled("org.eclipse.tm4e.ui/debug/log/GenerateTest");

	private static boolean THROW_ERROR = TMEclipseRegistryPlugin
			.isDebugOptionEnabled("org.eclipse.tm4e.ui/debug/log/ThrowError");

	private static final ILogger RECONCILER_LOGGER = new EclipseSystemLogger(
			"org.eclipse.tm4e.ui/debug/log/TMPresentationReconciler");

	/**
	 * The default text attribute if non is returned as data by the current token
	 */
	private final Token defaultToken;

	/** The target viewer. */
	private ITextViewer viewer;
	/** The internal listener. */
	private final InternalListener internalListener;

	private IGrammar grammar;
	private boolean forcedGrammar;

	private ITokenProvider tokenProvider;
	private boolean forcedTheme;

	private final TextAttribute fDefaultTextAttribute;

	private IPreferenceChangeListener themeChangeListener;

	private List<ITMPresentationReconcilerListener> listeners;

	private ILogger logger;

	private boolean initializeViewerColors;

	private boolean updateTextDecorations;

	private boolean openImportDialogWhenGrammarNotFound;

	/**
	 * true if the presentation reconciler is enabled (grammar and theme are
	 * available) and false otherwise.
	 */
	private boolean enabled;

	/**
	 * true if a {@link TMException} should be thrown if grammar or theme cannot be
	 * found and false otherwise.
	 */
	private boolean throwError;

	public TMPresentationReconciler() {
		this.defaultToken = new Token(null);
		this.internalListener = new InternalListener();
		this.fDefaultTextAttribute = new TextAttribute(null);
		listeners = null;
		if (GENERATE_TEST) {
			addTMPresentationReconcilerListener(new TMPresentationReconcilerTestGenerator());
		}
		setThrowError(THROW_ERROR);
	}

	/**
	 * Listener to recolorize editors when E4 Theme from General / Appearance
	 * preferences changed or TextMate theme changed..
	 *
	 */
	private class ThemeChangeListener implements IPreferenceChangeListener {

		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			IThemeManager themeManager = TMUIPlugin.getThemeManager();
			if (PreferenceConstants.E4_THEME_ID.equals(event.getKey())) {
				preferenceThemeChange((String) event.getNewValue(), themeManager);
			} else if (PreferenceConstants.THEME_ASSOCIATIONS.equals(event.getKey())) {
				preferenceThemeChange(PreferenceUtils.getE4PreferenceCSSThemeId(), themeManager);
			}
		}

		private void preferenceThemeChange(String eclipseThemeId, IThemeManager themeManager) {
			IDocument document = viewer.getDocument();
			if (document == null) {
				return;
			}
			IGrammar grammar = TMPresentationReconciler.this.grammar;
			if (grammar == null) {
				return;
			}
			if (forcedTheme) {
				// The theme was forced, don't update it.
				return;
			}
			ITokenProvider oldTheme = tokenProvider;
			// Select the well TextMate theme from the given E4 theme id.
			boolean dark = themeManager.isDarkEclipseTheme(eclipseThemeId);
			ITokenProvider newTheme = themeManager.getThemeForScope(grammar.getScopeName(), dark);
			themeChange(oldTheme, newTheme, document);
		}
	};

	/**
	 * Internal listener class.
	 */
	class InternalListener implements ITextInputListener, IModelTokensChangedListener, ITextListener {

		@Override
		public void inputDocumentAboutToBeChanged(IDocument oldDocument, IDocument newDocument) {
			if (oldDocument != null) {
				viewer.removeTextListener(this);
				getTMModelManager().disconnect(oldDocument);
				fireUninstall();
			}
		}

		@Override
		public void inputDocumentChanged(IDocument oldDocument, IDocument newDocument) {
			if (newDocument != null) {
				fireInstall(viewer, newDocument);
				try {
					viewer.addTextListener(this);
					// Update the grammar
					IGrammar grammar = forcedGrammar ? TMPresentationReconciler.this.grammar : null;
					if (grammar == null) {
						ContentTypeInfo info = ContentTypeHelper.findContentTypes(newDocument);
						if (info != null) {
							grammar = findGrammar(info);
							if (grammar == null && isOpenImportDialogWhenGrammarNotFound()) {
								// Grammar cannot be found, suggest the user to
								// register the grammar
								Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								if (MessageDialog.openConfirm(activeShell,
										TMUIMessages.TMPresentationReconciler_register_dialog_title,
										NLS.bind(TMUIMessages.TMPresentationReconciler_register_dialog_message,
												info.getFileName()))) {
									TextMateGrammarImportWizard wizard = new TextMateGrammarImportWizard();
									WizardDialog dialog = new WizardDialog(activeShell, wizard);
									if (dialog.open() == Window.OK) {
										// User grammar was created in the
										// registry, retry to find
										// the grammar.
										grammar = findGrammar(info);
									}
								}
							}
						}
					}

					if (grammar != null) {
						TMPresentationReconciler.this.grammar = grammar;
					} else if (isThrowError()) {
						throw new TMException("Cannot find TextMate grammar for the given document");
					}

					// Update the theme
					if (grammar != null) {
						String scopeName = grammar.getScopeName();
						if (tokenProvider == null) {
							tokenProvider = TMUIPlugin.getThemeManager().getThemeForScope(scopeName);
						}
						if (tokenProvider != null) {
							applyThemeEditor();
						} else if (isThrowError()) {
							throw new TMException("Cannot find Theme for the given grammar '" + scopeName + "'");
						}
					}

					boolean enable = TMPresentationReconciler.this.enabled = grammar != null && tokenProvider != null;
					if (enable) {
						// Connect a TextModel to the new document.
						ITMModel model = getTMModelManager().connect(newDocument);
						model.setGrammar(grammar);

						// Add model listener
						model.addModelTokensChangedListener(this);
					}
				} catch (CoreException e) {
					if (logger.isEnabled()) {
						logger.log("Error while initializing TextMate model.", e);
					}
				}
			}
		}

		private IGrammar findGrammar(ContentTypeInfo info) {
			IGrammar grammar;
			IContentType[] contentTypes = info.getContentTypes();
			// Discover the well grammar from the contentTypes
			grammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(contentTypes);
			if (grammar == null) {
				// Discover the well grammar from the filetype
				String fileName = info.getFileName();
				if (fileName != null) {
					String fileType = new Path(fileName).getFileExtension();
					grammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarForFileType(fileType);
				}
			}
			return grammar;
		}

		@Override
		public void textChanged(TextEvent e) {
			if (!e.getViewerRedrawState()) {
				return;
			}
			// changed text: propagate previous style, which will be overridden
			// later asynchronously by TM
			if (e.getDocumentEvent() != null) {
				int diff = e.getText().length() - e.getLength();
				if (diff > 0 && e.getOffset() > 0) {
					StyleRange range = viewer.getTextWidget().getStyleRangeAtOffset(e.getOffset() - 1);
					if (range != null) {
						range.length += diff;
						viewer.getTextWidget().setStyleRange(range);
					}
				}
			} else { // TextViewer#invalidateTextPresentation is called (because
						// of validation, folding, etc)
				// case 2), do the colorization.
				IDocument document = viewer.getDocument();
				if (document != null) {
					IRegion region = null;
					int fromLineNumber = -1;
					int toLineNumber = -1;
					if (e.getOffset() == 0 && e.getLength() == 0 && e.getText() == null) {
						// redraw state change, damage the whole document
						fromLineNumber = 0;
						toLineNumber = document.getNumberOfLines() - 1;
					} else {
						region = widgetRegion2ModelRegion(e);
						if (region != null) {
							if (region.getLength() == 0) {
								// Some text was removed, don't colorize it.
								return;
							}
							try {
								String text = document.get(region.getOffset(), region.getLength());
								DocumentEvent de = new DocumentEvent(document, region.getOffset(), region.getLength(),
										text);
								fromLineNumber = DocumentHelper.getStartLine(de);
								toLineNumber = DocumentHelper.getEndLine(de, false);
							} catch (BadLocationException x) {
							}
						}
					}
					if (enabled) {
						// case where there is grammar & theme -> update text presentation with the
						// grammar tokens
						ITMModel model = getTMModelManager().connect(document);
						colorize(fromLineNumber, toLineNumber, region, (TMDocumentModel) model);
					} else {
						if (region != null) {
							// case where there is no grammar & theme -> update text presentation with the
							// default styles (ex: to support highlighting with GenericEditor)
							TextPresentation presentation = new TextPresentation(region, 100);
							presentation.setDefaultStyleRange(
									new StyleRange(region.getOffset(), region.getLength(), null, null));
							applyTextRegionCollection(presentation);
						}
					}
				}
			}
		}

		/**
		 * Translates the given text event into the corresponding range of the viewer's
		 * document.
		 * 
		 * @param e
		 *            the text event
		 * @return the widget region corresponding the region of the given event or
		 *         <code>null</code> if none
		 * @since 2.1
		 */
		private IRegion widgetRegion2ModelRegion(TextEvent e) {

			String text = e.getText();
			int length = text == null ? 0 : text.length();

			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
				return extension.widgetRange2ModelRange(new Region(e.getOffset(), length));
			}

			IRegion visible = viewer.getVisibleRegion();
			IRegion region = new Region(e.getOffset() + visible.getOffset(), length);
			return region;
		}

		@Override
		public void modelTokensChanged(ModelTokensChangedEvent e) {
			Control control = viewer.getTextWidget();
			if (control != null) {
				control.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (viewer != null) {
							colorize(e);
						}
					}
				});
			}
		}
	}

	public void setGrammar(IGrammar grammar) {
		boolean changed = (viewer != null && ((this.grammar == null) || !this.grammar.equals(grammar)));
		this.grammar = grammar;
		this.forcedGrammar = true;
		if (changed) {
			// Grammar has changed, recreate the TextMate model
			IDocument document = viewer.getDocument();
			if (document == null) {
				return;
			}
			internalListener.inputDocumentAboutToBeChanged(viewer.getDocument(), null);
			internalListener.inputDocumentChanged(null, document);
		}
	}

	public IGrammar getGrammar() {
		return grammar;
	}

	public ITokenProvider getTokenProvider() {
		return tokenProvider;
	}

	public void setTokenProvider(ITokenProvider tokenProvider) {
		ITokenProvider oldTheme = this.tokenProvider;
		ITokenProvider newTheme = tokenProvider;
		boolean changed = ((oldTheme == null) || !oldTheme.equals(newTheme));
		this.tokenProvider = tokenProvider;
		this.forcedTheme = true;
		if (changed) {
			IGrammar grammar = TMPresentationReconciler.this.grammar;
			if (grammar != null) {
				themeChange(oldTheme, newTheme, viewer.getDocument());
			}
		}
	}

	/**
	 * Force the TextMate theme id to use for the editor.
	 * 
	 * @param themeId
	 */
	public void setThemeId(String themeId) {
		setTokenProvider(TMUIPlugin.getThemeManager().getThemeById(themeId));
	}

	/**
	 * Apply theme changed.
	 * 
	 * @param oldTheme
	 * @param newTheme
	 * @param document
	 */
	private void themeChange(ITokenProvider oldTheme, ITokenProvider newTheme, IDocument document) {
		if (newTheme != null && !newTheme.equals(oldTheme)) {
			// Theme has changed, recolorize
			tokenProvider = newTheme;
			applyThemeEditor();
			ITMModel model = getTMModelManager().connect(document);
			colorize(0, document.getNumberOfLines() - 1, null, (TMDocumentModel) model);
		}
	}

	@Override
	public void install(ITextViewer viewer) {
		Assert.isNotNull(viewer);

		this.viewer = viewer;
		viewer.addTextInputListener(internalListener);

		IDocument document = viewer.getDocument();
		if (document != null) {
			internalListener.inputDocumentChanged(null, document);
		}
		themeChangeListener = new ThemeChangeListener();
		ThemeManager.getInstance().addPreferenceChangeListener(themeChangeListener);
	}

	@Override
	public void uninstall() {
		viewer.removeTextInputListener(internalListener);
		// Ensure we uninstall all listeners
		internalListener.inputDocumentAboutToBeChanged(viewer.getDocument(), null);
		if (themeChangeListener != null) {
			ThemeManager.getInstance().removePreferenceChangeListener(themeChangeListener);
		}
		themeChangeListener = null;
	}

	@Override
	public IPresentationDamager getDamager(String contentType) {
		return null;
	}

	@Override
	public IPresentationRepairer getRepairer(String contentType) {
		return null;
	}

	private ITMModelManager getTMModelManager() {
		return TMUIPlugin.getTMModelManager();
	}

	private void colorize(ModelTokensChangedEvent e) {
		for (Range range : e.getRanges()) {
			colorize(range.fromLineNumber - 1, range.toLineNumber - 1, null, ((TMDocumentModel) e.getModel()));
		}
	}

	private void colorize(int fromLineNumber, int toLineNumber, IRegion damage, TMDocumentModel model) {
		applyThemeEditorIfNeeded();
		ILogger logger = getLogger();
		// Refresh the UI Presentation
		if (logger.isEnabled()) {
			logger.log("Render from: " + fromLineNumber + " to: " + toLineNumber);
		}
		TextPresentation presentation = null;
		Throwable error = null;
		try {
			IDocument document = model.getDocument();
			presentation = new TextPresentation(
					damage != null ? damage : DocumentHelper.getRegion(document, fromLineNumber, toLineNumber), 1000);

			int lastStart = presentation.getExtent().getOffset();
			int length = 0;
			boolean firstToken = true;
			IToken lastToken = Token.UNDEFINED;
			TextAttribute lastAttribute = getTokenTextAttribute(lastToken);

			List<TMToken> tokens = null;
			for (int line = fromLineNumber; line <= toLineNumber; line++) {
				tokens = model.getLineTokens(line);
				if (tokens == null) {
					// TextMate tokens was not computed for this line.
					// This case comes from when the viewer is invalidated (by
					// validation for instance) and textChanged is called.
					// see https://github.com/eclipse/tm4e/issues/78
					if (logger.isEnabled()) {
						logger.log("TextMate tokens not available for line " + line);
					}
					break;
				}
				int startLineOffset = document.getLineOffset(line);
				for (int i = 0; i < tokens.size(); i++) {
					TMToken currentToken = tokens.get(i);
					TMToken nextToken = (i + 1 < tokens.size()) ? tokens.get(i + 1) : null;
					int tokenStartIndex = currentToken.startIndex;

					if (damage != null) {
						// Damage region is setted (this case comes from when
						// hyperlink, occurences, folding are processed and call
						// TextViewer#invalidateTextPresentation)
						if (isBeforeRegion(currentToken, startLineOffset, damage)) {
							// The token is before the damage region
							if (nextToken != null) {
								if (isBeforeRegion(nextToken, startLineOffset, damage)) {
									// ignore it
									continue;
								} else {
									tokenStartIndex = damage.getOffset() - startLineOffset;
								}
							} else {
								tokenStartIndex = damage.getOffset() - startLineOffset;
								IToken token = toToken(currentToken);
								lastAttribute = getTokenTextAttribute(token);
								length += getTokenLengh(tokenStartIndex, nextToken, line, document);
								firstToken = false;
								// ignore it
								continue;
							}
						} else if (isAfterRegion(currentToken, startLineOffset, damage)) {
							// The token is after the damage region, stop the
							// colorization process
							break;
						}
					}

					IToken token = toToken(currentToken);
					TextAttribute attribute = getTokenTextAttribute(token);
					if (lastAttribute != null && lastAttribute.equals(attribute)) {
						length += getTokenLengh(tokenStartIndex, nextToken, line, document);
						firstToken = false;
					} else {
						if (!firstToken)
							addRange(presentation, lastStart, length, lastAttribute);
						firstToken = false;
						lastToken = token;
						lastAttribute = attribute;
						lastStart = tokenStartIndex + startLineOffset;
						length = getTokenLengh(tokenStartIndex, nextToken, line, document);
					}
				}
			}
			if (damage != null) {
				// adjust the length
				length = Math.min(length, damage.getOffset() + damage.getLength() - lastStart);
			}
			addRange(presentation, lastStart, length, lastAttribute);
			applyTextRegionCollection(presentation);
		} catch (Throwable e) {
			error = e;
			if (logger.isEnabled()) {
				logger.log("Error while rendering from: " + fromLineNumber + " to: " + toLineNumber, e);
			}
		} finally {
			fireColorize(presentation, error);
		}
	}

	/**
	 * Return true if the given token is before the given region and false
	 * otherwise.
	 * 
	 * @param token
	 * @param startLineOffset
	 * @param damage
	 * @return
	 */
	private boolean isBeforeRegion(TMToken token, int startLineOffset, IRegion damage) {
		return token.startIndex + startLineOffset < damage.getOffset();
	}

	/**
	 * Return true if the given token is after the given region and false otherwise.
	 * 
	 * @param t
	 * @param startLineOffset
	 * @param damage
	 * @return
	 */
	private boolean isAfterRegion(TMToken t, int startLineOffset, IRegion damage) {
		return t.startIndex + startLineOffset >= damage.getOffset() + damage.getLength();
	}

	private IToken toToken(TMToken t) {
		IToken token = getTokenProvider().getToken(t.type);
		if (token != null) {
			return token;
		}
		return defaultToken;
	}

	private int getTokenLengh(int tokenStartIndex, TMToken nextToken, int line, IDocument document)
			throws BadLocationException {
		if (nextToken != null) {
			return nextToken.startIndex - tokenStartIndex;
		}
		return DocumentHelper.getLineLength(document, line) - tokenStartIndex;
	}

	/**
	 * Returns a text attribute encoded in the given token. If the token's data is
	 * not <code>null</code> and a text attribute it is assumed that it is the
	 * encoded text attribute. It returns the default text attribute if there is no
	 * encoded text attribute found.
	 *
	 * @param token
	 *            the token whose text attribute is to be determined
	 * @return the token's text attribute
	 */
	protected TextAttribute getTokenTextAttribute(IToken token) {
		Object data = token.getData();
		if (data instanceof TextAttribute)
			return (TextAttribute) data;
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
	protected void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
		if (attr != null) {
			int style = attr.getStyle();
			int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
			StyleRange styleRange = new StyleRange(offset, length, attr.getForeground(), attr.getBackground(),
					fontStyle);
			styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
			styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
			styleRange.font = attr.getFont();
			presentation.addStyleRange(styleRange);
		}
	}

	/**
	 * Applies the given text presentation to the text viewer the presentation
	 * reconciler is installed on.
	 *
	 * @param presentation
	 *            the text presentation to be applied to the text viewer
	 */
	private void applyTextRegionCollection(TextPresentation presentation) {
		viewer.changeTextPresentation(presentation, false);
	}

	/**
	 * Add a TextMate presentation reconciler listener.
	 * 
	 * @param listener
	 *            the TextMate presentation reconciler listener to add.
	 */
	public void addTMPresentationReconcilerListener(ITMPresentationReconcilerListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
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
	public void removeTMPresentationReconcilerListener(ITMPresentationReconcilerListener listener) {
		if (listeners == null) {
			return;
		}
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void fireInstall(ITextViewer viewer, IDocument document) {
		if (listeners == null) {
			return;
		}
		synchronized (listeners) {
			for (ITMPresentationReconcilerListener listener : listeners) {
				listener.install(viewer, document);
			}
		}
	}

	private void fireUninstall() {
		if (listeners == null) {
			return;
		}
		synchronized (listeners) {
			for (ITMPresentationReconcilerListener listener : listeners) {
				listener.uninstall();
			}
		}
	}

	/**
	 * Fire colorize.
	 * 
	 * @param presentation
	 * @param error
	 */
	private void fireColorize(TextPresentation presentation, Throwable error) {
		if (listeners == null) {
			return;
		}
		synchronized (listeners) {
			for (ITMPresentationReconcilerListener listener : listeners) {
				listener.colorize(presentation, error);
			}
		}
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	public ILogger getLogger() {
		if (logger == null) {
			return RECONCILER_LOGGER;
		}
		return logger;
	}

	public static TMPresentationReconciler getTMPresentationReconciler(IEditorPart editorPart) {
		ITextOperationTarget target = editorPart.getAdapter(ITextOperationTarget.class);
		if (target instanceof ITextViewer) {
			ITextViewer textViewer = ((ITextViewer) target);
			return TMPresentationReconciler.getTMPresentationReconciler(textViewer);
		}
		return null;
	}

	/**
	 * Returns the {@link TMPresentationReconciler} of the given text viewer and
	 * null otherwise.
	 * 
	 * @param textViewer
	 * @return the {@link TMPresentationReconciler} of the given text viewer and
	 *         null otherwise.
	 */
	public static TMPresentationReconciler getTMPresentationReconciler(ITextViewer textViewer) {
		try {
			Field field = SourceViewer.class.getDeclaredField("fPresentationReconciler");
			if (field != null) {
				field.setAccessible(true);
				IPresentationReconciler presentationReconciler = (IPresentationReconciler) field.get(textViewer);
				return presentationReconciler instanceof TMPresentationReconciler
						? (TMPresentationReconciler) presentationReconciler
						: null;
			}
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * Initialize foreground, background color, current line highlight from the
	 * current theme.
	 * 
	 */
	private void applyThemeEditor() {
		this.initializeViewerColors = false;
		this.updateTextDecorations = false;
		applyThemeEditorIfNeeded();
	}

	/**
	 * Initialize foreground, background color, current line highlight from the
	 * current theme if needed.
	 * 
	 */
	private void applyThemeEditorIfNeeded() {
		if (!initializeViewerColors) {
			StyledText styledText = viewer.getTextWidget();
			((ITheme) tokenProvider).initializeViewerColors(styledText);
			initializeViewerColors = true;
		}
		if (!updateTextDecorations) {
			try {
				// Ugly code to update "current line highlight" :
				// - get the PaintManager from the ITextViewer with reflection.
				// - get the list of IPainter of PaintManager with reflection
				// - loop for IPainter to retrieve CursorLinePainter which manages "current line
				// highlight".
				PaintManager paintManager = ClassHelper.getFieldValue(viewer, "fPaintManager", TextViewer.class);
				if (paintManager != null) {
					List<IPainter> painters = ClassHelper.getFieldValue(paintManager, "fPainters", PaintManager.class);
					if (painters != null) {
						for (IPainter painter : painters) {
							if (painter instanceof CursorLinePainter) {
								// Update current line highlight
								Color background = tokenProvider.getEditorCurrentLineHighlight();
								if (background != null) {
									((CursorLinePainter) painter).setHighlightColor(background);
								}
								updateTextDecorations = true;
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set to true when the "Import Grammar" wizard dialog must be opened when
	 * grammar is not found and false otherwise.
	 * 
	 * @param openImportDialogWhenGrammarNotFound
	 */
	public void setOpenImportDialogWhenGrammarNotFound(boolean openImportDialogWhenGrammarNotFound) {
		this.openImportDialogWhenGrammarNotFound = openImportDialogWhenGrammarNotFound;
	}

	/**
	 * Returns true when the "Import Grammar" wizard dialog must be opened when
	 * grammar is not found and false otherwise.
	 * 
	 * @return
	 */
	public boolean isOpenImportDialogWhenGrammarNotFound() {
		return openImportDialogWhenGrammarNotFound;
	}

	/**
	 * Set true if a {@link TMException} should be thrown if grammar or theme cannot
	 * be found and false otherwise.
	 * 
	 * @param throwError
	 */
	public void setThrowError(boolean throwError) {
		this.throwError = throwError;
	}

	/**
	 * Return true if a {@link TMException} should be thrown if grammar or theme
	 * cannot be found and false otherwise.
	 * 
	 * @return true if a {@link TMException} should be thrown if grammar or theme
	 *         cannot be found and false otherwise.
	 */
	public boolean isThrowError() {
		return throwError;
	}

	/**
	 * Returns true if the presentation reconciler is enabled (grammar and theme are
	 * available) and false otherwise.
	 * 
	 * @return true if the presentation reconciler is enabled (grammar and theme are
	 *         available) and false otherwise.
	 */

	public boolean isEnabled() {
		return enabled;
	}
}
