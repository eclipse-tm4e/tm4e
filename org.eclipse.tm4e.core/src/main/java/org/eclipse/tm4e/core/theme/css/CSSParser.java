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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.theme.css.CSSStyle;
import org.eclipse.tm4e.core.internal.theme.css.CSSStyleSheetParser;
import org.eclipse.tm4e.core.theme.IStyle;

/**
 * CSS Parser to parse style for TextMate syntax coloring.
 */
public class CSSParser {

	private final List<IStyle> styles;

	protected CSSParser() {
		styles = Collections.emptyList();
	}

	public CSSParser(final InputStream source) throws IOException {
		this(new String(source.readAllBytes(), UTF_8));
	}

	public CSSParser(final String source) {
		styles = new ArrayList<>(CSSStyleSheetParser.parse(source));
	}

	public @Nullable IStyle getBestStyle(final String... cssClassNames) {
		final List<String> names = Arrays.asList(cssClassNames);
		int bestSpecificity = 0;
		IStyle bestStyle = null;
		for (final IStyle style : styles) {
			final int specificity = ((CSSStyle) style).getSpecificity(names);
			if (specificity > 0 && specificity >= bestSpecificity) {
				bestStyle = style;
				bestSpecificity = specificity;
			}
		}
		return bestStyle;
	}

	public List<IStyle> getStyles() {
		return styles;
	}
}
