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
package org.eclipse.tm4e.core.internal.grammar.tokenattrs;

/**
 * Standard TextMate token type.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode-textmate/blob/76ab07aecfbd7e959ee4b55de3976f7a3ee95f38/src/encodedTokenAttributes.ts#L159">
 *      github.com/microsoft/vscode-textmate/blob/main/src/encodedTokenAttributes.ts</a>
 */
final class StandardTokenType {

	static final int Other = 0;
	static final int Comment = 1;
	static final int String = 2;
	static final int RegEx = 3;

	/**
	 * Content should be accessed statically
	 */
	private StandardTokenType() {
	}
}
