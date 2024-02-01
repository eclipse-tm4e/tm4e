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
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/languageConfiguration.ts#L250">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L250</a>
 */
public final class CompleteEnterAction {

	/** Describe what to do with the indentation. */
	public final IndentAction indentAction;

	/** Describes text to be appended after the new line and after the indentation. */
	public final String appendText;

	/** Describes the number of characters to remove from the new line's indentation. */
	public final @Nullable Integer removeText;

	/** The line's indentation minus removeText. */
	public final String indentation;

	public CompleteEnterAction(final IndentAction indentAction, final String appendText, final @Nullable Integer removeText,
			final String indentation) {
		this.indentAction = indentAction;
		this.appendText = appendText;
		this.removeText = removeText;
		this.indentation = indentation;
	}

	@Override
	public String toString() {
		return StringUtils.toString(this, sb -> sb
				.append("indentAction=").append(indentAction).append(", ")
				.append("appendText=").append(appendText).append(", ")
				.append("removeText=").append(removeText).append(", ")
				.append("indentation=").append(indentation));
	}
}
