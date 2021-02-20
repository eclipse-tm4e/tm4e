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
package org.eclipse.tm4e.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegistryTest {

	@Test
	public void testGrammarRegistered() {
		IContentType contentType = Platform.getContentTypeManager().getContentType("org.eclipse.tm4e.ui.tests.testContentType");
		IGrammar grammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(new IContentType[] { contentType });
		Assertions.assertNotNull(grammar);
	}
}
