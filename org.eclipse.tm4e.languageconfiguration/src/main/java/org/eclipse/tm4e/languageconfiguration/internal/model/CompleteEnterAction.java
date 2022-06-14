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

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/d79132281222cdab77abeacca1af700e34c2f30b/src/vs/editor/common/languages/languageConfiguration.ts#L250">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L250</a>
 */
public final class CompleteEnterAction extends EnterAction {

	/**
	 * The line's indentation minus removeText
	 */
	public final String indentation;

	public CompleteEnterAction(final EnterAction action, final String indentation) {
		super(action.indentAction);
		this.indentation = indentation;
		this.appendText = action.appendText;
		this.removeText = action.removeText;
	}
}
