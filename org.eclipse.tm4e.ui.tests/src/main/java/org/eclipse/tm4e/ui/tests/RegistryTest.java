/**
 * Copyright (c) 2015-2021 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.junit.jupiter.api.Test;

class RegistryTest {

	@Test
	void testGrammarRegistered() {
		final IContentType contentType = Platform.getContentTypeManager().getContentType("org.eclipse.tm4e.ui.tests.testContentType");
		final IGrammar grammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(contentType);
		assertThat(grammar).isNotNull();
	}

	@Test
	void testThemeAppliesToSubtypes() {
		final IContentType contentType = Platform.getContentTypeManager().getContentType("org.eclipse.tm4e.ui.tests.testContentType.child");
		final IGrammar grammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(contentType);
		assertThat(grammar).isNotNull();
	}
}
