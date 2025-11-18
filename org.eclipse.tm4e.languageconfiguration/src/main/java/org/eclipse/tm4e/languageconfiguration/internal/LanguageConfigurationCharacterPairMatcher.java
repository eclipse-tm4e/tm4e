/**
 * Copyright (c) 2018 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Sebastian Thomschke (Vegard IT GmbH) - quote pair matching using TextMate scopes
 */
package org.eclipse.tm4e.languageconfiguration.internal;

import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcherExtension;
import org.eclipse.tm4e.core.model.ITMModel;
import org.eclipse.tm4e.core.model.TMToken;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPair;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.ui.internal.model.TMModelManager;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;

/**
 * Support of matching bracket with language configuration.
 */
public class LanguageConfigurationCharacterPairMatcher implements ICharacterPairMatcher, ICharacterPairMatcherExtension {

	private static final DefaultCharacterPairMatcher NOOP_MATCHER = new DefaultCharacterPairMatcher(new char[0]);
	private static final char[] NO_BRACKETS = new char[0];
	private static final char[] NO_QUOTES = new char[0];

	private @Nullable DefaultCharacterPairMatcher matcher;
	private @Nullable IDocument document;
	private char[] bracketPairs = NO_BRACKETS;
	private char[] quoteChars = NO_QUOTES;
	private int anchor = -1;

	@Override
	public @Nullable IRegion match(final IDocument document, final int offset) {
		final var matcher = getMatcher(document);

		// 1) try TM-aware structural bracket pairs first (when grammar is available)
		final var tmBracketRegion = matchBracketsWithTM(document, offset);
		if (tmBracketRegion != null)
			return tmBracketRegion;

		// 2) fall back to DefaultCharacterPairMatcher for brackets
		final var bracketRegion = filterBracketRegion(document, matcher.match(document, offset));
		if (bracketRegion != null) {
			anchor = matcher.getAnchor();
			return bracketRegion;
		}

		// 3) finally, try quote matching for symmetric pairs
		final var quoteRegion = matchQuotes(document, offset);
		if (quoteRegion != null)
			return quoteRegion;

		anchor = -1;
		return null;
	}

	@Override
	public @Nullable IRegion match(final IDocument document, final int offset, final int length) {
		final var matcher = getMatcher(document);

		// 1) for caret-only selection, delegate to core match(...)
		if (length == 0)
			return match(document, offset);

		// 2) for extended selections, fall back to DefaultCharacterPairMatcher
		final var bracketRegion = filterBracketRegion(document, matcher.match(document, offset, length));
		if (bracketRegion != null) {
			anchor = matcher.getAnchor();
			return bracketRegion;
		}

		anchor = -1;
		return null;
	}

	@Override
	public int getAnchor() {
		return anchor;
	}

	@Override
	public @Nullable IRegion findEnclosingPeerCharacters(final IDocument document, final int offset, final int length) {
		final var matcher = getMatcher(document);
		final var region = filterBracketRegion(document, matcher.findEnclosingPeerCharacters(document, offset, length));
		if (region == null) {
			anchor = -1;
			return null;
		}
		anchor = matcher.getAnchor();
		return region;
	}

	@Override
	public boolean isMatchedChar(final char ch) {
		final var document = this.document;
		if (document == null)
			return false;

		final var matcher = getMatcher(document); // ensure quoteChars is initialized
		return isQuoteChar(ch) || matcher.isMatchedChar(ch);
	}

	@Override
	public boolean isMatchedChar(final char ch, final IDocument document, final int offset) {
		final var matcher = getMatcher(document); // ensure quoteChars is initialized
		return isQuoteChar(ch) || matcher.isMatchedChar(ch, document, offset);
	}

	@Override
	public boolean isRecomputationOfEnclosingPairRequired(final IDocument document, final IRegion currentSelection,
			final IRegion previousSelection) {
		return getMatcher(document)
				.isRecomputationOfEnclosingPairRequired(document, currentSelection, previousSelection);
	}

	@Override
	public void dispose() {
		if (matcher != null) {
			matcher.dispose();
		}
		matcher = null;
		bracketPairs = NO_BRACKETS;
		quoteChars = NO_QUOTES;
		anchor = -1;
	}

	@Override
	public void clear() {
		if (matcher != null) {
			matcher.clear();
		}
		anchor = -1;
	}

	/**
	 * @return the matcher for the document.
	 */
	private DefaultCharacterPairMatcher getMatcher(final IDocument document) {
		var matcher = this.matcher;
		if (matcher == null || !document.equals(this.document)) {
			this.document = document;

			// initialize a DefaultCharacterPairMatcher by using character pairs of the language configuration.
			final ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
			final IContentType[] contentTypes = info == null ? null : info.getContentTypes();

			if (contentTypes == null || contentTypes.length == 0) {
				this.matcher = matcher = NOOP_MATCHER;
				bracketPairs = NO_BRACKETS;
				quoteChars = NO_QUOTES;
			} else {
				final var surroundingBracketsChars = new StringBuilder();
				final var surroundingQuotes = new ArrayList<Character>();
				final var registry = LanguageConfigurationRegistryManager.getInstance();
				for (final IContentType contentType : contentTypes) {
					if (registry.shouldSurroundingPairs(contentType)) {
						for (final AutoClosingPair surroundingPair : registry.getSurroundingPairs(contentType)) {
							if (Objects.equals(surroundingPair.open, surroundingPair.close) && surroundingPair.open.length() == 1) {
								// symmetric, single-character pairs like " and '
								surroundingQuotes.add(surroundingPair.open.charAt(0));
							} else {
								surroundingBracketsChars.append(surroundingPair.open);
								surroundingBracketsChars.append(surroundingPair.close);
							}
						}
					}
				}
				final var hasBrackets = !surroundingBracketsChars.isEmpty();
				final var hasQuotes = !surroundingQuotes.isEmpty();

				if (!hasBrackets && !hasQuotes) {
					this.matcher = matcher = NOOP_MATCHER;
					bracketPairs = NO_BRACKETS;
					quoteChars = NO_QUOTES;
				} else {
					final char[] bracketsChars;
					if (hasBrackets) {
						bracketsChars = new char[surroundingBracketsChars.length()];
						surroundingBracketsChars.getChars(0, surroundingBracketsChars.length(), bracketsChars, 0);
					} else {
						bracketsChars = new char[0];
					}
					final char[] quotes;
					if (hasQuotes) {
						quotes = new char[surroundingQuotes.size()];
						for (int i = 0; i < surroundingQuotes.size(); i++) {
							quotes[i] = surroundingQuotes.get(i).charValue();
						}
					} else {
						quotes = NO_QUOTES;
					}

					bracketPairs = bracketsChars;
					quoteChars = quotes;
					this.matcher = matcher = hasBrackets ? new DefaultCharacterPairMatcher(bracketsChars) : NOOP_MATCHER;
				}
			}
		}
		return matcher;
	}

	private boolean isQuoteChar(final char ch) {
		for (final char quote : quoteChars) {
			if (quote == ch)
				return true;
		}
		return false;
	}

	private boolean isBracketChar(final char ch) {
		for (final char c : bracketPairs) {
			if (c == ch)
				return true;
		}
		return false;
	}

	private boolean isOpeningBracket(final char ch) {
		for (int i = 0; i < bracketPairs.length; i += 2) {
			if (bracketPairs[i] == ch)
				return true;
		}
		return false;
	}

	private char getMatchingBracket(final char ch) {
		for (int i = 0; i < bracketPairs.length; i += 2) {
			final char open = bracketPairs[i];
			final char close = bracketPairs[i + 1];
			if (open == ch)
				return close;
			if (close == ch)
				return open;
		}
		return '\0';
	}

	private @Nullable IRegion matchBracketsWithTM(final IDocument document, final int offset) {
		if (bracketPairs.length == 0)
			return null;

		if (offset < 0 || offset > document.getLength())
			return null;

		final var connectedModel = TMModelManager.INSTANCE.getConnectedModel(document);
		if (connectedModel == null || connectedModel.getGrammar() == null) {
			return null;
		}
		final ITMModel tmModel = connectedModel;

		try {
			final int docLength = document.getLength();
			if (docLength == 0)
				return null;

			final char prevChar = offset > 0 ? document.getChar(offset - 1) : Character.MIN_VALUE;
			final char currChar = offset < docLength ? document.getChar(offset) : Character.MIN_VALUE;

			char bracketChar;
			boolean searchForward;
			int bracketOffset;

			if (isBracketChar(prevChar)) {
				bracketChar = prevChar;
				bracketOffset = offset - 1;
				searchForward = isOpeningBracket(prevChar);
			} else if (isBracketChar(currChar)) {
				bracketChar = currChar;
				bracketOffset = offset;
				searchForward = isOpeningBracket(currChar);
			} else {
				return null;
			}

			// do not treat brackets inside string/comment/character tokens as structural
			if (isInsideStringCommentOrCharacterToken(tmModel, document, bracketOffset)) {
				return null;
			}

			final char mate = getMatchingBracket(bracketChar);
			if (mate == '\0')
				return null;

			if (searchForward) {
				final int startPos = bracketOffset + 1;
				int nesting = 0;
				for (int pos = startPos; pos < docLength; pos++) {
					if (isInsideStringCommentOrCharacterToken(tmModel, document, pos)) {
						continue;
					}
					final char c = document.getChar(pos);
					if (c == bracketChar) {
						nesting++;
					} else if (c == mate) {
						if (nesting == 0) {
							anchor = ICharacterPairMatcher.LEFT;
							final int start = bracketOffset;
							final int end = pos;
							return new Region(Math.min(start, end), Math.abs(end - start) + 1);
						}
						nesting--;
					}
				}
			} else {
				final int startPos = bracketOffset - 1;
				int nesting = 0;
				for (int pos = startPos; pos >= 0; pos--) {
					if (isInsideStringCommentOrCharacterToken(tmModel, document, pos)) {
						continue;
					}
					final char c = document.getChar(pos);
					if (c == bracketChar) {
						nesting++;
					} else if (c == mate) {
						if (nesting == 0) {
							anchor = ICharacterPairMatcher.RIGHT;
							final int start = pos;
							final int end = bracketOffset;
							return new Region(Math.min(start, end), Math.abs(end - start) + 1);
						}
						nesting--;
					}
				}
			}
		} catch (final BadLocationException e) {
			// ignore and fall through to default matcher
		}
		return null;
	}

	private @Nullable IRegion filterBracketRegion(final IDocument document, final @Nullable IRegion region) {
		if (region == null)
			return null;

		final var connectedModel = TMModelManager.INSTANCE.getConnectedModel(document);
		if (connectedModel == null || connectedModel.getGrammar() == null) {
			return region;
		}

		final ITMModel tmModel = connectedModel;
		final int startOffset = region.getOffset();
		final int endOffset = region.getOffset() + region.getLength() - 1;
		try {
			final boolean startInString = isInsideStringCommentOrCharacterToken(tmModel, document, startOffset);
			final boolean endInString = isInsideStringCommentOrCharacterToken(tmModel, document, endOffset);
			if (startInString != endInString) {
				// one end inside string/character, the other outside: discard this pair
				return null;
			}
		} catch (final BadLocationException e) {
			// if token lookup fails, fall back to the original region
			return region;
		}
		return region;
	}

	private @Nullable IRegion matchQuotes(final IDocument document, final int offset) {
		if (quoteChars.length == 0)
			return null;

		if (offset < 0 || offset > document.getLength())
			return null;

		final var connectedModel = TMModelManager.INSTANCE.getConnectedModel(document);
		if (connectedModel == null)
			return null;

		final ITMModel tmModel = connectedModel;
		if (tmModel.getGrammar() == null)
			return null; // no grammar => no reliable token scopes

		try {
			if (document.getLength() == 0)
				return null;

			final int docLength = document.getLength();
			DelimiterInfo delimiter = null;

			// check character before caret first
			if (offset > 0) {
				delimiter = findDelimiterAt(tmModel, document, offset - 1);
			}
			// then try character at caret
			if (delimiter == null && offset < docLength) {
				delimiter = findDelimiterAt(tmModel, document, offset);
			}
			if (delimiter == null)
				return null;

			if (!isQuoteChar(delimiter.ch))
				return null;

			int mateOffset = -1;
			if (delimiter.isBegin && !delimiter.isEnd) {
				mateOffset = findMatchingQuoteForward(tmModel, document, delimiter);
				anchor = ICharacterPairMatcher.LEFT;
			} else if (delimiter.isEnd && !delimiter.isBegin) {
				mateOffset = findMatchingQuoteBackward(tmModel, document, delimiter);
				anchor = ICharacterPairMatcher.RIGHT;
			} else if (delimiter.isBegin && delimiter.isEnd) {
				// ambiguous scopes, try forward first, then backward
				mateOffset = findMatchingQuoteForward(tmModel, document, delimiter);
				if (mateOffset != -1) {
					anchor = ICharacterPairMatcher.LEFT;
				} else {
					mateOffset = findMatchingQuoteBackward(tmModel, document, delimiter);
					if (mateOffset != -1) {
						anchor = ICharacterPairMatcher.RIGHT;
					}
				}
			} else
				return null; // not tagged as begin or end; do not try to guess

			if (mateOffset == -1)
				return null;

			final int start = Math.min(delimiter.offset, mateOffset);
			final int end = Math.max(delimiter.offset, mateOffset) + 1;
			return new Region(start, end - start);
		} catch (final BadLocationException e) {
			return null;
		}
	}

	private static @Nullable DelimiterInfo findDelimiterAt(final ITMModel tmModel, final IDocument document, final int offset)
			throws BadLocationException {
		if (offset < 0 || offset >= document.getLength()) {
			return null;
		}
		final int lineIndex = document.getLineOfOffset(offset);
		final int lineOffset = document.getLineOffset(lineIndex);
		final int column = offset - lineOffset;

		final var tokens = tmModel.getLineTokens(lineIndex);
		if (tokens == null || tokens.isEmpty())
			return null;

		final int lineLength = document.getLineLength(lineIndex);
		final String lineText = document.get(lineOffset, lineLength);

		for (int i = 0; i < tokens.size(); i++) {
			final TMToken token = tokens.get(i);
			final int tokenStart = token.startIndex;
			final int tokenEnd = (i + 1 < tokens.size()) ? tokens.get(i + 1).startIndex : lineLength;
			if (column < tokenStart || column >= tokenEnd) {
				continue;
			}

			if (column >= lineText.length())
				return null;

			final char ch = lineText.charAt(column);

			if (!hasStringDelimiterScope(token))
				return null;

			final boolean isBegin = isBeginDelimiter(token);
			final boolean isEnd = isEndDelimiter(token);
			if (!isBegin && !isEnd)
				return null; // e.g. escaped quote inside string

			final String stringScope = findStringScope(token);
			return new DelimiterInfo(offset, lineIndex, ch, isBegin, isEnd, stringScope);
		}
		return null;
	}

	private static int findMatchingQuoteForward(final ITMModel tmModel, final IDocument document, final DelimiterInfo start)
			throws BadLocationException {
		final int lineCount = document.getNumberOfLines();
		final char quoteChar = start.ch;
		final @Nullable String expectedScope = start.stringScope;

		for (int line = start.lineIndex; line < lineCount; line++) {
			final var tokens = tmModel.getLineTokens(line);
			if (tokens == null || tokens.isEmpty()) {
				continue;
			}
			final int lineOffset = document.getLineOffset(line);
			final int lineLength = document.getLineLength(line);
			final String lineText = document.get(lineOffset, lineLength);

			for (int i = 0; i < tokens.size(); i++) {
				final TMToken token = tokens.get(i);
				final int tokenStartColumn = token.startIndex;
				final int tokenStartOffset = lineOffset + tokenStartColumn;

				if (line == start.lineIndex && tokenStartOffset <= start.offset) {
					// skip the starting delimiter itself and anything before it
					continue;
				}

				if (!hasStringDelimiterScope(token)) {
					continue;
				}
				if (!matchesStringScope(expectedScope, token)) {
					continue;
				}
				if (tokenStartColumn >= lineText.length()) {
					continue;
				}
				final char ch = lineText.charAt(tokenStartColumn);
				if (ch != quoteChar) {
					continue;
				}
				if (isEndDelimiter(token))
					return tokenStartOffset;
			}
		}
		return -1;
	}

	private static int findMatchingQuoteBackward(final ITMModel tmModel, final IDocument document, final DelimiterInfo start)
			throws BadLocationException {
		final char quoteChar = start.ch;
		final @Nullable String expectedScope = start.stringScope;

		for (int line = start.lineIndex; line >= 0; line--) {
			final var tokens = tmModel.getLineTokens(line);
			if (tokens == null || tokens.isEmpty()) {
				continue;
			}
			final int lineOffset = document.getLineOffset(line);
			final int lineLength = document.getLineLength(line);
			final String lineText = document.get(lineOffset, lineLength);

			for (int i = tokens.size() - 1; i >= 0; i--) {
				final TMToken token = tokens.get(i);
				final int tokenStartColumn = token.startIndex;
				final int tokenStartOffset = lineOffset + tokenStartColumn;

				if (line == start.lineIndex && tokenStartOffset >= start.offset) {
					// skip the starting delimiter itself and anything after it
					continue;
				}

				if (!hasStringDelimiterScope(token)) {
					continue;
				}
				if (!matchesStringScope(expectedScope, token)) {
					continue;
				}
				if (tokenStartColumn >= lineText.length()) {
					continue;
				}
				final char ch = lineText.charAt(tokenStartColumn);
				if (ch != quoteChar) {
					continue;
				}
				if (isBeginDelimiter(token))
					return tokenStartOffset;
			}
		}
		return -1;
	}

	private static boolean hasStringDelimiterScope(final TMToken token) {
		for (final String scope : token.scopes) {
			if (scope.contains("punctuation.definition.string")
					|| scope.contains("punctuation.definition.character"))
				return true;
		}
		return false;
	}

	private static boolean isBeginDelimiter(final TMToken token) {
		for (final String scope : token.scopes) {
			if ((scope.contains("punctuation.definition.string")
					|| scope.contains("punctuation.definition.character"))
					&& scope.contains("begin"))
				return true;
		}
		return false;
	}

	private static boolean isEndDelimiter(final TMToken token) {
		for (final String scope : token.scopes) {
			if ((scope.contains("punctuation.definition.string")
					|| scope.contains("punctuation.definition.character"))
					&& scope.contains("end"))
				return true;
		}
		return false;
	}

	private static @Nullable String findStringScope(final TMToken token) {
		for (final String scope : token.scopes) {
			if (scope.startsWith("string.") || scope.contains(".string."))
				return scope;
		}
		return null;
	}

	private static boolean matchesStringScope(final @Nullable String expectedScope, final TMToken token) {
		if (expectedScope == null)
			return true;
		final String otherScope = findStringScope(token);
		return Objects.equals(expectedScope, otherScope);
	}

	private static boolean isInsideStringCommentOrCharacterToken(final ITMModel tmModel, final IDocument document, final int offset)
			throws BadLocationException {
		if (offset < 0 || offset >= document.getLength()) {
			return false;
		}
		final int lineIndex = document.getLineOfOffset(offset);
		final int lineOffset = document.getLineOffset(lineIndex);
		final int column = offset - lineOffset;

		final var tokens = tmModel.getLineTokens(lineIndex);
		if (tokens == null || tokens.isEmpty())
			return false;

		final int lineLength = document.getLineLength(lineIndex);
		for (int i = 0; i < tokens.size(); i++) {
			final TMToken token = tokens.get(i);
			final int tokenStart = token.startIndex;
			final int tokenEnd = (i + 1 < tokens.size()) ? tokens.get(i + 1).startIndex : lineLength;
			if (column < tokenStart || column >= tokenEnd) {
				continue;
			}
			for (final String scope : token.scopes) {
				if (scope.startsWith("string.") || scope.contains(".string.")
						|| scope.startsWith("comment.") || scope.contains(".comment.")
						|| scope.contains("constant.character")) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	private record DelimiterInfo(int offset, int lineIndex, char ch, boolean isBegin, boolean isEnd, @Nullable String stringScope) {
	}
}
