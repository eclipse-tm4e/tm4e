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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
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
 * Also checks that background scans publish on the UI thread and cannot outlive their document contents or folding lifecycle.
 */
class CompositeFoldingStrategyTest {

	/** Pauses one real folding scan without depending on document size or regex performance. */
	private static final class PausingDocument extends Document {
		final CountDownLatch scanStarted = new CountDownLatch(1);
		final CountDownLatch releaseScan = new CountDownLatch(1);
		volatile boolean pauseNextScan;

		PausingDocument() {
			super("root\n    child\nend\n");
		}

		@Override
		public int getLineOffset(final int line) throws BadLocationException {
			if (pauseNextScan && !UI.isUIThread()) {
				pauseNextScan = false;
				scanStarted.countDown();
				try {
					if (!releaseScan.await(10, TimeUnit.SECONDS))
						throw new AssertionError("The test did not release the folding scan.");
				} catch (final InterruptedException ex) {
					Thread.currentThread().interrupt();
					throw new AssertionError(ex);
				}
			}
			return super.getLineOffset(line);
		}
	}

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

	@Test
	void backgroundScanPublishesAnnotationsOnUiThread() throws Exception {
		final var updateThread = new AtomicReference<Thread>();
		firstViewer.getProjectionAnnotationModel().addAnnotationModelListener(model -> updateThread.set(Thread.currentThread()));
		try (var worker = Executors.newSingleThreadExecutor()) {
			worker.submit(firstStrategy::initialReconcile).get(5, TimeUnit.SECONDS);
		}
		assertFoldingRegions(firstViewer, "first\n    child\n");
		assertThat(updateThread.get()).as("Annotation listeners run on the UI thread").isSameAs(Thread.currentThread());
	}

	@Test
	void completedScansDoNotQueueDuplicateFolds() throws Exception {
		try (var worker = Executors.newSingleThreadExecutor()) {
			// Keep the UI queue paused until both scans finish to expose comparisons made against stale annotations.
			worker.submit(firstStrategy::initialReconcile).get(5, TimeUnit.SECONDS);
			worker.submit(firstStrategy::initialReconcile).get(5, TimeUnit.SECONDS);
		}
		assertFoldingRegions(firstViewer, "first\n    child\n");
	}

	@Test
	void textChangesDiscardQueuedAnnotationsAndPreserveCollapsedFolds() throws Exception {
		final var document = firstViewer.getDocument();
		document.set("preamble\nroot\n    child\nend\n");
		firstStrategy.initialReconcile();
		assertFoldingRegions(firstViewer, "root\n    child\n");
		final var annotations = firstViewer.getProjectionAnnotationModel();
		final var fold = (ProjectionAnnotation) annotations.getAnnotationIterator().next();
		annotations.collapse(fold);
		try (var worker = Executors.newSingleThreadExecutor()) {
			worker.submit(firstStrategy::initialReconcile).get(5, TimeUnit.SECONDS);
			// Insert above the fold after scanning, but before publishing. Existing positions move with the edit;
			// queued line numbers do not, so applying them would replace the collapsed fold with the wrong range.
			document.replace(0, 0, "header\n");
			assertFoldingRegions(firstViewer, "root\n    child\n");
			assertThat(annotations.getAnnotationIterator().next()).isSameAs(fold);
			assertThat(fold.isCollapsed()).isTrue();

			worker.submit(firstStrategy::initialReconcile).get(5, TimeUnit.SECONDS);
			assertFoldingRegions(firstViewer, "root\n    child\n");
			assertThat(annotations.getAnnotationIterator().next()).isSameAs(fold);
			assertThat(fold.isCollapsed()).isTrue();
		}
	}

	@Test
	void editsToPreviousInputDoNotDiscardCurrentAnnotations() throws Exception {
		final var previousDocument = firstViewer.getDocument();
		final var document = new Document("replacement\n    child\nend\n");
		firstViewer.setDocument(document, new AnnotationModel());
		firstViewer.enableProjection();
		firstStrategy.setDocument(document);
		try (var worker = Executors.newSingleThreadExecutor()) {
			worker.submit(firstStrategy::initialReconcile).get(5, TimeUnit.SECONDS);
		}
		// The old input may remain open elsewhere, but its edits must no longer invalidate this editor's scans.
		previousDocument.replace(0, 0, "header\n");
		assertFoldingRegions(firstViewer, "replacement\n    child\n");
	}

	@Test
	void togglingProjectionDiscardsTheRunningScan() throws Exception {
		assertLifecycleChangeDoesNotWaitForScan(false);
	}

	@Test
	void uninstallDoesNotWaitOrAllowLateAnnotations() throws Exception {
		assertLifecycleChangeDoesNotWaitForScan(true);
	}

	@Test
	void uninstallDiscardsQueuedAnnotations() throws Exception {
		final var annotations = firstViewer.getProjectionAnnotationModel();
		try (var worker = Executors.newSingleThreadExecutor()) {
			worker.submit(firstStrategy::initialReconcile).get(5, TimeUnit.SECONDS);
		}
		// The scan has finished, but its UI callback must still respect a later uninstall.
		firstStrategy.uninstall();
		dispatchUiUpdates();
		assertThat(annotations.getAnnotationIterator().hasNext()).as("No queued annotations after uninstall").isFalse();
	}

	private void assertLifecycleChangeDoesNotWaitForScan(final boolean uninstall) throws Exception {
		final var document = new PausingDocument();
		firstViewer.setDocument(document, new AnnotationModel());
		firstViewer.enableProjection();
		firstStrategy.setDocument(document);
		final var annotations = firstViewer.getProjectionAnnotationModel();
		final var uiReturned = new CountDownLatch(1);
		try (var workers = Executors.newFixedThreadPool(2)) {
			document.pauseNextScan = true;
			final var scan = workers.submit(firstStrategy::initialReconcile);
			try {
				assertThat(document.scanStarted.await(5, TimeUnit.SECONDS)).as("Background scan started").isTrue();
				// Release a blocked implementation after the deadline, so a failing test cannot hang SWT.
				final var watchdog = workers.submit(() -> {
					if (!uiReturned.await(2, TimeUnit.SECONDS))
						document.releaseScan.countDown();
					return null;
				});
				if (uninstall) {
					firstStrategy.uninstall();
				} else {
					// The document stays the same, so document identity alone cannot invalidate this scan.
					firstViewer.disableProjection();
					firstViewer.enableProjection();
				}
				uiReturned.countDown();
				assertThat(document.releaseScan.getCount()).as("UI callback returned while the scan was paused").isEqualTo(1);
				watchdog.get(5, TimeUnit.SECONDS);
			} finally {
				uiReturned.countDown();
				document.releaseScan.countDown();
			}
			scan.get(5, TimeUnit.SECONDS);
			if (uninstall) {
				dispatchUiUpdates();
				assertThat(annotations.getAnnotationIterator().hasNext()).as("No annotations after uninstall").isFalse();
			} else {
				assertFoldingRegions(firstViewer);
				// A fresh scan can publish after folding has been enabled again.
				workers.submit(firstStrategy::initialReconcile).get(5, TimeUnit.SECONDS);
				assertFoldingRegions(firstViewer, "root\n    child\n");
			}
		}
	}

	private static void dispatchUiUpdates() {
		// Completed scans queue their annotation comparison and update on the UI thread.
		while (UI.getDisplay().readAndDispatch()) {
		}
	}

	private static void assertFoldingRegions(final ProjectionViewer viewer, final String... expected) throws BadLocationException {
		dispatchUiUpdates();
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
