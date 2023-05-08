/*******************************************************************************
 * Copyright (c) 2023 Vegard IT GmbH and others.
 * This program and the Faccompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.tm4e.ui.internal.utils.MarkerConfig;
import org.junit.jupiter.api.Test;

class PreferenceHelperTest {

	@Test
	void testMarkerConfigsSerialization() {
		final var defaults = MarkerConfig.getDefaults();
		final var defaultsAsJson = PreferenceHelper.toJsonMarkerConfigs(defaults);
		assertEquals(defaults, PreferenceHelper.loadMarkerConfigs(defaultsAsJson));
	}
}
