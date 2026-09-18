/**
 * Copyright (c) 2026 Vegard IT GmbH and others.
 * This program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.ui.tests;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IImportWizard;
import org.junit.jupiter.api.Test;

/** Verifies that Eclipse can create the contributed grammar import wizard. */
class GrammarImportLifecycleTest {

	@Test
	void standaloneImportWizardCanBeCreatedFromItsExtension() throws Exception {
		for (final var element : Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.importWizards")) {
			if ("org.eclipse.tm4e.ui.wizards.TextMateGrammarWizard".equals(element.getAttribute("id"))) {
				final var wizard = (IImportWizard) element.createExecutableExtension("class");
				wizard.dispose();
				return;
			}
		}
		throw new AssertionError("Grammar import wizard is not registered");
	}
}
