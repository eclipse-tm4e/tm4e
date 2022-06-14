/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/e568a31f82680cde0949d7e07dac913565134c93/src/vs/editor/common/languages/languageConfiguration.ts#L28">
 *      https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/languageConfiguration.ts#L28</a>
 */
public interface ILanguageConfiguration {

	/**
	 * Returns the language's comments. The comments are used by {@link StandardAutoClosingPairConditional} when
	 * <code>notIn</code> contains <code>comment</code>
	 *
	 * @return the language's comments or <code>null</code> if not set
	 */
	@Nullable
	CommentRule getComments();

	/**
	 * Returns the language's brackets. This configuration implicitly affects pressing Enter around these brackets.
	 *
	 * @return the language's brackets. This configuration implicitly affects pressing Enter around these brackets.
	 */
	@Nullable
	List<CharacterPair> getBrackets();

	/**
	 * Returns the language's auto closing pairs. The 'close' character is automatically inserted with the 'open'
	 * character is typed. If not set, the configured brackets will be used.
	 *
	 * @return the language's auto closing pairs. The 'close' character is
	 *         automatically inserted with the 'open' character is typed. If not
	 *         set, the configured brackets will be used.
	 */
	@Nullable
	List<StandardAutoClosingPairConditional> getAutoClosingPairs();

	/**
	 * Returns the language's rules to be evaluated when pressing Enter.
	 *
	 * @return the language's rules to be evaluated when pressing Enter.
	 */
	@Nullable
	List<OnEnterRule> getOnEnterRules();

	/**
	 * Returns the language's surrounding pairs. When the 'open' character is typed on a selection, the selected string
	 * is surrounded by the open and close characters. If not set, the autoclosing pairs settings will be used.
	 *
	 * @return the language's surrounding pairs. When the 'open' character is typed on a selection, the selected string
	 *         is surrounded by the open and close characters. If not set, the autoclosing pairs settings will be used.
	 */
	@Nullable
	List<CharacterPair> getSurroundingPairs();

	/**
	 * Returns the language's folding rules.
	 *
	 * @return the language's folding or <code>null</code> if not set
	 */
	@Nullable
	FoldingRule getFolding();

	/**
	 * Returns the language's definition of a word. This is the regex used when referring to a word.
	 *
	 * @return the language's word pattern or <code>null</code> if not set
	 */
	@Nullable
	String getWordPattern();
}
