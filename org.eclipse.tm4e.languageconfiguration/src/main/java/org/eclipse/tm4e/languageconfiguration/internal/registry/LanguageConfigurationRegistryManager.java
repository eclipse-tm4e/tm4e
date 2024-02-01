/**
 * Copyright (c) 2015-2018 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Lucas Bullen (Red Hat Inc.) - language configuration preferences
 */
package org.eclipse.tm4e.languageconfiguration.internal.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.model.CompleteEnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.languageconfiguration.internal.preferences.PreferenceHelper;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPairSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterActionHelper;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterSupport;
import org.osgi.service.prefs.BackingStoreException;

public final class LanguageConfigurationRegistryManager extends AbstractLanguageConfigurationRegistryManager {

	private static final String EXTENSION_LANGUAGE_CONFIGURATIONS = "languageConfigurations"; //$NON-NLS-1$
	private static final String LANGUAGE_CONFIGURATION_ELT = "languageConfiguration"; //$NON-NLS-1$

	/** see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java */
	private static final class InstanceHolder {
		static final LanguageConfigurationRegistryManager INSTANCE = new LanguageConfigurationRegistryManager();
		static {
			INSTANCE.load();
		}
	}

	public static LanguageConfigurationRegistryManager getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private @Nullable LanguageConfigurationDefinition getDefinition(final IContentType contentType) {
		LanguageConfigurationDefinition bestFit = null;
		for (final var iDefinition : getDefinitions()) {
			if (iDefinition instanceof final LanguageConfigurationDefinition definition) {
				final var definitionContentType = definition.getContentType();
				if (contentType.isKindOf(definitionContentType)
						&& (bestFit == null || definitionContentType.isKindOf(bestFit.getContentType()))) {
					bestFit = definition;
				}
			}
		}
		return bestFit;
	}

	public @Nullable AutoClosingPairConditional getAutoClosingPair(final String text, final int offset, final String newCharacter,
			final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null || !definition.isBracketAutoClosingEnabled()) {
			return null;
		}
		final var charPairSupport = this._getCharacterPairSupport(contentType);
		return charPairSupport == null ? null
				: charPairSupport.getAutoClosingPair(text, offset, newCharacter);
	}

	public String getAutoCloseBefore(final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null) {
			return CharacterPairSupport.DEFAULT_AUTOCLOSE_BEFORE_LANGUAGE_DEFINED;
		}
		final var charPairSupport = this._getCharacterPairSupport(contentType);
		return charPairSupport == null
				? CharacterPairSupport.DEFAULT_AUTOCLOSE_BEFORE_LANGUAGE_DEFINED
				: charPairSupport.autoCloseBefore;
	}

	public boolean shouldSurroundingPairs(final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null || !definition.isMatchingPairsEnabled()) {
			return false;
		}
		final var characterPairSupport = this._getCharacterPairSupport(contentType);
		return characterPairSupport != null;
	}

	public boolean shouldEnterAction(final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null || !definition.isOnEnterEnabled()) {
			return false;
		}
		final var onEnterSupport = this._getOnEnterSupport(contentType);
		return onEnterSupport != null;
	}

	public boolean shouldComment(final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null || !definition.isOnEnterEnabled()) {
			return false;
		}
		final var commentSupport = this.getCommentSupport(contentType);
		return commentSupport != null;
	}

	public List<AutoClosingPairConditional> getEnabledAutoClosingPairs(final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null || !definition.isBracketAutoClosingEnabled()) {
			return Collections.emptyList();
		}
		final var characterPairSupport = this._getCharacterPairSupport(contentType);
		return characterPairSupport == null
				? Collections.emptyList()
				: characterPairSupport.autoClosingPairs;
	}

	public List<AutoClosingPair> getSurroundingPairs(final IContentType contentType) {
		final var characterPairSupport = this._getCharacterPairSupport(contentType);
		return characterPairSupport == null
				? Collections.emptyList()
				: characterPairSupport.surroundingPairs;
	}

	/**
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode/blob/ba2cf46e20df3edf77bdd905acde3e175d985f70/src/vs/editor/common/languages/enterAction.ts">
	 *      github.com/microsoft/vscode/src/vs/editor/common/languages/enterAction.ts</a>
	 */
	public @Nullable CompleteEnterAction getEnterAction(final IDocument doc, final int offset, final IContentType contentType) {
		final OnEnterSupport onEnterSupport = _getOnEnterSupport(contentType /* scopedLineTokens.languageId */);
		if (onEnterSupport == null) {
			return null;
		}
		return EnterActionHelper.getEnterAction(doc, offset, onEnterSupport);
	}

	public @Nullable CommentSupport getCommentSupport(final IContentType contentType) {
		final var definition = this.getDefinition(contentType);
		return definition == null ? null : definition.getCommentSupport();
	}

	private @Nullable OnEnterSupport _getOnEnterSupport(final IContentType contentType) {
		final var definition = this.getDefinition(contentType);
		return definition == null ? null : definition.getOnEnter();
	}

	private @Nullable CharacterPairSupport _getCharacterPairSupport(final IContentType contentType) {
		final var definition = this.getDefinition(contentType);
		return definition == null ? null : definition.getCharacterPair();
	}

	private void load() {
		loadFromExtensionPoints();
		loadFromPreferences();
	}

	private void loadFromExtensionPoints() {
		final var config = Platform.getExtensionRegistry().getConfigurationElementsFor(
				LanguageConfigurationPlugin.PLUGIN_ID, EXTENSION_LANGUAGE_CONFIGURATIONS);
		for (final var configElem : config) {
			final String name = configElem.getName();
			if (LANGUAGE_CONFIGURATION_ELT.equals(name)) {
				try {
					registerLanguageConfigurationDefinition(new LanguageConfigurationDefinition(configElem));
				} catch (final CoreException ex) {
					LanguageConfigurationPlugin.log(ex.getStatus());
				}
			}
		}
	}

	private void loadFromPreferences() {
		// Load grammar definitions from the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.languageconfiguration.prefs"
		final var prefs = InstanceScope.INSTANCE.getNode(LanguageConfigurationPlugin.PLUGIN_ID);
		final String json = prefs.get(PreferenceConstants.LANGUAGE_CONFIGURATIONS, null);
		if (json != null) {
			final var definitions = PreferenceHelper.loadLanguageConfigurationDefinitions(json);
			for (final var definition : definitions) {
				registerLanguageConfigurationDefinition(definition);
			}
		}
	}

	@Override
	public void save() throws BackingStoreException {
		// Save grammar definitions in the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.languageconfiguration.prefs"
		final var definitions = new ArrayList<ILanguageConfigurationDefinition>();
		userDefinitions.values().forEach(definitions::add);
		pluginDefinitions.values().forEach(def -> {
			if (!(def.isBracketAutoClosingEnabled() && def.isMatchingPairsEnabled() && def.isOnEnterEnabled())) {
				definitions.add(def);
			}
		});
		final var json = PreferenceHelper.toJson(definitions);
		final var prefs = InstanceScope.INSTANCE.getNode(LanguageConfigurationPlugin.PLUGIN_ID);
		prefs.put(PreferenceConstants.LANGUAGE_CONFIGURATIONS, json);
		prefs.flush();
	}
}
