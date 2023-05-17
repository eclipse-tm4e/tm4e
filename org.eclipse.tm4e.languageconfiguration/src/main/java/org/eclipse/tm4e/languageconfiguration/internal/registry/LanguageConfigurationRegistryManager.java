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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPair;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.model.CompleteEnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction.IndentAction;
import org.eclipse.tm4e.languageconfiguration.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.languageconfiguration.internal.preferences.PreferenceHelper;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CharacterPairSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.CommentSupport;
import org.eclipse.tm4e.languageconfiguration.internal.supports.OnEnterSupport;
import org.eclipse.tm4e.languageconfiguration.internal.utils.TextUtils;
import org.osgi.service.prefs.BackingStoreException;

public final class LanguageConfigurationRegistryManager extends AbstractLanguageConfigurationRegistryManager {

	private static final String EXTENSION_LANGUAGE_CONFIGURATIONS = "languageConfigurations"; //$NON-NLS-1$
	private static final String LANGUAGE_CONFIGURATION_ELT = "languageConfiguration"; //$NON-NLS-1$

	/**
	 * see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
	 */
	private static final class InstanceHolder {
		static final LanguageConfigurationRegistryManager INSTANCE = new LanguageConfigurationRegistryManager();
		static {
			INSTANCE.load();
		}
	}

	public static LanguageConfigurationRegistryManager getInstance() {
		return InstanceHolder.INSTANCE;
	}

	@Nullable
	private LanguageConfigurationDefinition getDefinition(final IContentType contentType) {
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

	@Nullable
	public AutoClosingPairConditional getAutoClosingPair(final String text, final int offset,
			final String newCharacter, final IContentType contentType) {
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
	 * @see <a href="https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/enterAction.ts">
	 *      https://github.com/microsoft/vscode/blob/main/src/vs/editor/common/languages/enterAction.ts</a>
	 */
	@Nullable
	public CompleteEnterAction getEnterAction(final IDocument document, final int offset,
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
			afterEnterText = document.get(offset, lineInfo.getLength() - (offset - lineInfo.getOffset()));
			// afterEnterText = scopedLineText.substr(range.startColumn - 1 - scopedLineTokens.firstCharOffset);
			// } else {
			// const endScopedLineTokens = this.getScopedLineTokens(model,
			// range.endLineNumber, range.endColumn);
			// afterEnterText = endScopedLineTokens.getLineContent().substr(range.endColumn - 1 -
			// scopedLineTokens.firstCharOffset);
			// }

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
				enterResult = onEnterSupport.onEnter(beforeEnterText, afterEnterText);
			} catch (final Exception e) {
				// onUnexpectedError(e);
			}

			if (enterResult == null) {
				return null;
			}

			// Here we add `\t` to appendText first because enterAction is leveraging
			// appendText and removeText to change indentation.
			if (enterResult.appendText == null) {
				if (enterResult.indentAction == IndentAction.Indent
						|| enterResult.indentAction == IndentAction.IndentOutdent) {
					enterResult.appendText = "\t"; //$NON-NLS-1$
				} else {
					enterResult.appendText = ""; //$NON-NLS-1$
				}
			}

			final var removeText = enterResult.removeText;
			if (removeText != null) {
				indentation = indentation.substring(0, indentation.length() - removeText);
			}

			return new CompleteEnterAction(enterResult, indentation);
		} catch (final BadLocationException e1) {
			// ignore
		}
		return null;
	}

	@Nullable
	public CommentSupport getCommentSupport(final IContentType contentType) {
		final var definition = this.getDefinition(contentType);
		return definition == null ? null : definition.getCommentSupport();
	}

	@Nullable
	private OnEnterSupport _getOnEnterSupport(final IContentType contentType) {
		final var definition = this.getDefinition(contentType);
		return definition == null ? null : definition.getOnEnter();

	}

	@Nullable
	private CharacterPairSupport _getCharacterPairSupport(final IContentType contentType) {
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
