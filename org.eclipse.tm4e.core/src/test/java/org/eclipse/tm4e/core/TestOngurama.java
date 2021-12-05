/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core;

import org.eclipse.tm4e.core.internal.oniguruma.IOnigNextMatchResult;
import org.eclipse.tm4e.core.internal.oniguruma.OnigScanner;

public class TestOngurama {

	public static void main(String[] args) {

		OnigScanner scanner = new OnigScanner(new String[] { "c", "a(b)?" });
		IOnigNextMatchResult result = scanner.findNextMatchSync("abc", 0);
		System.err.println(result);

		scanner = new OnigScanner(new String[] { "a([b-d])c" });
		result = scanner.findNextMatchSync("!abcdef", 0);
		System.err.println(result);
	}
}
