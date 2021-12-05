/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.markdown;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.model.ITokenizationSupport;
import org.eclipse.tm4e.core.model.LineTokens;
import org.eclipse.tm4e.core.model.TMState;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.core.model.Tokenizer;
import org.eclipse.tm4e.markdown.marked.HTMLRenderer;
import org.eclipse.tm4e.markdown.marked.Helpers;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;

public class TMHTMLRenderer extends HTMLRenderer {

	private final String defaultLang;

	public TMHTMLRenderer(String defaultLang) {
		this.defaultLang = defaultLang;
	}

	@Override
	public void code(String code, String lang, boolean escaped) {
		IGrammar grammar = lang == null ? getDefaultGrammar() : getGrammar(lang);
		if (grammar == null) {
			super.code(code, lang, escaped);
		} else {
			ITokenizationSupport tokenizationSupport = new Tokenizer(grammar);
			html.append("<div style=\"white-space: pre-wrap;\">");
			tokenizeLines(code, tokenizationSupport);
			html.append("</div>");
		}
	}

	private void tokenizeLines(String text, ITokenizationSupport tokenizationSupport) {
		String[] lines = text.split("\r\n|\r|\n");
		TMState currentState = tokenizationSupport.getInitialState();
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

	private TMState tokenizeLine(String line, ITokenizationSupport tokenizationSupport, TMState startState) {
		LineTokens tokenized = tokenizationSupport.tokenize(line, startState);
		TMState endState = tokenized.getEndState();
		List<TMToken> tokens = tokenized.getTokens();
		int offset = 0;
		String tokenText;

		// For each token inject spans with proper class names based on token
		// type
		for (int j = 0; j < tokens.size(); j++) {
			TMToken token = tokens.get(j);

			// Tokens only provide a startIndex from where they are valid from.
			// As such, we need to
			// look ahead the value of the token by advancing until the next
			// tokens start inex or the
			// end of the line.
			if (j < tokens.size() - 1) {
				tokenText = line.substring(offset, tokens.get(j + 1).startIndex);
				offset = tokens.get(j + 1).startIndex;
			} else {
				tokenText = line.substring(offset);
			}

			String className = "token";
			String safeType = token.type.replaceAll("[^a-z0-9\\-]", " ");
			if (safeType.length() > 0) {
				className += ' ' + safeType;
			}

			html.append("<span ");
			html.append("class=\"");
			html.append(className);
			html.append("\">");
			html.append(Helpers.escape(tokenText));
			html.append("</span>");
			// emitToken(className, tokenText);
		}

		return endState;
	}

	protected IGrammar getDefaultGrammar() {
		return getGrammar(defaultLang);
	}

	protected IGrammar getGrammar(String lang) {
		IContentType[] contentTypes = Platform.getContentTypeManager().findContentTypesFor("x." + lang);
		return TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(contentTypes);
	}
}
