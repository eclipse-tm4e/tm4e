/*********************************************************************
* Copyright (c) 2018 Red Hat Inc., and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStreamReader;

import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfiguration;
import org.junit.jupiter.api.Test;

public class ParsingTest {

	@Test
	public void testCanLoadRustLanguageConfig() throws Exception {
		LanguageConfiguration languageConfiguration = LanguageConfiguration.load(new InputStreamReader(getClass().getResourceAsStream("/rust-language-configuration.json")));
		assertNotNull(languageConfiguration);
	}

}
