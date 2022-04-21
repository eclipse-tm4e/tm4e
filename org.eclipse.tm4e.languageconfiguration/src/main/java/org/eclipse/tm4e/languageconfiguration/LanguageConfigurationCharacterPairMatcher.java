/**
 *  Copyright (c) 2018 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
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
 *
 */
public class LanguageConfigurationCharacterPairMatcher
		implements ICharacterPairMatcher, ICharacterPairMatcherExtension {

	private DefaultCharacterPairMatcher matcher;

	private IDocument document;

	@Override
	public IRegion match(IDocument document, int offset) {
		DefaultCharacterPairMatcher matcher = getMatcher(document);
		return matcher != null ? matcher.match(document, offset) : null;
	}

	@Override
	public IRegion match(IDocument document, int offset, int length) {
		DefaultCharacterPairMatcher matcher = getMatcher(document);
		return matcher != null ? matcher.match(document, offset, length) : null;
	}

	@Override
	public int getAnchor() {
		return matcher != null ? matcher.getAnchor() : -1;
	}

	@Override
	public IRegion findEnclosingPeerCharacters(IDocument document, int offset, int length) {
		DefaultCharacterPairMatcher matcher = getMatcher(document);
		return matcher != null ? matcher.findEnclosingPeerCharacters(document, offset, length) : null;
	}

	@Override
	public boolean isMatchedChar(char ch) {
		DefaultCharacterPairMatcher matcher = getMatcher(document);
		return matcher != null ? matcher.isMatchedChar(ch) : false;
	}

	@Override
	public boolean isMatchedChar(char ch, IDocument document, int offset) {
		DefaultCharacterPairMatcher matcher = getMatcher(document);
		return matcher != null ? matcher.isMatchedChar(ch, document, offset) : false;
	}

	@Override
	public boolean isRecomputationOfEnclosingPairRequired(IDocument document, IRegion currentSelection,
			IRegion previousSelection) {
		DefaultCharacterPairMatcher matcher = getMatcher(document);
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
	 *
	 * @param document
	 * @return
	 */
	private DefaultCharacterPairMatcher getMatcher(IDocument document) {
		if (!document.equals(this.document)) {
			matcher = null;
		}
		if (matcher == null) {
			// initizalize a DefaultCharacterPairMatcher by using character pairs of the
			// language configuration.
			final StringBuilder chars = new StringBuilder();
			this.document = document;
			IContentType[] contentTypes = findContentTypes(document);
			if (contentTypes != null) {
				LanguageConfigurationRegistryManager registry = LanguageConfigurationRegistryManager.getInstance();
				for (IContentType contentType : contentTypes) {
					if (!registry.shouldSurroundingPairs(document, -1, contentType)) {
						continue;
					}
					List<CharacterPair> surroundingPairs = registry.getSurroundingPairs(contentType);
					for (CharacterPair surroundingPair : surroundingPairs) {
						chars.append(surroundingPair.getKey());
						chars.append(surroundingPair.getValue());
					}
				}
			}
			matcher = new DefaultCharacterPairMatcher(chars.toString().toCharArray());
		}
		return matcher;
	}

	private IContentType[] findContentTypes(IDocument document) {
		try {
			ContentTypeInfo info = ContentTypeHelper.findContentTypes(document);
			if(info != null) {
				return info.getContentTypes();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
