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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class OnigScannerTest {
	@Test
	void testOnigScanner() {
		var scanner = new OnigScanner(Arrays.asList("c", "a(b)?"));
		OnigScannerMatch result = scanner.findNextMatch(OnigString.of("abc"), 0);
		assert result != null;
		assertThat(result.index).isEqualTo(1);
		assertThat(result.getCaptureIndices())
				.containsExactly(
						new OnigCaptureIndex(0, 2),
						new OnigCaptureIndex(1, 2));

		scanner = new OnigScanner(Arrays.asList("a([b-d])c"));
		result = scanner.findNextMatch(OnigString.of("!abcdef"), 0);
		assert result != null;
		assertThat(result.index).isEqualTo(0);
		assertThat(result.getCaptureIndices())
				.containsExactly(
						new OnigCaptureIndex(1, 4),
						new OnigCaptureIndex(2, 3));
	}
}
