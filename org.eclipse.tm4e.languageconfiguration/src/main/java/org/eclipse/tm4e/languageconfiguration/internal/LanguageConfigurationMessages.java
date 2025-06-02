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
package org.eclipse.tm4e.languageconfiguration.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
@NonNullByDefault({})
public final class LanguageConfigurationMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages"; //$NON-NLS-1$

	public static String AutoClosingPairConditionalTableWidget_notIn;
	public static String CharacterPairsTableWidget_end;
	public static String CharacterPairsTableWidget_start;

	public static String LanguageConfigurationInfoWidget_autoClosingPairs_title;
	public static String LanguageConfigurationInfoWidget_autoCloseBefore_message;
	public static String LanguageConfigurationInfoWidget_autoCloseBefore_title;
	public static String LanguageConfigurationInfoWidget_blockCommentsEnd;
	public static String LanguageConfigurationInfoWidget_blockCommentsStart;
	public static String LanguageConfigurationInfoWidget_brackets_title;
	public static String LanguageConfigurationInfoWidget_colorizedBracketPairs_title;
	public static String LanguageConfigurationInfoWidget_comments_title;
	public static String LanguageConfigurationInfoWidget_end;
	public static String LanguageConfigurationInfoWidget_folding_title;
	public static String LanguageConfigurationInfoWidget_indentationRules_title;
	public static String LanguageConfigurationInfoWidget_indentationRules_decreaseIndentPattern;
	public static String LanguageConfigurationInfoWidget_indentationRules_increaseIndentPattern;
	public static String LanguageConfigurationInfoWidget_indentationRules_indentNextLinePattern;
	public static String LanguageConfigurationInfoWidget_indentationRules_unIndentedLinePattern;
	public static String LanguageConfigurationInfoWidget_lineComments;
	public static String LanguageConfigurationInfoWidget_markers;
	public static String LanguageConfigurationInfoWidget_offSide;
	public static String LanguageConfigurationInfoWidget_offSide_tooltip;
	public static String LanguageConfigurationInfoWidget_onEnterRules_title;
	public static String LanguageConfigurationInfoWidget_start;
	public static String LanguageConfigurationInfoWidget_surroundingPairs_title;
	public static String LanguageConfigurationInfoWidget_wordPattern_message;
	public static String LanguageConfigurationInfoWidget_wordPattern_title;

	public static String LanguageConfigurationPreferencePage_title;
	public static String LanguageConfigurationPreferencePage_column_contentTypeName;
	public static String LanguageConfigurationPreferencePage_column_contentTypeId;
	public static String LanguageConfigurationPreferencePage_description;
	public static String LanguageConfigurationPreferencePage_description2;
	public static String LanguageConfigurationPreferencePage_button_add;
	public static String LanguageConfigurationPreferencePage_column_source;
	public static String LanguageConfigurationPreferencePage_button_remove;

	public static String LanguageConfigurationPreferencesWidget_enableAutoClosing;
	public static String LanguageConfigurationPreferencesWidget_enableIndentRules;
	public static String LanguageConfigurationPreferencesWidget_enableOnEnterActions;
	public static String LanguageConfigurationPreferencesWidget_enableMatchingBrackets;

	public static String OnEnterRuleTableWidget_beforeText;
	public static String OnEnterRuleTableWidget_afterText;
	public static String OnEnterRuleTableWidget_previousLineText;
	public static String OnEnterRuleTableWidget_indentAction;
	public static String OnEnterRuleTableWidget_appendText;
	public static String OnEnterRuleTableWidget_removeText;

	public static String SelectLanguageConfigurationWizardPage_browse_fileSystem;
	public static String SelectLanguageConfigurationWizardPage_browse_workspace;
	public static String SelectLanguageConfigurationWizardPage_contentType;
	public static String SelectLanguageConfigurationWizardPage_file;
	public static String SelectLanguageConfigurationWizardPage_fileError_error;
	public static String SelectLanguageConfigurationWizardPage_fileError_invalid;
	public static String SelectLanguageConfigurationWizardPage_fileError_noSelection;
	public static String SelectLanguageConfigurationWizardPage_contentTypeError_noSelection;
	public static String SelectLanguageConfigurationWizardPage_contentTypeError_invalid;
	public static String SelectLanguageConfigurationWizardPage_contentTypeWarning_duplicate;
	public static String SelectLanguageConfigurationWizardPage_page_description;
	public static String SelectLanguageConfigurationWizardPage_page_title;
	public static String SelectLanguageConfigurationWizardPage_workspace_description;
	public static String SelectLanguageConfigurationWizardPage_workspace_title;

	public static String ToggleLineCommentHandler_ReadOnlyEditor_title;
	public static String ToggleLineCommentHandler_ReadOnlyEditor_inputReadonly;
	public static String ToggleLineCommentHandler_ReadOnlyEditor_fileReadonly;
	public static String ToggleLineCommentHandler_ReadOnlyEditor_makingWritableFailed;

	static {
		NLS.initializeMessages(BUNDLE_NAME, LanguageConfigurationMessages.class);
	}
}
