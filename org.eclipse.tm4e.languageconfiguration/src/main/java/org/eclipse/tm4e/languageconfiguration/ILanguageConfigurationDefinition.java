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

import org.eclipse.core.runtime.content.IContentType;
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
	ILanguageConfiguration getLanguageConfiguration();

	/**
	 * Returns whether on-enter actions are enabled for this language configuration
	 * content type pair
	 *
	 * @return <code>true</code> if on enter is enabled, <code>false</code>
	 *         otherwise
	 */
	Boolean isOnEnterEnabled();

	/**
	 * Set whether on-enter actions are enabled for this language configuration
	 * content type pair
	 */
	void setOnEnterEnabled(boolean onEnterEnabled);

	/**
	 * Returns whether the bracket auto closing action is enabled for this language
	 * configuration content type pair
	 *
	 * @return <code>true</code> if bracket auto closing is enabled,
	 *         <code>false</code> otherwise
	 */
	Boolean isBracketAutoClosingEnabled();

	/**
	 * Set whether the bracket auto closing action is enabled for this language
	 * configuration content type pair
	 */
	void setBracketAutoClosingEnabled(boolean bracketAutoClosingEnabled);

	/**
	 * Returns whether the highlighting of matching pairs is enabled for this
	 * language configuration content type pair
	 *
	 * @return <code>true</code> if highlighting of matching pairs is enabled,
	 *         <code>false</code> otherwise
	 */
	Boolean isMatchingPairsEnabled();

	/**
	 * Set whether the highlighting of matching pairs is enabled for this language
	 * configuration content type pair
	 */
	void setMatchingPairsEnabled(boolean matchingPairsEnabled);

}
