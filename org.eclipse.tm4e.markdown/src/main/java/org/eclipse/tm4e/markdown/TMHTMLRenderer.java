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
package org.eclipse.tm4e.markdown;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IStateStack;
import org.eclipse.tm4e.core.model.ITokenizationSupport;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.core.model.TMTokenizationSupport;
import org.eclipse.tm4e.markdown.marked.HTMLRenderer;
import org.eclipse.tm4e.markdown.marked.Helpers;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;

public class TMHTMLRenderer extends HTMLRenderer {

	private final String defaultLang;

	public TMHTMLRenderer(final String defaultLang) {
		this.defaultLang = defaultLang;
	}

	@Override
	public void code(final String code, final @Nullable String lang, final boolean escaped) {
		final IGrammar grammar = lang == null ? getDefaultGrammar() : getGrammar(lang);
		if (grammar == null) {
			super.code(code, lang, escaped);
		} else {
			final var tokenizationSupport = new TMTokenizationSupport(grammar);
			html.append("<div style=\"white-space: pre-wrap;\">");
			tokenizeLines(code, tokenizationSupport);
			html.append("</div>");
		}
	}

	private void tokenizeLines(final String text, final ITokenizationSupport tokenizationSupport) {
		final String[] lines = text.split("\r\n|\r|\n");
		@Nullable
		IStateStack currentState = tokenizationSupport.getInitialState();
		for (int i = 0; i < lines.length; i++) {
			currentState = tokenizeLine(lines[i], tokenizationSupport, currentState);

			// Keep new lines
			if (i < lines.length - 1) {
				emitNewLine();
			}
		}
	}

	private void emitNewLine() {
		html.append("<br/>");
	}

	private @Nullable IStateStack tokenizeLine(final String line, final ITokenizationSupport tokenizationSupport,
			final @Nullable IStateStack startState) {
		final var tokenized = tokenizationSupport.tokenize(line, startState);
		final var endState = tokenized.getEndState();
		final var tokens = tokenized.getTokens();
		int offset = 0;
		String tokenText;

		// For each token inject spans with proper class names based on token type
		for (int j = 0; j < tokens.size(); j++) {
			final TMToken token = tokens.get(j);

			// Tokens only provide a startIndex from where they are valid from.
			// As such, we need to look ahead the value of the token by advancing until the next
			// tokens start inex or the end of the line.
			if (j < tokens.size() - 1) {
				tokenText = line.substring(offset, tokens.get(j + 1).startIndex);
				offset = tokens.get(j + 1).startIndex;
			} else {
				tokenText = line.substring(offset);
			}

			StringBuilder className = new StringBuilder("token");
			final String safeType = token.type.replaceAll("[^a-z0-9\\-]", " ");
			if (!safeType.isEmpty()) {
				className.append(' ').append(safeType);
			}

			html.append("<span ");
			html.append("class=\"");
			html.append(className.toString());
			html.append("\">");
			html.append(Helpers.htmlEscape(tokenText));
			html.append("</span>");
			// emitToken(className, tokenText);
		}

		return endState;
	}

	protected @Nullable IGrammar getDefaultGrammar() {
		return getGrammar(defaultLang);
	}

	protected @Nullable IGrammar getGrammar(final String lang) {
		final IContentType[] contentTypes = Platform.getContentTypeManager().findContentTypesFor("x." + lang);
		return TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(contentTypes);
	}
}
