/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.tests.internal.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.tm4e.ui.internal.text.TMPartitioner;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.tests.support.TestUtils;
import org.eclipse.tm4e.ui.text.TMPartitions;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies that TMDocumentSetupParticipant auto-installs the TM partitioner for documents with a resolvable grammar.
 */
class TMPartitioningDocumentSetupParticipantTest {

	private IEditorDescriptor genericEditorDescr;
	private IEditorPart editor;

	@BeforeEach
	void setup() throws Exception {
		genericEditorDescr = TestUtils.assertHasGenericEditor();
		TestUtils.assertNoTM4EThreadsRunning();
	}

	@AfterEach
	void tearDown() throws Exception {
		TestUtils.closeEditor(editor);
		editor = null;
		TestUtils.assertNoTM4EThreadsRunning();
	}

	@Test
	void participantInstallsPartitioner() throws Exception {
		// Use a temporary file outside workspace; participants should also engage via file store buffers
		final File f = TestUtils.createTempFile(".ts");
		try (var out = new FileOutputStream(f)) {
			out.write("let a = 1;".getBytes());
		}

		editor = IDE.openEditor(UI.getActivePage(), f.toURI(), genericEditorDescr.getId(), true);

		final var textEditor = (ITextEditor) editor;
		final IDocument doc = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
		assertThat(doc).isInstanceOf(IDocumentExtension3.class);

		final var ext3 = (IDocumentExtension3) doc;
		TestUtils.waitForAndAssertCondition(3_000, () -> ext3.getDocumentPartitioner(TMPartitions.TM_PARTITIONING) != null);

		final var part = ext3.getDocumentPartitioner(TMPartitions.TM_PARTITIONING);
		assertThat(part).as("TM partitioner is installed").isNotNull();

		// Ensure the TM model has completed at least one tokenization pass before asserting
		TestUtils.waitForModelReady(doc, 5_000);

		final ITypedRegion r = part.getPartition(0);
		assertThat(r.getType()).isEqualTo(TMPartitioner.scopeToPartitionType("source.ts"));
	}
}
