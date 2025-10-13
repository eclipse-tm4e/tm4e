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
 */
package org.eclipse.tm4e.languageconfiguration.internal;

import java.util.Objects;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcherExtension;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPair;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;

/**
 * Support of matching bracket with language configuration.
 */
public class LanguageConfigurationCharacterPairMatcher
		implements ICharacterPairMatcher, ICharacterPairMatcherExtension {

	private static final DefaultCharacterPairMatcher NOOP_MATCHER = new DefaultCharacterPairMatcher(new char[0]);

	private @Nullable DefaultCharacterPairMatcher matcher;
	private @Nullable IDocument document;

	@Override
	public @Nullable IRegion match(final IDocument document, final int offset) {
		return getMatcher(document).match(document, offset);
	}

	@Override
	public @Nullable IRegion match(final IDocument document, final int offset, final int length) {
		return getMatcher(document).match(document, offset, length);
	}

	@Override
	public int getAnchor() {
		return matcher != null ? matcher.getAnchor() : -1;
	}

	@Override
	public @Nullable IRegion findEnclosingPeerCharacters(final IDocument document, final int offset, final int length) {
		return getMatcher(document).findEnclosingPeerCharacters(document, offset, length);
	}

	@Override
	public boolean isMatchedChar(final char ch) {
		final var document = this.document;
		if (document == null)
			return false;
		return getMatcher(document).isMatchedChar(ch);
	}

	@Override
	public boolean isMatchedChar(final char ch, final IDocument document, final int offset) {
		return getMatcher(document).isMatchedChar(ch, document, offset);
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
	}

	@Override
	public void clear() {
		if (matcher != null) {
			matcher.clear();
		}
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
			} else {
				final var surroundingBracketsChars = new StringBuilder();
				final var surroundingQuotesChars = new StringBuilder();
				final var registry = LanguageConfigurationRegistryManager.getInstance();
				for (final IContentType contentType : contentTypes) {
					if (registry.shouldSurroundingPairs(contentType)) {
						for (final AutoClosingPair surroundingPair : registry.getSurroundingPairs(contentType)) {
							if (Objects.equals(surroundingPair.open, surroundingPair.close)) {
								surroundingQuotesChars.append(surroundingPair.open);
							} else {
								surroundingBracketsChars.append(surroundingPair.open);
								surroundingBracketsChars.append(surroundingPair.close);
							}
						}
					}
				}
				if (surroundingBracketsChars.isEmpty() && surroundingQuotesChars.isEmpty()) {
					this.matcher = matcher = NOOP_MATCHER;
				} else {
					final var bracketsChars = new char[surroundingBracketsChars.length()];
					surroundingBracketsChars.getChars(0, surroundingBracketsChars.length(), bracketsChars, 0);
					// TODO handle surroundingQuotesChars, DefaultCharacterPairMatcher cannot handle pairs correctly when open and close chars
					//      are identically, see https://github.com/eclipse-tm4e/tm4e/issues/470
					this.matcher = matcher = new DefaultCharacterPairMatcher(bracketsChars);
				}
			}
		}
		return matcher;
	}
}
