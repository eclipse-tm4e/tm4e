/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial code from https://github.com/microsoft/vscode/
 * Initial copyright Copyright (C) Microsoft Corporation. All rights reserved.
 * Initial license: MIT
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;

/**
 * Provides tokenization related functionality of the text model.
 *
 * @see <a href=
 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/tokenizationTextModelPart.ts#L15">
 *      github.com/microsoft/vscode/main/src/vs/editor/common/tokenizationTextModelPart.ts
 *      <code>#ITokenizationTextModelPart</code></a>
 */
public interface ITMModel extends ModelTokensChangedEvent.Listenable {

	/**
	 * @param lineIndex 0-based
	 *
	 * @return <code>null</code> if line does not exist or has not yet been tokenized.
	 *
	 */
	@Nullable
	List<TMToken> getLineTokens(int lineIndex);

	BackgroundTokenizationState getBackgroundTokenizationState();

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/tokenizationTextModelPart.ts#L93">
	 *      github.com/microsoft/vscode/main/src/vs/editor/common/tokenizationTextModelPart.ts<code>#BackgroundTokenizationState</code></a>
	 */
	enum BackgroundTokenizationState {
		IN_PROGRESS,
		COMPLETED
	}

	// -----------------------------------------------------
	// methods below are TM4E specific and not from upstream
	// -----------------------------------------------------

	/**
	 * stops the async tokenizer thread
	 */
	void dispose();

	/**
	 * @param lineIndex 0-based
	 *
	 * @throws Exception if line does not exist in the underlying document
	 */
	String getLineText(int lineIndex) throws Exception;

	/**
	 * @return the grammar to use to parse the lines of the document
	 */
	@Nullable
	IGrammar getGrammar();

	/**
	 * Sets the grammar to use to parse the lines of the document.
	 */
	void setGrammar(IGrammar grammar);

	int getNumberOfLines();
}
