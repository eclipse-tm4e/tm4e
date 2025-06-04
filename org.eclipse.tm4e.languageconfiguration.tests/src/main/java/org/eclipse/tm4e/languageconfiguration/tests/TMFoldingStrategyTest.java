/**
 * Copyright (c) 2025 Vegard IT GmbH and others.
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

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import org.assertj.core.util.Arrays;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TMFoldingStrategyTest {

	private static void assertAnnotations(final ProjectionViewer viewer, final String... expectedTextOfFoldableRegions) {
		final var annoModel = viewer.getProjectionAnnotationModel();
		final var notSeen = Arrays.asList(expectedTextOfFoldableRegions);
		TestUtils.waitForAndAssertCondition(5_000, () -> {
			for (final Iterator<Annotation> it = annoModel.getAnnotationIterator(); it.hasNext();) {
				final var anno = (ProjectionAnnotation) it.next();
				final var pos = annoModel.getPosition(anno);
				final var textOfRegion = viewer.getDocument().get(pos.offset, pos.length);
				notSeen.remove(textOfRegion);
			}
			assertThat(notSeen).as("Missing folding regions").isEmpty();
			return notSeen.isEmpty();
		});

	}
	private IProject project;
	private ITextEditor editor;

	@BeforeEach
	public void setup() throws Exception {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(getClass().getSimpleName() + System.currentTimeMillis());
		project.create(null);
		project.open(null);

	}

	@AfterEach
	public void teardown() throws Exception {
		if (editor != null) {
			UI.runSync(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, false));
		}
		if (project != null && project.exists()) {
			project.delete(true, null);
		}
	}

	@Test
	public void testIndentationBasedFolding() throws Exception {
		/*
		 * Create and open file with nested indentation
		 */
		final var file = project.getFile("recursive-indent.lc-test");
		final String initialContent = """
			a
			    b
			        c
			    d
			e
			""";
		file.create(new ByteArrayInputStream(initialContent.getBytes()), true, null);

		final IWorkbenchPage page = UI.getActivePage();
		editor = (ITextEditor) IDE.openEditor(page, file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		// Retrieve the ProjectionViewer created by GenericEditor via the adapter mechanism
		final var targetOp = editor.getAdapter(ITextOperationTarget.class);
		if (!(targetOp instanceof final ProjectionViewer viewer))
			throw new IllegalStateException("GenericEditor did not return a ProjectionViewer");

		// Wait until the reconciler has produced the initial annotations
		assertAnnotations(viewer, "    b\n        c\n",
				"a\n    b\n        c\n    d\n");

		/*
		 * Modify the document by adding a new indentation block under "e"
		 */
		final int insertOffset = document.getLength();
		final String addition = "    f\n";
		document.replace(insertOffset, 0, addition);

		// Assert that the new folding region for "e\n    f\n" was added alongside the original two
		assertAnnotations(viewer, "    b\n        c\n",
				"a\n    b\n        c\n    d\n",
				"e\n    f\n");
	}

	@Test
	public void testMarkerBasedFolding() throws Exception {
		/*
		 * Create and open file with foldable region
		 * IMPORTANT: The file extension "*.lc-test" must be bound to a language configuration with <code>#region … #endregion</code> markers
		 */
		final var file = project.getFile("folding.lc-test");
		final String initialContent = "#region\naaa\nbbb\n#endregion\n";
		file.create(new ByteArrayInputStream(initialContent.getBytes()), true, null);

		final IWorkbenchPage page = UI.getActivePage();
		editor = (ITextEditor) IDE.openEditor(page, file, "org.eclipse.ui.genericeditor.GenericEditor");
		final var document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		// Retrieve the ProjectionViewer created by GenericEditor via the adapter mechanism
		final var targetOp = editor.getAdapter(ITextOperationTarget.class);
		if (!(targetOp instanceof final ProjectionViewer viewer))
			throw new IllegalStateException("GenericEditor did not return a ProjectionViewer");

		// Wait until the reconciler has produced the first annotation
		assertAnnotations(viewer, initialContent);

		/*
		 * Add a second foldable region
		 */
		final int insertOffset = document.getLength();
		final String addition = "#region\nccc\nddd\n#endregion\n";
		document.replace(insertOffset, 0, addition);

		assertAnnotations(viewer, initialContent, addition);
	}
}
