/**
 *  Copyright (c) 2015-2018 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lucas Bullen (Red Hat Inc.) - language configuration preferences
 */
package org.eclipse.tm4e.languageconfiguration.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
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

public class LanguageConfigurationRegistryManager extends AbstractLanguageConfigurationRegistryManager {

	private static final String EXTENSION_LANGUAGE_CONFIGURATIONS = "languageConfigurations"; //$NON-NLS-1$
	private static final String LANGUAGE_CONFIGURATION_ELT = "languageConfiguration"; //$NON-NLS-1$

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
		LanguageConfigurationRegistryManager manager = new LanguageConfigurationRegistryManager();
		manager.load();
		return manager;
	}

	private LanguageConfigurationDefinition getDefinition(IContentType contentType) {
		LanguageConfigurationDefinition bestFit = null;
		for (ILanguageConfigurationDefinition iDefinition : getDefinitions()) {
			if (iDefinition instanceof LanguageConfigurationDefinition) {
				LanguageConfigurationDefinition definition = (LanguageConfigurationDefinition) iDefinition;
				if (contentType.isKindOf(definition.getContentType())
						&& (bestFit == null || definition.getContentType().isKindOf(bestFit.getContentType()))) {
					bestFit = definition;
				}
			}

		}
		return bestFit;
	}

	public CharacterPair getAutoClosePair(String text, Integer offset, String newCharacter, IContentType contentType) {
		LanguageConfigurationDefinition definition = getDefinition(contentType);
		if (definition == null || !definition.isBracketAutoClosingEnabled()) {
			return null;
		}
		CharacterPairSupport characterPairSupport = this._getCharacterPairSupport(contentType);
		return characterPairSupport == null ? null : characterPairSupport.getAutoClosePair(text, offset, newCharacter);
	}

	public boolean shouldSurroundingPairs(IDocument document, int offset, IContentType contentType) {
		LanguageConfigurationDefinition definition = getDefinition(contentType);
		if (definition == null || !definition.isMatchingPairsEnabled()) {
			return false;
		}
		CharacterPairSupport characterPairSupport = this._getCharacterPairSupport(contentType);
		return characterPairSupport != null;
	}

	public boolean shouldEnterAction(IDocument document, int offset, IContentType contentType) {
		LanguageConfigurationDefinition definition = getDefinition(contentType);
		if (definition == null || !definition.isOnEnterEnabled()) {
			return false;
		}
		OnEnterSupport onEnterSupport = this._getOnEnterSupport(contentType);
		return onEnterSupport != null;
	}

	public boolean shouldComment(IContentType contentType) {
		LanguageConfigurationDefinition definition = getDefinition(contentType);
		if (definition == null || !definition.isOnEnterEnabled()) {
			return false;
		}
		CommentSupport commentSupport = this.getCommentSupport(contentType);
		if (commentSupport == null) {
			return false;
		}
		return true;
	}

	public List<CharacterPair> getAutoClosingPairs(IContentType contentType) {
		CharacterPairSupport characterPairSupport = this._getCharacterPairSupport(contentType);
		if (characterPairSupport == null) {
			return Collections.emptyList();
		}
		return characterPairSupport.getAutoClosingPairs();
	}

	public List<CharacterPair> getSurroundingPairs(IContentType contentType) {
		CharacterPairSupport characterPairSupport = this._getCharacterPairSupport(contentType);
		if (characterPairSupport == null) {
			return Collections.emptyList();
		}
		return characterPairSupport.getSurroundingPairs();
	}

	public EnterActionAndIndent getEnterAction(IDocument document, int offset, IContentType contentType) {
		String indentation = TextUtils.getLinePrefixingWhitespaceAtPosition(document, offset);
		// let scopedLineTokens = this.getScopedLineTokens(model, range.startLineNumber,
		// range.startColumn);
		OnEnterSupport onEnterSupport = this._getOnEnterSupport(contentType /* scopedLineTokens.languageId */);
		if (onEnterSupport == null) {
			return null;
		}

		try {
			IRegion lineInfo = document.getLineInformationOfOffset(offset);

			// String scopeLineText = DocumentHelper.getLineTextOfOffset(document, offset,
			// false);
			String beforeEnterText = document.get(lineInfo.getOffset(), offset - lineInfo.getOffset());
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

			String oneLineAboveText = ""; //$NON-NLS-1$
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
			} catch (Exception e) {
				// onUnexpectedError(e);
			}

			if (enterResult == null) {
				return null;
			} else {
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
			}

			if (enterResult.getRemoveText() != null) {
				indentation = indentation.substring(0, indentation.length() - enterResult.getRemoveText());
			}

			return new EnterActionAndIndent(enterResult, indentation);

		} catch (BadLocationException e1) {
		}
		return null;
	}

	public CommentSupport getCommentSupport(IContentType contentType) {
		LanguageConfigurationDefinition value = this.getDefinition(contentType);
		if (value == null) {
			return null;
		}
		return value.getCommentSupport();
	}

	private OnEnterSupport _getOnEnterSupport(IContentType contentType) {
		LanguageConfigurationDefinition value = this.getDefinition(contentType);
		if (value == null) {
			return null;
		}
		return value.getOnEnter();

	}

	private CharacterPairSupport _getCharacterPairSupport(IContentType contentType) {
		LanguageConfigurationDefinition value = this.getDefinition(contentType);
		if (value == null) {
			return null;
		}
		return value.getCharacterPair();
	}

	private void load() {
		loadFromExtensionPoints();
		loadFromPreferences();
	}

	private void loadFromExtensionPoints() {
		IConfigurationElement[] cf = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(LanguageConfigurationPlugin.PLUGIN_ID, EXTENSION_LANGUAGE_CONFIGURATIONS);
		for (IConfigurationElement ce : cf) {
			String name = ce.getName();
			if (LANGUAGE_CONFIGURATION_ELT.equals(name)) {
				LanguageConfigurationDefinition delegate = new LanguageConfigurationDefinition(ce);
				registerLanguageConfigurationDefinition(delegate);
			}
		}

	}

	private void loadFromPreferences() {
		// Load grammar definitions from the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.languageconfiguration.prefs"
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LanguageConfigurationPlugin.PLUGIN_ID);
		String json = prefs.get(PreferenceConstants.LANGUAGE_CONFIGURATIONS, null);
		if (json != null) {
			ILanguageConfigurationDefinition[] definitions = PreferenceHelper
					.loadLanguageConfigurationDefinitions(json);
			for (ILanguageConfigurationDefinition definition : definitions) {
				registerLanguageConfigurationDefinition(definition);
			}
		}
	}

	@Override
	public void save() throws BackingStoreException {
		// Save grammar definitions in the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.languageconfiguration.prefs"
		List<ILanguageConfigurationDefinition> definitions = new ArrayList<>();
		userDefinitions.values().forEach(definition -> definitions.add(definition));
		pluginDefinitions.values().forEach(definition -> {
			if (!(definition.isBracketAutoClosingEnabled() && definition.isMatchingPairsEnabled()
					&& definition.isOnEnterEnabled())) {
				definitions.add(definition);
			}
		});
		String json = PreferenceHelper.toJson(definitions);
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(LanguageConfigurationPlugin.PLUGIN_ID);
		prefs.put(PreferenceConstants.LANGUAGE_CONFIGURATIONS, json);
		prefs.flush();
	}

}
