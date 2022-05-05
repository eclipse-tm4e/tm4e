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
package org.eclipse.tm4e.languageconfiguration;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ICharacterPairMatcherExtension;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeInfo;

/**
 * Support of matching bracket with language configuration.
 */
public class LanguageConfigurationCharacterPairMatcher
		implements ICharacterPairMatcher, ICharacterPairMatcherExtension {

	@Nullable
	private DefaultCharacterPairMatcher matcher;

	@Nullable
	private IDocument document;

	@Nullable
	@Override
	public IRegion match(@Nullable final IDocument document, final int offset) {
		if (document == null)
			return null;
		final var matcher = getMatcher(document);
		return matcher != null
				? matcher.match(document, offset)
				: null;
	}

	@Nullable
	@Override
	public IRegion match(@Nullable final IDocument document, final int offset, final int length) {
		if (document == null)
			return null;
		final var matcher = getMatcher(document);
		return matcher != null
				? matcher.match(document, offset, length)
				: null;
	}

	@Override
	public int getAnchor() {
		return matcher != null ? matcher.getAnchor() : -1;
	}

	@Nullable
	@Override
	public IRegion findEnclosingPeerCharacters(@Nullable final IDocument document, final int offset, final int length) {
		if (document == null)
			return null;
		final var matcher = getMatcher(document);
		return matcher != null
				? matcher.findEnclosingPeerCharacters(document, offset, length)
				: null;
	}

	@Override
	public boolean isMatchedChar(final char ch) {
		final var document = this.document;
		if (document == null)
			return false;
		final var matcher = getMatcher(document);
		return matcher != null
				? matcher.isMatchedChar(ch)
				: false;
	}

	@Override
	public boolean isMatchedChar(final char ch, @Nullable final IDocument document, final int offset) {
		if (document == null)
			return false;
		final var matcher = getMatcher(document);
		return matcher != null
				? matcher.isMatchedChar(ch, document, offset)
				: false;
	}

	@Override
	public boolean isRecomputationOfEnclosingPairRequired(@Nullable final IDocument document,
			@Nullable final IRegion currentSelection, @Nullable final IRegion previousSelection) {
		if (document == null)
			return false;
		final var matcher = getMatcher(document);
		return matcher != null
				? matcher.isRecomputationOfEnclosingPairRequired(document, currentSelection, previousSelection)
				: false;
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
	 * Returns the matcher from the document.
	 */
	@Nullable
	private DefaultCharacterPairMatcher getMatcher(final IDocument document) {
		if (!document.equals(this.document)) {
			matcher = null;
		}
		if (matcher == null) {
			// initialize a DefaultCharacterPairMatcher by using character pairs of the language configuration.
			final var sb = new StringBuilder();
			this.document = document;
			final IContentType[] contentTypes = findContentTypes(document);
			if (contentTypes != null) {
				final LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager
						.getInstance();
				for (final IContentType contentType : contentTypes) {
					if (!registry.shouldSurroundingPairs(document, -1, contentType)) {
						continue;
					}
					final List<CharacterPair> surroundingPairs = registry.getSurroundingPairs(contentType);
					for (final CharacterPair surroundingPair : surroundingPairs) {
						sb.append(surroundingPair.getKey());
						sb.append(surroundingPair.getValue());
					}
				}
			}
			final var chars = new char[sb.length()];
			sb.getChars(0, sb.length(), chars, 0);
			matcher = new DefaultCharacterPairMatcher(chars);
		}
		return matcher;
	}

	private IContentType @Nullable [] findContentTypes(final IDocument document) {
		try {
			final ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
			if (info != null) {
				return info.getContentTypes();
			}
		} catch (final CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
