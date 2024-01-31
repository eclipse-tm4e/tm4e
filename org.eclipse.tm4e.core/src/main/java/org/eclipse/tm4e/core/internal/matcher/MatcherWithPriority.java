/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode-textmate/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Angelo Zerr <angelo.zerr@gmail.com> - translation and adaptation to Java
 */
package org.eclipse.tm4e.core.internal.matcher;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/09effd8b7429b71010e0fa34ea2e16e622692946/src/matcher.ts#L5">
 *      github.com/microsoft/vscode-textmate/blob/main/src/matcher.ts</a>
 */
public final class MatcherWithPriority<T> {

	public final Matcher<T> matcher;

	/** -1 | 0 | 1 */
	public final int priority;

	MatcherWithPriority(final Matcher<T> matcher, final int priority) {
		this.matcher = matcher;
		this.priority = priority;
	}
}
