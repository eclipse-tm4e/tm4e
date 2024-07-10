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
package org.eclipse.tm4e.ui.internal.widgets;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;
import org.eclipse.tm4e.ui.themes.ITheme;

/**
 * Simple TextMate Viewer.
 */
public final class TMViewer extends SourceViewer {

	private final class TMSourceViewerConfiguration extends SourceViewerConfiguration {
		@Override
		public IPresentationReconciler getPresentationReconciler(final @Nullable ISourceViewer sourceViewer) {
			return reconciler;
		}
	}

	private final TMPresentationReconciler reconciler = new TMPresentationReconciler();

	public TMViewer(final Composite parent, final int styles) {
		this(parent, null, null, false, styles);
	}

	public TMViewer(final Composite parent, final @Nullable IVerticalRuler verticalRuler, final @Nullable IOverviewRuler overviewRuler,
			final boolean showAnnotationsOverview, final int styles) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
		configure(new TMSourceViewerConfiguration());
	}

	public void setGrammar(final @Nullable IGrammar grammar) {
		reconciler.setGrammar(grammar);
		if (getDocument() == null) {
			setDocument(new Document());
		}
	}

	public void setTheme(final ITheme theme) {
		reconciler.setTheme(theme);
		final StyledText styledText = getTextWidget();
		styledText.setFont(JFaceResources.getTextFont());
		styledText.setForeground(null);
		styledText.setBackground(null);
		theme.initializeViewerColors(styledText);
	}

	public void setText(final String text) {
		if (getDocument() == null) {
			setDocument(new Document());
		}
		castNonNull(getDocument()).set(text);
	}
}
