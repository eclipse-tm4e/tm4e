/*******************************************************************************
 * Copyright (c) 2024 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.hover;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.ui.internal.model.TMDocumentModel;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.preferences.PreferenceHelper;
import org.eclipse.ui.editors.text.EditorsUI;

public class TMTokenTextHover implements ITextHover, ITextHoverExtension {

	private static final class RegionWithTMToken extends Region {
		final TMToken token;
		final String tokenText;

		RegionWithTMToken(final int offset, final int length, final String tokenText, final TMToken token) {
			super(offset, length);
			this.tokenText = tokenText;
			this.token = token;
		}
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		// setup a hover control that interprets basic HTML input
		return new AbstractReusableInformationControlCreator() {
			@Override
			protected IInformationControl doCreateInformationControl(final @NonNullByDefault({}) Shell parent) {
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		};
	}

	@Override
	public @Nullable String getHoverInfo(final @NonNullByDefault({}) ITextViewer textViewer,
			final @NonNullByDefault({}) IRegion hoverRegion) {
		if (hoverRegion instanceof final RegionWithTMToken regionWithToken) {
			final var text = regionWithToken.tokenText.replace(' ', '·').replace('\t', '→');
			return "<b>" + text + "</b> (" + text.length()
					+ " chars)<br>"
					+ "<br>"
					+ "<b>Token Type:</b> " + regionWithToken.token.type + "<br>"
					+ "<b>TextMate Scopes:</b> <li>" + String.join("<li>", regionWithToken.token.scopes);
		}
		return null;
	}

	@Override
	public @Nullable IRegion getHoverRegion(final @NonNullByDefault({}) ITextViewer textViewer, final int offset) {
		if (!PreferenceHelper.isTMTokenHoverEnabled())
			return null;

		final @Nullable IDocument doc = textViewer.getDocument();
		if (doc == null)
			return null;

		final TMDocumentModel model = TMModelManager.INSTANCE.getConnectedModel(doc);
		if (model == null)
			return null;

		try {
			// retrieve parsed TM tokens of the hovered line
			final int lineIndex = doc.getLineOfOffset(offset);
			final var tokens = model.getLineTokens(lineIndex);
			if (tokens == null)
				return null;

			// find the TM token at the hover position
			final int lineStartOffset = doc.getLineOffset(lineIndex);
			TMToken hoveredToken = null;
			TMToken nextToken = null;
			for (final TMToken token : tokens) {
				if (token.startIndex <= offset - lineStartOffset) {
					hoveredToken = token;
				} else {
					nextToken = token;
					break;
				}
			}
			if (hoveredToken == null)
				return null;

			final int regionOffset = lineStartOffset + hoveredToken.startIndex;
			final int regionLength = nextToken == null
					? doc.getLineLength(lineIndex) - hoveredToken.startIndex
					: nextToken.startIndex - hoveredToken.startIndex;
			return new RegionWithTMToken(regionOffset, regionLength, doc.get(regionOffset, regionLength), hoveredToken);
		} catch (final BadLocationException e) {
			return null;
		}
	}
}
