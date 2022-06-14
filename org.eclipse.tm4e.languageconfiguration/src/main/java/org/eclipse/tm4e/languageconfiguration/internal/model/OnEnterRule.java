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

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.utils.RegExpUtils;

/**
 * Describes a rule to be evaluated when pressing Enter.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/d79132281222cdab77abeacca1af700e34c2f30b/src/vs/editor/common/languages/languageConfiguration.ts#L157">
 *      github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L157</a>
 */
public final class OnEnterRule {

	/**
	 * This rule will only execute if the text before the cursor matches this regular expression.
	 */
	@Nullable
	public final Pattern beforeText;

	/**
	 * This rule will only execute if the text after the cursor matches this regular expression.
	 */
	@Nullable
	public final Pattern afterText;

	/**
	 * The action to execute.
	 */
	public final EnterAction action;

	public OnEnterRule(final String beforeText, @Nullable final String afterText, final EnterAction action) {
		this.beforeText = RegExpUtils.create(beforeText);
		this.afterText = afterText != null ? RegExpUtils.create(afterText) : null;
		this.action = action;
	}
}
