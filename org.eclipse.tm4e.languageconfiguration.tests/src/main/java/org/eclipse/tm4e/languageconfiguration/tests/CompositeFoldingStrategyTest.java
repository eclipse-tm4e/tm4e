/**
 * Copyright (c) 2026 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.languageconfiguration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm4e.languageconfiguration.internal.folding.CompositeFoldingStrategy;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that reconciling or closing one editor does not affect another editor's folding state.
 * Reconciliation runs directly so the assertions do not depend on background job timing.
 */
class CompositeFoldingStrategyTest {

	private final CompositeFoldingStrategy firstStrategy = new CompositeFoldingStrategy();
	private final CompositeFoldingStrategy secondStrategy = new CompositeFoldingStrategy();
	private Shell shell;
	private ProjectionViewer firstViewer;
	private ProjectionViewer secondViewer;

	@BeforeEach
	void setup() {
		shell = new Shell(UI.getDisplay());
		firstViewer = createViewer(firstStrategy, "first\n    child\nend\n");
		secondViewer = createViewer(secondStrategy, "second\n    child\nend\n");
	}

	private ProjectionViewer createViewer(final CompositeFoldingStrategy strategy, final String text) {
		final var viewer = new ProjectionViewer(shell, null, null, false, SWT.NONE);
		viewer.setDocument(new Document(text), new AnnotationModel());
		viewer.enableProjection();
		strategy.install(viewer);
		strategy.setDocument(viewer.getDocument());
		return viewer;
	}

	@AfterEach
	void teardown() {
		firstStrategy.uninstall();
		secondStrategy.uninstall();
		shell.dispose();
	}

	@Test
	void reconcilingOnlyUpdatesItsOwnEditor() throws BadLocationException {
		firstStrategy.initialReconcile();
		assertFoldingRegions(firstViewer, "first\n    child\n");
		assertFoldingRegions(secondViewer);

		secondStrategy.initialReconcile();
		assertFoldingRegions(secondViewer, "second\n    child\n");
		assertFoldingRegions(firstViewer, "first\n    child\n");
	}

	@Test
	void uninstallingOneEditorKeepsTheOtherEditorsFolding() throws BadLocationException {
		firstStrategy.uninstall();
		secondStrategy.initialReconcile();
		assertFoldingRegions(secondViewer, "second\n    child\n");
	}

	private static void assertFoldingRegions(final ProjectionViewer viewer, final String... expected) throws BadLocationException {
		final var model = viewer.getProjectionAnnotationModel();
		final var regions = new ArrayList<String>();
		final var annotations = model.getAnnotationIterator();
		while (annotations.hasNext()) {
			final var position = model.getPosition(annotations.next());
			regions.add(viewer.getDocument().get(position.offset, position.length));
		}
		assertThat(regions).containsExactlyInAnyOrder(expected);
	}
}
