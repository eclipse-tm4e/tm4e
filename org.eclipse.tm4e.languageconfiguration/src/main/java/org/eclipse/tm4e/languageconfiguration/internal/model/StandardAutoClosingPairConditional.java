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
package org.eclipse.tm4e.languageconfiguration.internal.model;

import java.util.List;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/d79132281222cdab77abeacca1af700e34c2f30b/src/vs/editor/common/languages/languageConfiguration.ts#L201">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L201</a>
 */
public final class StandardAutoClosingPairConditional extends CharacterPair {

	public final List<String> notIn;

	public StandardAutoClosingPairConditional(final String open, final String close, final List<String> notIn) {
		super(open, close);
		this.notIn = notIn;
	}
}
