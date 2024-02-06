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
package org.eclipse.tm4e.ui.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
@NonNullByDefault({})
public final class TMUIMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.tm4e.ui.internal.TMUIMessages"; //$NON-NLS-1$

	// Buttons
	public static String Button_new;
	public static String Button_edit;
	public static String Button_remove;
	public static String Button_browse_FileSystem;
	public static String Button_browse_Workspace;

	// TextMate preferences page
	public static String TextMatePreferencePage_GrammarRelatedLink;
	public static String TextMatePreferencePage_LanguageConfigurationRelatedLink;
	public static String TextMatePreferencePage_TaskTagsRelatedLink;
	public static String TextMatePreferencePage_ThemeRelatedLink;

	// Grammar preferences page
	public static String GrammarPreferencePage_title;
	public static String GrammarPreferencePage_description;
	public static String GrammarPreferencePage_column_scopeName;
	public static String GrammarPreferencePage_column_path;
	public static String GrammarPreferencePage_column_pluginId;
	public static String GrammarPreferencePage_tab_general_text;
	public static String GrammarInfoWidget_name_text;
	public static String GrammarInfoWidget_scopeName_text;
	public static String GrammarInfoWidget_fileTypes_text;
	public static String GrammarPreferencePage_tab_contentType_text;
	public static String GrammarPreferencePage_tab_theme_text;
	public static String GrammarPreferencePage_tab_injection_text;
	public static String GrammarPreferencePage_preview;

	// Task Tags preferences page
	public static String TaskTagsPreferencePage_description;
	public static String TaskTagsPreferencePage_column_tag;
	public static String TaskTagsPreferencePage_column_type;
	public static String TaskTagsPreferencePage_column_level;
	public static String TaskTagsPreferencePage_addTagDialog_windowTitle;
	public static String TaskTagsPreferencePage_addTagDialog_header;
	public static String TaskTagsPreferencePage_addTagDialog_message;
	public static String TaskTagsPreferencePage_editTagDialog_windowTitle;
	public static String TaskTagsPreferencePage_editTagDialog_header;
	public static String TaskTagsPreferencePage_editTagDialog_message;

	// Theme preferences page
	public static String ThemePreferencePage_title;
	public static String ThemePreferencePage_description;
	public static String ThemePreferencePage_column_name;
	public static String ThemePreferencePage_column_type;
	public static String ThemePreferencePage_column_path;
	public static String ThemePreferencePage_column_pluginId;
	public static String ThemePreferencePage_darkThemeButton_label;
	public static String ThemePreferencePage_defaultThemeButton_label;
	public static String ThemePreferencePage_preview;

	// Widgets
	public static String ContentTypesBindingWidget_description;
	public static String ThemeAssociationsWidget_description;
	public static String ThemeAssociationsWidget_remove_dialog_title;
	public static String ThemeAssociationsWidget_remove_dialog_message;
	public static String ThemeAssociationLabelProvider_light;
	public static String ThemeAssociationLabelProvider_dark;

	// Wizards
	public static String SelectGrammarWizardPage_title;
	public static String SelectGrammarWizardPage_description;
	public static String SelectGrammarWizardPage_file_label;
	public static String SelectGrammarWizardPage_file_error_required;
	public static String SelectGrammarWizardPage_file_error_load;
	public static String SelectGrammarWizardPage_file_error_invalid;

	public static String CreateThemeAssociationWizardPage_title;
	public static String CreateThemeAssociationWizardPage_description;
	public static String CreateThemeAssociationWizardPage_theme_text;
	public static String CreateThemeAssociationWizardPage_grammar_text;
	public static String CreateThemeAssociationWizardPage_theme_error_required;
	public static String CreateThemeAssociationWizardPage_grammar_error_required;
	public static String CreateThemeAssociationWizardPage_whenDark_text;

	// TMPresentationReconciler register dialog confirmation
	public static String TMPresentationReconciler_register_dialog_title;
	public static String TMPresentationReconciler_register_dialog_message;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TMUIMessages.class);
	}

}
