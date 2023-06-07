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
package org.eclipse.tm4e.core.internal.theme.css;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

@NonNullByDefault({})
public final class CSSSelectorFactory implements SelectorFactory {

	public static final SelectorFactory INSTANCE = new CSSSelectorFactory();

	@Override
	public SimpleSelector createAnyNodeSelector() throws CSSException {
		throw new UnsupportedOperationException("CSS any selector is not supported");
	}

	@Override
	public CharacterDataSelector createCDataSectionSelector(final String arg0) throws CSSException {
		throw new UnsupportedOperationException("CSS CDATA section is not supported");
	}

	@Override
	public DescendantSelector createChildSelector(final Selector arg0, final SimpleSelector arg1)
			throws CSSException {
		throw new UnsupportedOperationException("CSS child selector is not supported");
	}

	@Override
	public CharacterDataSelector createCommentSelector(final String arg0) throws CSSException {
		throw new UnsupportedOperationException("CSS comment is not supported");
	}

	@Override
	public ConditionalSelector createConditionalSelector(final SimpleSelector selector, final Condition condition)
			throws CSSException {
		return new CSSConditionalSelector((ExtendedSelector) selector, (ExtendedCondition) condition);
	}

	@Override
	public DescendantSelector createDescendantSelector(final Selector arg0,
			final SimpleSelector arg1) throws CSSException {
		throw new UnsupportedOperationException("CSS descendant selector is not supported");
	}

	@Override
	public SiblingSelector createDirectAdjacentSelector(final short arg0, final Selector arg1,
			final SimpleSelector arg2)
			throws CSSException {
		throw new UnsupportedOperationException("CSS direct adjacent selector is not supported");
	}

	@Override
	public ElementSelector createElementSelector(final String uri, final String name) throws CSSException {
		return new CSSElementSelector(uri, name);
	}

	@Override
	public NegativeSelector createNegativeSelector(final SimpleSelector arg0) throws CSSException {
		throw new UnsupportedOperationException("CSS negative selector is not supported");
	}

	@Override
	public ProcessingInstructionSelector createProcessingInstructionSelector(final String arg0,
			final String arg1)
			throws CSSException {
		throw new UnsupportedOperationException("CSS processing instruction is not supported");
	}

	@Override
	public ElementSelector createPseudoElementSelector(final String arg0, final String arg1)
			throws CSSException {
		throw new UnsupportedOperationException("CSS pseudo element selector is not supported");
	}

	@Override
	public SimpleSelector createRootNodeSelector() throws CSSException {
		throw new UnsupportedOperationException("CSS root node selector is not supported");
	}

	@Override
	public CharacterDataSelector createTextNodeSelector(final String arg0) throws CSSException {
		throw new UnsupportedOperationException("CSS text node selector is not supported");
	}

}
