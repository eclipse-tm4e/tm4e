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
import org.eclipse.tm4e.languageconfiguration.internal.utils.Indentation;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/cursorCommon.ts#L50">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/cursorCommon.ts</a>
 */
public class CursorConfiguration {

	/**
	 * Specifies if the text editor uses spaces for tabs.
	 *
	 * @see AbstractDecoratedTextEditorPreferenceConstants#EDITOR_SPACES_FOR_TABS
	 */
	public final boolean insertSpaces;

	/**
	 * The number of spaces to insert on indentation when {@link #insertSpaces} is true.
	 *
	 * @see AbstractDecoratedTextEditorPreferenceConstants#EDITOR_TAB_WIDTH
	 */
	public final int indentSize;

	public CursorConfiguration(final boolean insertSpaces, final int indentSize) {
		this.insertSpaces = insertSpaces;
		this.indentSize = Math.max(1, indentSize);
	}

	public String normalizeIndentation(final String str) {
		return Indentation.normalizeIndentation(str, this.indentSize, this.insertSpaces);
	}

	public String outdentString(final String str) {
		if (str.startsWith("\t"))
			return str.substring(1);

		if (insertSpaces) {
			final var indent = getIndent();
			if (str.startsWith(indent))
				return str.substring(indent.length());
		}
		return str;
	}

	public String getIndent() {
		return insertSpaces
				? " ".repeat(indentSize)
				: "\t";
	}

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> sb
				.append("insertSpaces=").append(insertSpaces).append(", ")
				.append("indentSize=").append(indentSize));
	}
}
