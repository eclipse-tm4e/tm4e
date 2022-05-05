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
package org.eclipse.tm4e.languageconfiguration.internal.supports;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Describes what to do when pressing Enter.
 */
public final class EnterAction {

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
	private final IndentAction indentAction;

	/**
	 * Describe whether to outdent current line.
	 */
	@Nullable
	private Boolean outdentCurrentLine;

	/**
	 * Describes text to be appended after the new line and after the indentation.
	 */
	@Nullable
	private String appendText;

	/**
	 * Describes the number of characters to remove from the new line's indentation.
	 */
	@Nullable
	private Integer removeText;

	public EnterAction(final IndentAction indentAction) {
		this.indentAction = indentAction;
	}

	public IndentAction getIndentAction() {
		return indentAction;
	}

	/**
	 * @return the outdentCurrentLine
	 */
	@Nullable
	private Boolean getOutdentCurrentLine() {
		return outdentCurrentLine;
	}

	/**
	 * @param outdentCurrentLine
	 *            the outdentCurrentLine to set
	 * @return
	 */
	private EnterAction setOutdentCurrentLine(final Boolean outdentCurrentLine) {
		this.outdentCurrentLine = outdentCurrentLine;
		return this;
	}

	/**
	 * @return the appendText
	 */
	@Nullable
	public String getAppendText() {
		return appendText;
	}

	/**
	 * @param appendText the appendText to set
	 */
	public EnterAction setAppendText(@Nullable final String appendText) {
		this.appendText = appendText;
		return this;
	}

	/**
	 * @return the removeText
	 */
	@Nullable
	public Integer getRemoveText() {
		return removeText;
	}

	/**
	 * @param removeText the removeText to set
	 */
	public EnterAction setRemoveText(@Nullable final Integer removeText) {
		this.removeText = removeText;
		return this;
	}

}
