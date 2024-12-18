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
package org.eclipse.tm4e.core.theme.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.theme.css.CSSConditionFactory;
import org.eclipse.tm4e.core.internal.theme.css.CSSDocumentHandler;
import org.eclipse.tm4e.core.internal.theme.css.CSSSelectorFactory;
import org.eclipse.tm4e.core.internal.theme.css.CSSStyle;
import org.eclipse.tm4e.core.internal.theme.css.ExtendedSelector;
import org.eclipse.tm4e.core.internal.theme.css.sac.SACParserFactory;
import org.eclipse.tm4e.core.theme.IStyle;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;
import org.w3c.css.sac.SelectorList;

/**
 * CSS Parser to parse style for TextMate syntax coloring.
 */
public class CSSParser {

	private static InputSource toSource(final InputStream source) {
		final var in = new InputSource();
		in.setByteStream(source);
		return in;
	}

	protected final CSSDocumentHandler handler = new CSSDocumentHandler();

	protected CSSParser() {
	}

	public CSSParser(final InputStream source) throws Exception {
		this(toSource(source));
	}

	public CSSParser(final InputSource source) throws Exception {
		this(source, new SACParserFactory().makeParser());
	}

	public CSSParser(final String source) throws Exception {
		this(new InputSource(new StringReader(source)));
	}

	public CSSParser(final InputSource source, final Parser parser) throws CSSException, IOException {
		parser.setDocumentHandler(handler);
		parser.setConditionFactory(CSSConditionFactory.INSTANCE);
		parser.setSelectorFactory(CSSSelectorFactory.INSTANCE);
		parser.parseStyleSheet(source);
	}

	public @Nullable IStyle getBestStyle(final String... cssClassNames) {
		int bestSpecificity = 0;
		IStyle bestStyle = null;
		for (final IStyle style : handler.getStyles()) {
			final SelectorList list = ((CSSStyle) style).selectorList;
			for (int i = 0, l = list.getLength(); i < l; i++) {
				final var selector = (ExtendedSelector) list.item(i);

				final int nbMatch = selector.nbMatch(cssClassNames);
				if (nbMatch < 1)
					continue;

				if ((nbMatch >= bestSpecificity || bestStyle == null)
						&& nbMatch == selector.nbClass()) {
					bestStyle = style;
					bestSpecificity = nbMatch;
				}
			}
		}
		return bestStyle;
	}

	public List<IStyle> getStyles() {
		return handler.getStyles();
	}
}
