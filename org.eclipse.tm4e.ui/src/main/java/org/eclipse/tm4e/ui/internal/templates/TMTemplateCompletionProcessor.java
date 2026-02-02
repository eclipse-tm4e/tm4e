/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dietrich Travkin (SOLUNAR GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.templates;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.templates.ContextTypeRegistry;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.ui.TMImages;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.utils.CodeTemplateContextTypeUtils;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.model.ITMDocumentModel;
import org.eclipse.tm4e.ui.templates.CommentTemplateContextType;
import org.eclipse.tm4e.ui.templates.DefaultTMTemplateContextType;
import org.eclipse.tm4e.ui.templates.DocumentationCommentTemplateContextType;

public class TMTemplateCompletionProcessor extends TemplateCompletionProcessor {

	private static final Template[] NO_TEMPLATES = {};

	@Override
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
		// TODO Check why Invalid thread access exception occurs here without UI.runSync()
		return UI.runSync(() -> TMTemplateCompletionProcessor.super.computeCompletionProposals(viewer, offset));
	}

	private static class TmTokenRegion implements IRegion {

		private final TMToken token;
		private final int offset;
		private final int length;

		public TmTokenRegion(final TMToken token, final int offset, final int length) {
			this.token = token;
			this.offset = offset;
			this.length = length;
		}

		@Override
		public int getLength() {
			return this.length;
		}

		@Override
		public int getOffset() {
			return this.offset;
		}

		public TMToken getToken() {
			return this.token;
		}
	}

	private @Nullable TmTokenRegion retrieveTmTokenFor(final IDocument document, final int offset) {
		final ITMDocumentModel model = TMUIPlugin.getTMModelManager().connect(document);

		int lineIndex;
		int lineStartOffset;
		int lineLength;
		String lineDelimiter;
		try {
			lineIndex = document.getLineOfOffset(offset);
			lineStartOffset = document.getLineOffset(lineIndex);
			lineLength = document.getLineLength(lineIndex);
			lineDelimiter = document.getLineDelimiter(lineIndex);
		} catch (final BadLocationException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
			return null;
		}

		final List<TMToken> lineTokens = model.getLineTokens(lineIndex);
		if (lineTokens == null) {
			return null;
		}

		TMToken tokenAtOffset = null;
		TMToken nextToken = null;
		for (final TMToken token : lineTokens) {
			if (token.startIndex <= offset - lineStartOffset) {
				tokenAtOffset = token;
			} else {
				nextToken = token;
				break;
			}
		}

		if (tokenAtOffset == null) {
			return null;
		}

		int length;
		if (nextToken != null) {
			length = nextToken.startIndex - tokenAtOffset.startIndex;
		} else {
			length = lineLength - tokenAtOffset.startIndex;
			if (lineDelimiter != null) {
				length -= lineDelimiter.length();
			}
		}

		return new TmTokenRegion(tokenAtOffset, offset, length);
	}

	private @Nullable TemplateContextType retrieveTemplateContextType(final TMToken textMateToken) {
		final TMUIPlugin plugin = TMUIPlugin.getDefault();
		if (plugin == null) {
			return null;
		}

		// check language-agnostic context types (comment context types)
		final ContextTypeRegistry contextTypeRegistry = plugin.getTemplateContextRegistry();
		if (textMateToken.type.contains("comment")) {
			TemplateContextType contextType;
			if (textMateToken.type.contains("documentation")) {
				contextType = contextTypeRegistry.getContextType(DocumentationCommentTemplateContextType.CONTEXT_ID);
			} else {
				contextType = contextTypeRegistry.getContextType(CommentTemplateContextType.CONTEXT_ID);
			}

			if (contextType != null) {
				return contextType;
			}
		}

		// Check language-specific context types
		final TemplateContextType contextType = plugin.getTemplateContextRegistry().getContextType(
				CodeTemplateContextTypeUtils.toContextTypeId(textMateToken));
		if (contextType != null) {
			return contextType;
		}

		// last option
		return contextTypeRegistry.getContextType(DefaultTMTemplateContextType.CONTEXT_ID);
	}

	@Override
	protected @Nullable TemplateContextType getContextType(final ITextViewer viewer, final IRegion region) {
		final var plugin = TMUIPlugin.getDefault();
		if (plugin == null) {
			return null;
		}

		final var document = viewer.getDocument();
		if (document != null) {
			final TmTokenRegion tokenRegion = retrieveTmTokenFor(document, region.getOffset());
			if (tokenRegion != null) {
				return retrieveTemplateContextType(tokenRegion.getToken());
			}
		}

		return plugin.getTemplateContextRegistry().getContextType(DefaultTMTemplateContextType.CONTEXT_ID);
	}

	@Override
	protected @Nullable Image getImage(final Template template) {
		return TMImages.getImage(TMImages.IMG_TEMPLATE);
	}

	@Override
	protected Template[] getTemplates(final String contextTypeId) {
		final TMUIPlugin plugin = TMUIPlugin.getDefault();
		if (plugin == null) {
			return NO_TEMPLATES;
		}

		final TemplateStore templateStore = plugin.getTemplateStore();
		final Template[] customTemplates = templateStore.getTemplates(contextTypeId);

		if (customTemplates.length == 0) {
			return NO_TEMPLATES;
		}
		return customTemplates;
	}

}
