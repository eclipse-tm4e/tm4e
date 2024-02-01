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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.utils.StringUtils;

/**
 * Describes what to do when pressing Enter.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/languageConfiguration.ts#L232">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L232</a>
 */
public class EnterAction {

	public enum IndentAction {
		/** Insert new line and copy the previous line's indentation. */
		None,

		/** Insert new line and indent once (relative to the previous line's indentation). */
		Indent,

		/** Insert two new lines: the first one indented which will hold the cursor; the second one at the same indentation level */
		IndentOutdent,

		/** Insert new line and outdent once (relative to the previous line's indentation). */
		Outdent;

		public static IndentAction get(final @Nullable String value) {
			// see
			// https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/workbench/contrib/codeEditor/browser/languageConfigurationExtensionPoint.ts#L336
			if (value == null) {
				return IndentAction.None;
			}
			return switch (value) {
				case "none" -> IndentAction.None;
				case "indent" -> IndentAction.Indent;
				case "indentOutdent" -> IndentAction.IndentOutdent;
				case "outdent" -> IndentAction.Outdent;
				default -> IndentAction.None;
			};
		}
	}

	/** Describe what to do with the indentation. */
	public final IndentAction indentAction;

	/** Describes text to be appended after the new line and after the indentation. */
	public final @Nullable String appendText;

	/** Describes the number of characters to remove from the new line's indentation. */
	public final @Nullable Integer removeText;

	public EnterAction(final IndentAction indentAction) {
		this(indentAction, null, null);
	}

	public EnterAction(final IndentAction indentAction, final @Nullable String appendText, final @Nullable Integer removeText) {
		this.indentAction = indentAction;
		this.appendText = appendText;
		this.removeText = removeText;
	}

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> sb
				.append("indentAction=").append(indentAction).append(", ")
				.append("appendText=").append(appendText).append(", ")
				.append("removeText=").append(removeText));
	}
}
