/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration;

import java.util.List;

import org.eclipse.tm4e.languageconfiguration.internal.supports.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.Comments;
import org.eclipse.tm4e.languageconfiguration.internal.supports.Folding;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterRule;

public interface ILanguageConfiguration {
	/**
	 * Returns the language's comments. The comments are used by
	 * {@link AutoClosingPairConditional} when <code>notIn</code> contains
	 * <code>comment</code>
	 *
	 * @return the language's comments or <code>null</code> if not set
	 */
	Comments getComments();

	/**
	 * Returns the language's brackets. This configuration implicitly affects
	 * pressing Enter around these brackets.
	 *
	 * @return the language's brackets. This configuration implicitly affects
	 *         pressing Enter around these brackets.
	 */
	public List<CharacterPair> getBrackets();

	/**
	 * Returns the language's auto closing pairs. The 'close' character is
	 * automatically inserted with the 'open' character is typed. If not set, the
	 * configured brackets will be used.
	 *
	 * @return the language's auto closing pairs. The 'close' character is
	 *         autautomatically inserted with the 'open' character is typed. If not
	 *         set, the configured brackets will be used.
	 */
	public List<AutoClosingPairConditional> getAutoClosingPairs();

	/**
	 * Returns the language's rules to be evaluated when pressing Enter.
	 *
	 * @return the language's rules to be evaluated when pressing Enter.
	 */
	public List<OnEnterRule> getOnEnterRules();

	/**
	 * Returns the language's surrounding pairs. When the 'open' character is typed
	 * on a selection, the selected string is surrounded by the open and close
	 * characters. If not set, the autoclosing pairs settings will be used.
	 *
	 * @return the language's surrounding pairs. When the 'open' character is typed
	 *         on a selection, the selected string is surrounded by the open and
	 *         close characters. If not set, the autoclosing pairs settings will be
	 *         used.
	 */
	public List<CharacterPair> getSurroundingPairs();

	/**
	 * Returns the language's folding rules.
	 *
	 * @return the language's folding or <code>null</code> if not set
	 */
	Folding getFolding();

	/**
	 * Returns the language's definition of a word. This is the regex used when
	 * refering to a word.
	 *
	 * @return the language's word pattern or <code>null</code> if not set
	 */
	String getWordPattern();
}
