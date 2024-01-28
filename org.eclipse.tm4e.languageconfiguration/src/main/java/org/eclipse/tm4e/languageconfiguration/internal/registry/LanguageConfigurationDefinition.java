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
package org.eclipse.tm4e.languageconfiguration.internal.registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPairSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.IndentRulesSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterSupport;
import org.eclipse.tm4e.registry.TMResource;
import org.eclipse.tm4e.registry.XMLConstants;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;

/**
 * Language configuration definition.
 */
public final class LanguageConfigurationDefinition extends TMResource implements ILanguageConfigurationDefinition {

	private final IContentType contentType;
	private boolean onEnterEnabled = true;
	private boolean bracketAutoClosingEnabled = true;
	private boolean matchingPairsEnabled = true;

	private @Nullable CharacterPairSupport characterPair;
	private @Nullable OnEnterSupport onEnter;
	private @Nullable IndentRulesSupport indentRules;
	private @Nullable CommentSupport comment;

	public LanguageConfigurationDefinition(final IContentType contentType, final String path) {
		super(path);
		this.contentType = contentType;
	}

	public LanguageConfigurationDefinition(final IConfigurationElement ce) throws CoreException {
		super(ce);
		final var contentTypeId = ce.getAttribute(XMLConstants.CONTENT_TYPE_ID_ATTR);
		final var contentType = ContentTypeHelper.getContentTypeById(contentTypeId);
		if (contentType == null)
			throw new CoreException(new Status(IStatus.ERROR, LanguageConfiguration.class,
					"Cannot load language configuration with unknown content type ID " + contentTypeId));
		this.contentType = contentType;
	}

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public LanguageConfigurationDefinition(final IContentType contentType, final String path,
			@Nullable final String pluginId, final boolean onEnterEnabled, final boolean bracketAutoClosingEnabled,
			final boolean matchingPairsEnabled) {
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
	@Nullable
	CharacterPairSupport getCharacterPair() {
		if (this.characterPair == null) {
			final LanguageConfiguration conf = getLanguageConfiguration();
			if (conf == null)
				return null;

			this.characterPair = new CharacterPairSupport(conf);
		}
		return characterPair;
	}

	/**
	 * Returns the "on enter" support and null otherwise.
	 *
	 * @return the "on enter" support and null otherwise.
	 */
	@Nullable
	OnEnterSupport getOnEnter() {
		if (this.onEnter == null) {
			final LanguageConfiguration conf = getLanguageConfiguration();
			if (conf == null || conf.getBrackets().isEmpty() && conf.getOnEnterRules().isEmpty())
				return null;

			this.onEnter = new OnEnterSupport(conf.getBrackets(), conf.getOnEnterRules());
		}
		return onEnter;
	}

	@Nullable
	IndentRulesSupport getIndentRules() {
		if (this.indentRules == null) {
			final LanguageConfiguration conf = getLanguageConfiguration();
			if (conf == null)
				return null;

			var indentRules = conf.getIndentationRules();
			if (indentRules != null) {
				this.indentRules = new IndentRulesSupport(indentRules);
			}
		}
		return indentRules;
	}

	/**
	 * Returns the "comment" support and null otherwise.
	 *
	 * @return the "comment" support and null otherwise.
	 */
	@Nullable
	CommentSupport getCommentSupport() {
		if (this.comment == null) {
			final LanguageConfiguration conf = getLanguageConfiguration();
			if (conf == null)
				return null;

			this.comment = new CommentSupport(conf.getComments());
		}
		return comment;
	}

	@Override
	public IContentType getContentType() {
		return contentType;
	}

	@Override
	public @Nullable LanguageConfiguration getLanguageConfiguration() {
		try (var in = getInputStream()) {
			return LanguageConfiguration.load(new InputStreamReader(in, Charset.defaultCharset()));
		} catch (final IOException ex) {
			LanguageConfigurationPlugin.logError(ex);
			return null;
		}
	}

	@Override
	public boolean isOnEnterEnabled() {
		return onEnterEnabled;
	}

	@Override
	public void setOnEnterEnabled(final boolean onEnterEnabled) {
		this.onEnterEnabled = onEnterEnabled;
	}

	@Override
	public boolean isBracketAutoClosingEnabled() {
		return bracketAutoClosingEnabled;
	}

	@Override
	public void setBracketAutoClosingEnabled(final boolean bracketAutoClosingEnabled) {
		this.bracketAutoClosingEnabled = bracketAutoClosingEnabled;
	}

	@Override
	public boolean isMatchingPairsEnabled() {
		return matchingPairsEnabled;
	}

	@Override
	public void setMatchingPairsEnabled(final boolean matchingPairsEnabled) {
		this.matchingPairsEnabled = matchingPairsEnabled;
	}
}
