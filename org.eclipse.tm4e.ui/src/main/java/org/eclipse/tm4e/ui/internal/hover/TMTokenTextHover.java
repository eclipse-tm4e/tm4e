/*******************************************************************************
 * Copyright (c) 2024-2025 Vegard IT GmbH and others.
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

import java.util.Objects;

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
import org.eclipse.tm4e.ui.text.ITMPartitionRegion;
import org.eclipse.tm4e.ui.text.TMPartitions;
import org.eclipse.ui.editors.text.EditorsUI;

public class TMTokenTextHover implements ITextHover, ITextHoverExtension {

	private static final class RegionWithTMToken extends Region {
		final TMToken token;
		final String tokenText;
		final @Nullable ITMPartitionRegion tmPartition;

		RegionWithTMToken(
				final int offset,
				final int length,
				final String tokenText,
				final TMToken token,
				final @Nullable ITMPartitionRegion tmPartitionRegion) {
			super(offset, length);
			this.tokenText = tokenText;
			this.token = token;
			tmPartition = tmPartitionRegion;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(tmPartition, token, tokenText, getOffset(), getLength());
			return result;
		}

		@Override
		public boolean equals(final @Nullable Object obj) {
			if (this == obj)
				return true;
			if (obj == null || !super.equals(obj))
				return false;
			if (obj instanceof final RegionWithTMToken other)
				return Objects.equals(tmPartition, other.tmPartition) //
						&& Objects.equals(token, other.token)
						&& Objects.equals(tokenText, other.tokenText)
						&& Objects.equals(getOffset(), other.getOffset()) //
						&& Objects.equals(getLength(), other.getLength());
			return false;
		}
	}

	@Override
	public @Nullable IInformationControlCreator getHoverControlCreator() {
		// setup a hover control that interprets basic HTML input
		return new AbstractReusableInformationControlCreator() {
			@Override
			protected IInformationControl doCreateInformationControl(final Shell parent) {
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		};
	}

	@Override
	public @Nullable String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
		if (hoverRegion instanceof final RegionWithTMToken regionWithToken) {
			final var text = regionWithToken.tokenText.replace(' ', '·').replace('\t', '→');

			// Partition details captured at region creation time
			String partitionInfo = "";
			final var reg = regionWithToken.tmPartition;
			if (reg != null) {
				partitionInfo = "<br>"
						+ "<b>JFace Text Partition:</b>"
						+ "<li><b>Offset:</b>" + reg.getOffset()
						+ "<li><b>Length:</b>" + reg.getLength()
						+ "<li><b>Type:</b> " + reg.getType()
						+ "<li><b>Grammar Scope:</b> " + reg.getGrammarScope();
			}

			return "<b>" + text + "</b> (" + text.length() + " chars)<br>"
					+ "<br>"
					+ "<b>Token Type:</b> " + regionWithToken.token.type + "<br>"
					+ "<b>Origin Grammar Scope:</b> " + regionWithToken.token.grammarScope + "<br>"
					+ "<b>TextMate Scopes:</b> <li>" + String.join("<li>", regionWithToken.token.scopes) + "<br>"
					+ partitionInfo;
		}
		return null;
	}

	@Override
	public @Nullable IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
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
			return new RegionWithTMToken(regionOffset, regionLength, doc.get(regionOffset, regionLength), hoveredToken,
					TMPartitions.getPartition(doc, offset));
		} catch (final BadLocationException e) {
			return null;
		}
	}
}
