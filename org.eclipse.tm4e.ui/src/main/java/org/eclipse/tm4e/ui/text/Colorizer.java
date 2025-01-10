/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * Copyright (c) 2021-2023 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Sebastian Thomschke (Vegard IT GmbH) - code cleanup, bug fixes, performance improvements
 */
package org.eclipse.tm4e.ui.text;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.tm4e.core.model.ModelTokensChangedEvent;
import org.eclipse.tm4e.core.model.Range;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.utils.ClassHelper;
import org.eclipse.tm4e.ui.model.ITMDocumentModel;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.ITokenProvider;

class Colorizer {
	private static final TextAttribute DEFAULT_TEXT_ATTRIBUTE = new TextAttribute(null);

	private final ITokenProvider theme;
	private final ITextViewer viewer;

	private final Set<ITMPresentationReconcilerListener> listeners;

	private boolean isViewerStyleColorsInitialized;
	private boolean isViewerHighlightColorInitialized;

	Colorizer(final ITextViewer viewer, final ITokenProvider theme, final Set<ITMPresentationReconcilerListener> listeners) {
		this.viewer = viewer;
		this.theme = theme;
		this.listeners = listeners;
		applyThemeToViewer();
	}

	void colorize(final ModelTokensChangedEvent event) {
		final IDocument doc = viewer.getDocument();
		if (doc == null)
			return;

		if (event.model instanceof final ITMDocumentModel docModel) {
			if (docModel.getDocument() != doc)
				return;
			for (final Range range : event.ranges) {
				try {
					final int length = doc.getLineOffset(range.toLineNumber - 1) + doc.getLineLength(range.toLineNumber - 1)
							- doc.getLineOffset(range.fromLineNumber - 1);
					final var region = new Region(doc.getLineOffset(range.fromLineNumber - 1), length);
					colorize(region, docModel);
				} catch (final BadLocationException ex) {
					TMUIPlugin.logError(ex);
				}
			}
		}
	}

	void colorize(final IRegion damageRegion, final ITMDocumentModel tmModel) throws BadLocationException {
		applyThemeToViewerIfNeeded();

		final IDocument doc = tmModel.getDocument();
		final int fromLineIndex = doc.getLineOfOffset(damageRegion.getOffset());
		final int toLineIndex = doc.getLineOfOffset(damageRegion.getOffset() + damageRegion.getLength());

		// Refresh the UI Presentation
		if (TMUIPlugin.isLogTraceEnabled())
			TMUIPlugin.logTrace("Colorize lines from " + (fromLineIndex + 1) + " to " + (toLineIndex + 1));
		final var presentation = new TextPresentation(damageRegion, 1000);
		Exception error = null;

		final var theme = this.theme;

		try {
			int lastStart = presentation.getExtent().getOffset();
			int length = 0;
			boolean firstToken = true;
			IToken lastToken = Token.UNDEFINED;
			TextAttribute lastAttribute = getTokenTextAttribute(lastToken);

			List<TMToken> tokens = null;
			for (int lineIndex = fromLineIndex; lineIndex <= toLineIndex; lineIndex++) {
				tokens = tmModel.getLineTokens(lineIndex);
				if (tokens == null) {
					if (TMUIPlugin.isLogTraceEnabled())
						TMUIPlugin.logTrace("TextMate tokens not yet available for line " + lineIndex);
					continue;
				}
				final int startLineOffset = doc.getLineOffset(lineIndex);
				int nextTokenIndex = 0;
				for (final TMToken currentToken : tokens) {
					nextTokenIndex++;
					final @Nullable TMToken nextToken = nextTokenIndex < tokens.size()
							? tokens.get(nextTokenIndex)
							: null;
					int tokenStartIndex = currentToken.startIndex;

					// check if the current token is before the damaged region
					if (isTokenBeforeRegion(currentToken, startLineOffset, damageRegion)) {
						if (nextToken != null) {
							if (isTokenBeforeRegion(nextToken, startLineOffset, damageRegion)) {
								continue; // ignore it
							}
							tokenStartIndex = damageRegion.getOffset() - startLineOffset;
						} else {
							tokenStartIndex = damageRegion.getOffset() - startLineOffset;
							final IToken token = theme == null ? ITokenProvider.DEFAULT_TOKEN : theme.getToken(currentToken.type);
							lastAttribute = getTokenTextAttribute(token);
							length += getTokenLength(tokenStartIndex, nextToken, lineIndex, doc);
							firstToken = false;
							// ignore it
							continue;
						}
					}

					// check if the current token is after the damaged region
					else if (isTokenAfterRegion(currentToken, startLineOffset, damageRegion)) {
						// after damaged region -> stop the colorization process
						break;
					}

					final IToken token = theme == null ? ITokenProvider.DEFAULT_TOKEN : theme.getToken(currentToken.type);
					final TextAttribute attribute = getTokenTextAttribute(token);
					if (lastAttribute.equals(attribute)) {
						length += getTokenLength(tokenStartIndex, nextToken, lineIndex, doc);
						firstToken = false;
					} else {
						if (!firstToken) {
							addStyleRange(presentation, lastStart, length, lastAttribute);
						}
						firstToken = false;
						lastToken = token;
						lastAttribute = attribute;
						lastStart = tokenStartIndex + startLineOffset;
						length = getTokenLength(tokenStartIndex, nextToken, lineIndex, doc);
					}
				}
			}
			// adjust the length
			length = Math.min(length, damageRegion.getOffset() + damageRegion.getLength() - lastStart);
			addStyleRange(presentation, lastStart, length, lastAttribute);
			viewer.changeTextPresentation(presentation, false);
		} catch (final IllegalArgumentException | BadLocationException ex) {
			error = ex;
			// These exceptions can be thrown if there is a delay running the tokenizer thread
			// and the tokens become out of sync with the document line data.
			// As this is an expected state, only log them if tracing is enabled.
			if (TMUIPlugin.isLogTraceEnabled())
				TMUIPlugin.logError(ex);
		} catch (final Exception ex) {
			error = ex;
			TMUIPlugin.logError(ex);
		} finally {
			final var err = error;
			listeners.forEach(l -> l.onColorized(presentation, err));
		}
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
	 */
	private void addStyleRange(final TextPresentation presentation, final int offset, final int length, final TextAttribute attr) {
		final int style = attr.getStyle();
		final int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
		final var styleRange = new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), fontStyle);
		styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
		styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
		styleRange.font = attr.getFont();
		presentation.addStyleRange(styleRange);
	}

	/**
	 * Initialize foreground, background color, current line highlight from the current theme.
	 */
	private void applyThemeToViewer() {
		this.isViewerStyleColorsInitialized = false;
		this.isViewerHighlightColorInitialized = false;
		applyThemeToViewerIfNeeded();
	}

	/**
	 * Initialize foreground, background color, current line highlight from the current theme if needed.
	 */
	private void applyThemeToViewerIfNeeded() {
		if (!isViewerStyleColorsInitialized && this.theme instanceof final ITheme theTheme) {
			final StyledText styledText = viewer.getTextWidget();
			theTheme.initializeViewerColors(styledText);
			isViewerStyleColorsInitialized = true;
		}

		if (!isViewerHighlightColorInitialized) {
			try {
				// Ugly code to update "current line highlight" :
				// - get the PaintManager from the ITextViewer with reflection.
				// - get the list of IPainter of PaintManager with reflection
				// - loop for IPainter to retrieve CursorLinePainter which manages "current line highlight".
				final PaintManager paintManager = ClassHelper.getFieldValue(viewer, "fPaintManager", TextViewer.class);
				if (paintManager == null)
					return;

				final List<IPainter> painters = ClassHelper.getFieldValue(paintManager, "fPainters", PaintManager.class);
				if (painters == null)
					return;

				for (final IPainter painter : painters) {
					if (painter instanceof final CursorLinePainter cursorLinePainter) {
						// Update current line highlight
						final Color background = theme.getEditorCurrentLineHighlight();
						if (background != null) {
							cursorLinePainter.setHighlightColor(background);
						}
						isViewerHighlightColorInitialized = true;
					}
				}
			} catch (final Exception ex) {
				TMUIPlugin.logError(ex);
			}
		}
	}

	/**
	 * @return true if the given token is after the given region and false otherwise
	 */
	private boolean isTokenAfterRegion(final TMToken token, final int startLineOffset, final IRegion damage) {
		return token.startIndex + startLineOffset >= damage.getOffset() + damage.getLength();
	}

	/**
	 * @return true if the given token is before the given region and false otherwise
	 */
	private boolean isTokenBeforeRegion(final TMToken token, final int startLineOffset, final IRegion damage) {
		return token.startIndex + startLineOffset < damage.getOffset();
	}

	private int getTokenLength(final int tokenStartIndex, final @Nullable TMToken nextToken, final int line, final IDocument doc)
			throws BadLocationException {
		return nextToken == null
				? doc.getLineLength(line) - tokenStartIndex
				: nextToken.startIndex - tokenStartIndex;
	}

	/**
	 * @return the text attribute encoded in the given token or {@link #DEFAULT_TEXT_ATTRIBUTE}
	 */
	private TextAttribute getTokenTextAttribute(final IToken token) {
		return token.getData() instanceof final TextAttribute textAttr ? textAttr : DEFAULT_TEXT_ATTRIBUTE;
	}

	ITextViewer getTextViewer() {
		return viewer;
	}
}
