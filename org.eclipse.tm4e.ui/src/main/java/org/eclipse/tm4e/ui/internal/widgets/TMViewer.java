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
package org.eclipse.tm4e.ui.internal.widgets;

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
 *
 */
public class TMViewer extends SourceViewer {

	private TMPresentationReconciler reconciler;

	public TMViewer(Composite parent, IVerticalRuler ruler, int styles) {
		super(parent, ruler, styles);
		init();
	}

	public TMViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
			boolean showAnnotationsOverview, int styles) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
		init();
	}

	private void init() {
		this.reconciler = new TMPresentationReconciler();
		SourceViewerConfiguration configuration = new TMSourceViewerConfiguration();
		this.configure(configuration);
	}

	private class TMSourceViewerConfiguration extends SourceViewerConfiguration {

		@Override
		public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
			return reconciler;
		}

	}

	public void setGrammar(IGrammar grammar) {
		reconciler.setGrammar(grammar);
		if (getDocument() == null) {
			super.setDocument(new Document());
		}
	}

	public void setTheme(ITheme theme) {
		reconciler.setTheme(theme);
		StyledText styledText = getTextWidget();
		styledText.setForeground(null);
		styledText.setBackground(null);
		theme.initializeViewerColors(styledText);
		getTextWidget().setFont(JFaceResources.getTextFont());
	}

	public void setText(String text) {
		if (getDocument() == null) {
			super.setDocument(new Document());
		}
		getDocument().set(text);
	}
}
