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
package org.eclipse.tm4e.core.internal.oniguruma;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class OnigScannerTest {

	@Test
	void testOnigScanner() {
		var scanner = new OnigScanner(Arrays.asList("c", "a(b)?"));
		OnigScannerMatch result = scanner.findNextMatch("abc", 0);
		assertNotNull(result);
		assertEquals(1, result.getIndex());
		assertArrayEquals(new OnigCaptureIndex[] {
			new OnigCaptureIndex(0, 2),
			new OnigCaptureIndex(1, 2) },
				result.getCaptureIndices());

		scanner = new OnigScanner(Arrays.asList("a([b-d])c"));
		result = scanner.findNextMatch("!abcdef", 0);
		assertNotNull(result);
		assertEquals(0, result.getIndex());
		assertArrayEquals(new OnigCaptureIndex[] {
			new OnigCaptureIndex(1, 4),
			new OnigCaptureIndex(2, 3) },
				result.getCaptureIndices());

	}
}
