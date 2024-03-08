/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TestSurroundingPairs {

	@AfterEach
	public void tearDown() throws Exception {
		UI.getActivePage().closeAllEditors(false);
		for (final IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			p.delete(true, null);
		}
	}

	@Test
	public void testSurroundingPairs() throws Exception {
		final IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(null);
		p.open(null);
		final IFile file = p.getFile("test.lc-test");
		file.create(new ByteArrayInputStream(new byte[0]), true, null);

		final var contentType = file.getContentDescription().getContentType();
		final var langDef = Stream.of(LanguageConfigurationRegistryManager.getInstance().getDefinitions())
				.filter(e -> e.getContentType().equals(contentType))
				.findFirst()
				.get();

		final ITextEditor editor = (ITextEditor) IDE.openEditor(UI.getActivePage(), file);
		final StyledText text = (StyledText) editor.getAdapter(Control.class);

		// test with enabled surrounding pairs
		langDef.setMatchingPairsEnabled(true);

		text.setText("the mountain is high");
		text.setSelection(4, 12);
		assertEquals(12, text.getCaretOffset());
		assertEquals("mountain", text.getSelectionText());
		text.insert("(");
		assertEquals("the (mountain) is high", text.getText());
		assertEquals("mountain", text.getSelectionText());
		assertEquals(13, text.getCaretOffset());

		// test with disabled surrounding pairs
		langDef.setMatchingPairsEnabled(false);

		text.setText("the mountain is high");
		text.setSelection(4, 12);
		assertEquals(12, text.getCaretOffset());
		assertEquals("mountain", text.getSelectionText());
		text.insert("(");
		assertEquals("the ( is high", text.getText());

	}
}
