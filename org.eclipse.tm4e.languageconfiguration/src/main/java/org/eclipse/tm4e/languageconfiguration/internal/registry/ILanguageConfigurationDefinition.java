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
package org.eclipse.tm4e.languageconfiguration.internal.registry;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;
import org.eclipse.tm4e.registry.ITMResource;

public interface ILanguageConfigurationDefinition extends ITMResource {

	/**
	 * Returns the content type of the language configuration.
	 *
	 * @return the content type of the language configuration.
	 */
	IContentType getContentType();

	/**
	 * Returns the language configuration.
	 *
	 * @return the language configuration.
	 */
	@Nullable
	LanguageConfiguration getLanguageConfiguration();

	/**
	 * Returns whether on-enter actions are enabled for this language configuration content type pair
	 *
	 * @return <code>true</code> if on enter is enabled, <code>false</code> otherwise
	 */
	boolean isOnEnterEnabled();

	/**
	 * Set whether on-enter actions are enabled for this language configuration content type pair
	 */
	void setOnEnterEnabled(boolean onEnterEnabled);

	/**
	 * @return <code>true</code> if indent rules are enabled, <code>false</code> otherwise
	 */
	boolean isIndentRulesEnabled();

	/**
	 * Set whether indent-rules are enabled for this language configuration content type pair
	 */
	void setIndentRulesEnabled(boolean indentRulesEnabled);

	/**
	 * Returns whether the bracket auto closing action is enabled for this language configuration content type pair
	 *
	 * @return <code>true</code> if bracket auto closing is enabled, <code>false</code> otherwise
	 */
	boolean isBracketAutoClosingEnabled();

	/**
	 * Set whether the bracket auto closing action is enabled for this language configuration content type pair
	 */
	void setBracketAutoClosingEnabled(boolean bracketAutoClosingEnabled);

	/**
	 * Returns whether the highlighting of matching pairs is enabled for this language configuration content type pair
	 *
	 * @return <code>true</code> if highlighting of matching pairs is enabled, <code>false</code> otherwise
	 */
	boolean isMatchingPairsEnabled();

	/**
	 * Set whether the highlighting of matching pairs is enabled for this language configuration content type pair
	 */
	void setMatchingPairsEnabled(boolean matchingPairsEnabled);
}
