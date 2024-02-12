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
 * Jochen Ulrich <jochenulrich@t-online.de> - exception messages
 */
package org.eclipse.tm4e.core.internal.theme.css.util;

import static org.w3c.css.sac.CSSException.SAC_NOT_SUPPORTED_ERR;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CharacterDataSelector;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.NegativeSelector;
import org.w3c.css.sac.ProcessingInstructionSelector;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorFactory;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;

public abstract class AbstractSelectorFactory implements SelectorFactory {

	@Override
	public SimpleSelector createAnyNodeSelector() throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Any node selector is not supported", null);
	}

	@Override
	public CharacterDataSelector createCDataSectionSelector(final String arg0) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "CDATA section is not supported", null);
	}

	@Override
	public DescendantSelector createChildSelector(final Selector arg0, final SimpleSelector arg1) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Child selector is not supported", null);
	}

	@Override
	public CharacterDataSelector createCommentSelector(final String arg0) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Comment is not supported", null);
	}

	@Override
	public ConditionalSelector createConditionalSelector(final SimpleSelector selector, final Condition condition) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Descendant selector is not supported", null);
	}

	@Override
	public DescendantSelector createDescendantSelector(final Selector arg0, final SimpleSelector arg1) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Descendant selector is not supported", null);
	}

	@Override
	public SiblingSelector createDirectAdjacentSelector(final short arg0, final Selector arg1, final SimpleSelector arg2)
			throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Direct adjacent selector is not supported", null);
	}

	@Override
	public ElementSelector createElementSelector(final String uri, final String name) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Element selector is not supported", null);
	}

	@Override
	public NegativeSelector createNegativeSelector(final SimpleSelector arg0) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Negative selector is not supported", null);
	}

	@Override
	public ProcessingInstructionSelector createProcessingInstructionSelector(final String arg0, final String arg1) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Processing instruction is not supported", null);
	}

	@Override
	public ElementSelector createPseudoElementSelector(final String arg0, final String arg1) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Pseudo element selector is not supported", null);
	}

	@Override
	public SimpleSelector createRootNodeSelector() throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Root node selector is not supported", null);
	}

	@Override
	public CharacterDataSelector createTextNodeSelector(final String arg0) throws CSSException {
		throw new CSSException(SAC_NOT_SUPPORTED_ERR, "Text node selector is not supported", null);
	}
}
