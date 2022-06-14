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

/**
 * Describes what to do when pressing Enter.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/d79132281222cdab77abeacca1af700e34c2f30b/src/vs/editor/common/languages/languageConfiguration.ts#L232">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L232</a>
 */
public class EnterAction {

	public enum IndentAction {
		/**
		 * Insert new line and copy the previous line's indentation.
		 */
		None,
		/**
		 * Insert new line and indent once (relative to the previous line's
		 * indentation).
		 */
		Indent,
		/**
		 * Insert two new lines: - the first one indented which will hold the cursor -
		 * the second one at the same indentation level
		 */
		IndentOutdent,
		/**
		 * Insert new line and outdent once (relative to the previous line's
		 * indentation).
		 */
		Outdent;
	}

	/**
	 * Describe what to do with the indentation.
	 */
	public final IndentAction indentAction;

	/**
	 * Describes text to be appended after the new line and after the indentation.
	 */
	@Nullable
	public String appendText;

	/**
	 * Describes the number of characters to remove from the new line's indentation.
	 */
	@Nullable
	public Integer removeText;

	public EnterAction(final IndentAction indentAction) {
		this.indentAction = indentAction;
	}

	/**
	 * @param appendText the appendText to set
	 */
	EnterAction withAppendText(@Nullable final String appendText) {
		this.appendText = appendText;
		return this;
	}

	/**
	 * @param removeText the removeText to set
	 */
	EnterAction withRemoveText(@Nullable final Integer removeText) {
		this.removeText = removeText;
		return this;
	}
}
