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
package org.eclipse.tm4e.languageconfiguration.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.languageconfiguration.internal.preferences.PreferenceHelper;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPair;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPairSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.supports.EnterActionAndIndent;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterSupport;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.osgi.service.prefs.BackingStoreException;

public final class LanguageConfigurationRegistryManager extends AbstractLanguageConfigurationRegistryManager {

	private static final String EXTENSION_LANGUAGE_CONFIGURATIONS = "languageConfigurations"; //$NON-NLS-1$
	private static final String LANGUAGE_CONFIGURATION_ELT = "languageConfiguration"; //$NON-NLS-1$

	@Nullable
	private static LanguageConfigurationRegistryManager INSTANCE;

	public static LanguageConfigurationRegistryManager getInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		INSTANCE = createInstance();
		return INSTANCE;
	}

	private static synchronized LanguageConfigurationRegistryManager createInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		final LanguageConfigurationRegistryManager manager = new LanguageConfigurationRegistryManager();
		manager.load();
		return manager;
	}

	@Nullable
	private LanguageConfigurationDefinition getDefinition(final IContentType contentType) {
		LanguageConfigurationDefinition bestFit = null;
		for (final var iDefinition : getDefinitions()) {
			if (iDefinition instanceof LanguageConfigurationDefinition definition) {
				final var definitionContentType = definition.getContentType();
				if (contentType.isKindOf(definitionContentType)
						&& (bestFit == null || definitionContentType.isKindOf(bestFit.getContentType()))) {
					bestFit = definition;
				}
			}

		}
		return bestFit;
	}

	@Nullable
	public CharacterPair getAutoClosePair(final String text, final int offset, final String newCharacter,
			final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null || !definition.isBracketAutoClosingEnabled()) {
			return null;
		}
		final CharacterPairSupport characterPairSupport = this._getCharacterPairSupport(contentType);
		return characterPairSupport == null ? null : characterPairSupport.getAutoClosePair(text, offset, newCharacter);
	}

	public boolean shouldSurroundingPairs(final IDocument document, final int offset, final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null || !definition.isMatchingPairsEnabled()) {
			return false;
		}
		final var characterPairSupport = this._getCharacterPairSupport(contentType);
		return characterPairSupport != null;
	}

	public boolean shouldEnterAction(final IDocument document, final int offset, final IContentType contentType) {
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
		if (commentSupport == null) {
			return false;
		}
		return true;
	}

	public List<CharacterPair> getEnabledAutoClosingPairs(final IContentType contentType) {
		final var definition = getDefinition(contentType);
		if (definition == null || !definition.isBracketAutoClosingEnabled()) {
			return Collections.emptyList();
		}
		final var characterPairSupport = this._getCharacterPairSupport(contentType);
		if (characterPairSupport == null) {
			return Collections.emptyList();
		}
		return characterPairSupport.getAutoClosingPairs();
	}

	public List<CharacterPair> getSurroundingPairs(final IContentType contentType) {
		final var characterPairSupport = this._getCharacterPairSupport(contentType);
		if (characterPairSupport == null) {
			return Collections.emptyList();
		}
		return characterPairSupport.getSurroundingPairs();
	}

	@Nullable
	public EnterActionAndIndent getEnterAction(final IDocument document, final int offset,
			final IContentType contentType) {
		String indentation = TextUtils.getLinePrefixingWhitespaceAtPosition(document, offset);
		// let scopedLineTokens = this.getScopedLineTokens(model, range.startLineNumber, range.startColumn);
		final var onEnterSupport = this._getOnEnterSupport(contentType /* scopedLineTokens.languageId */);
		if (onEnterSupport == null) {
			return null;
		}

		try {
			final IRegion lineInfo = document.getLineInformationOfOffset(offset);

			// String scopeLineText = DocumentHelper.getLineTextOfOffset(document, offset, false);
			final String beforeEnterText = document.get(lineInfo.getOffset(), offset - lineInfo.getOffset());
			String afterEnterText = null;

			// selection support
			// if (range.isEmpty()) {
			afterEnterText = document.get(offset, lineInfo.getLength() - (offset - lineInfo.getOffset())); // scopedLineText.substr(range.startColumn
																											 // - 1 -
																											 // scopedLineTokens.firstCharOffset);
			// } else {
			// const endScopedLineTokens = this.getScopedLineTokens(model,
			// range.endLineNumber, range.endColumn);
			// afterEnterText = endScopedLineTokens.getLineContent().substr(range.endColumn
			// - 1 - scopedLineTokens.firstCharOffset);
			// }

			final String oneLineAboveText = ""; //$NON-NLS-1$
			/*
			 * let lineNumber = range.startLineNumber; let oneLineAboveText = '';
			 *
			 * if (lineNumber > 1 && scopedLineTokens.firstCharOffset === 0) { // This is
			 * not the first line and the entire line belongs to this mode let
			 * oneLineAboveScopedLineTokens = this.getScopedLineTokens(model, lineNumber -
			 * 1); if (oneLineAboveScopedLineTokens.languageId ===
			 * scopedLineTokens.languageId) { // The line above ends with text belonging to
			 * the same mode oneLineAboveText =
			 * oneLineAboveScopedLineTokens.getLineContent(); } }
			 */

			EnterAction enterResult = null;
			try {
				enterResult = onEnterSupport.onEnter(oneLineAboveText, beforeEnterText, afterEnterText);
			} catch (final Exception e) {
				// onUnexpectedError(e);
			}

			if (enterResult == null) {
				return null;
			}

			// Here we add `\t` to appendText first because enterAction is leveraging
			// appendText and removeText to change indentation.
			if (enterResult.getAppendText() == null) {
				if ((enterResult.getIndentAction() == IndentAction.Indent)
						|| (enterResult.getIndentAction() == IndentAction.IndentOutdent)) {
					enterResult.setAppendText("\t"); //$NON-NLS-1$
				} else {
					enterResult.setAppendText(""); //$NON-NLS-1$
				}
			}

			final var removeText = enterResult.getRemoveText();
			if (removeText != null) {
				indentation = indentation.substring(0, indentation.length() - removeText);
			}

			return new EnterActionAndIndent(enterResult, indentation);

		} catch (final BadLocationException e1) {
		}
		return null;
	}

	@Nullable
	public CommentSupport getCommentSupport(final IContentType contentType) {
		final var definition = this.getDefinition(contentType);
		if (definition == null) {
			return null;
		}
		return definition.getCommentSupport();
	}

	@Nullable
	private OnEnterSupport _getOnEnterSupport(final IContentType contentType) {
		final var definition = this.getDefinition(contentType);
		if (definition == null) {
			return null;
		}
		return definition.getOnEnter();

	}

	@Nullable
	private CharacterPairSupport _getCharacterPairSupport(final IContentType contentType) {
		final var definition = this.getDefinition(contentType);
		if (definition == null) {
			return null;
		}
		return definition.getCharacterPair();
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
				final LanguageConfigurationDefinition delegate;
				try {
					delegate = new LanguageConfigurationDefinition(configElem);
				} catch (CoreException ex) {
					LanguageConfigurationPlugin.log(ex.getStatus());
					continue;
				}
				registerLanguageConfigurationDefinition(delegate);
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
		pluginDefinitions.values().forEach(definition -> {
			if (!(definition.isBracketAutoClosingEnabled() && definition.isMatchingPairsEnabled()
					&& definition.isOnEnterEnabled())) {
				definitions.add(definition);
			}
		});
		final var json = PreferenceHelper.toJson(definitions);
		final var prefs = InstanceScope.INSTANCE.getNode(LanguageConfigurationPlugin.PLUGIN_ID);
		prefs.put(PreferenceConstants.LANGUAGE_CONFIGURATIONS, json);
		prefs.flush();
	}
}
