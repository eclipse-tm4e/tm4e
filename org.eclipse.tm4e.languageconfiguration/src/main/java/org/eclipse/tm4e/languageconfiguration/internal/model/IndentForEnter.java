/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * - Microsoft Corporation: Initial code, written in TypeScript, licensed under MIT license
 * - Sebastian Thomschke - translation and adaptation to Java
 */
package org.eclipse.tm4e.languageconfiguration.internal.model;

import org.eclipse.tm4e.core.internal.utils.StringUtils;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/autoIndent.ts#L301">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/autoIndent.ts</a>
 */
public class IndentForEnter {

	/** existing indentation of the line in which enter was pressed */
	public final String beforeEnter;
	/** indentation of the newly inserted line */
	public final String afterEnter;

	public IndentForEnter(final String beforeEnter, final String afterEnter) {
		this.beforeEnter = beforeEnter;
		this.afterEnter = afterEnter;
	}

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> sb
				.append("beforeEnter=").append(beforeEnter).append(", ")
				.append("afterEnter=").append(afterEnter));
	}
}
