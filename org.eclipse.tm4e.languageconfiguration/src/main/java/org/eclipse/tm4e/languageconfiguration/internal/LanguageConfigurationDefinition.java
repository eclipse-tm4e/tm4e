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
package org.eclipse.tm4e.languageconfiguration.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPairSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterSupport;
import org.eclipse.tm4e.registry.TMResource;
import org.eclipse.tm4e.registry.XMLConstants;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;

/**
 * Language configuration definition.
 *
 */
public class LanguageConfigurationDefinition extends TMResource implements ILanguageConfigurationDefinition {

	private final IContentType contentType;
	private boolean onEnterEnabled = true;
	private boolean bracketAutoClosingEnabled = true;
	private boolean matchingPairsEnabled = true;

	private CharacterPairSupport characterPair;
	private OnEnterSupport onEnter;
	private CommentSupport comment;

	public LanguageConfigurationDefinition(IContentType contentType, String path) {
		super(path);
		this.contentType = contentType;
	}

	public LanguageConfigurationDefinition(IConfigurationElement ce) {
		super(ce);
		this.contentType = ContentTypeHelper.getContentTypeById(ce.getAttribute(XMLConstants.CONTENT_TYPE_ID_ATTR));
	}

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public LanguageConfigurationDefinition(IContentType contentType, String path, String pluginId,
			boolean onEnterEnabled, boolean bracketAutoClosingEnabled, boolean matchingPairsEnabled) {
		super(path, pluginId);
		this.contentType = contentType;
		this.onEnterEnabled = onEnterEnabled;
		this.bracketAutoClosingEnabled = bracketAutoClosingEnabled;
		this.matchingPairsEnabled = matchingPairsEnabled;
	}

	/**
	 * Returns the "character pair" support and null otherwise.
	 *
	 * @return the "character pair" support and null otherwise.
	 */
	public CharacterPairSupport getCharacterPair() {
		if (this.characterPair == null) {
			ILanguageConfiguration conf = getLanguageConfiguration();
			if (conf != null) {
				this.characterPair = new CharacterPairSupport(conf.getBrackets(), conf.getAutoClosingPairs(),
						conf.getSurroundingPairs());
			}
		}
		return characterPair;
	}

	/**
	 * Returns the "on enter" support and null otherwise.
	 *
	 * @return the "on enter" support and null otherwise.
	 */
	public OnEnterSupport getOnEnter() {
		if (this.onEnter == null) {
			ILanguageConfiguration conf = getLanguageConfiguration();
			if (conf != null && (conf.getBrackets() != null || conf.getOnEnterRules() != null)) {
				this.onEnter = new OnEnterSupport(conf.getBrackets(), conf.getOnEnterRules());
			}
		}
		return onEnter;
	}

	/**
	 * Returns the "commment" support and null otherwise.
	 *
	 * @return the "commment" support and null otherwise.
	 */
	public CommentSupport getCommentSupport() {
		if (this.comment == null) {
			ILanguageConfiguration conf = getLanguageConfiguration();
			if (conf != null) {
				this.comment = new CommentSupport(conf.getComments());
			}
		}
		return comment;
	}

	@Override
	public IContentType getContentType() {
		return contentType;
	}

	@Override
	public ILanguageConfiguration getLanguageConfiguration() {
		try {
			return LanguageConfiguration.load(new InputStreamReader(getInputStream(), Charset.defaultCharset()));
		} catch (IOException e) {
			LanguageConfigurationPlugin.getInstance().getLog().log(new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID, e.getMessage(), e));
			return null;
		}
	}

	@Override
	public Boolean isOnEnterEnabled() {
		return onEnterEnabled;
	}

	@Override
	public void setOnEnterEnabled(boolean onEnterEnabled) {
		this.onEnterEnabled = onEnterEnabled;
	}

	@Override
	public Boolean isBracketAutoClosingEnabled() {
		return bracketAutoClosingEnabled;
	}

	@Override
	public void setBracketAutoClosingEnabled(boolean bracketAutoClosingEnabled) {
		this.bracketAutoClosingEnabled = bracketAutoClosingEnabled;
	}

	@Override
	public Boolean isMatchingPairsEnabled() {
		return matchingPairsEnabled;
	}

	@Override
	public void setMatchingPairsEnabled(boolean matchingPairsEnabled) {
		this.matchingPairsEnabled = matchingPairsEnabled;
	}

}
