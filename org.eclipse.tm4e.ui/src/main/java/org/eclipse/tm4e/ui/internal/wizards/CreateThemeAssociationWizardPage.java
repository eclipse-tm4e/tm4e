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
package org.eclipse.tm4e.ui.internal.wizards;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.widgets.GrammarDefinitionLabelProvider;
import org.eclipse.tm4e.ui.internal.widgets.ThemeLabelProvider;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;

/**
 * Wizard page to create association between grammar and theme.
 */
final class CreateThemeAssociationWizardPage extends AbstractWizardPage {

	private static final String PAGE_NAME = CreateThemeAssociationWizardPage.class.getName();

	private ComboViewer themeViewer = lazyNonNull();
	private ComboViewer grammarsCombo = lazyNonNull();

	@Nullable
	private final IGrammarDefinition initialDefinition;

	@Nullable
	private final IThemeAssociation initialAssociation;

	private Button whenDarkButton = lazyNonNull();

	protected CreateThemeAssociationWizardPage(@Nullable final IGrammarDefinition initialDefinition,
			@Nullable final IThemeAssociation initialAssociation) {
		super(PAGE_NAME);
		super.setTitle(TMUIMessages.CreateThemeAssociationWizardPage_title);
		super.setDescription(TMUIMessages.CreateThemeAssociationWizardPage_description);
		this.initialDefinition = initialDefinition;
		this.initialAssociation = initialAssociation;
	}

	@Override
	protected void createBody(final Composite ancestor) {
		final var parent = new Composite(ancestor, SWT.NONE);
		parent.setFont(parent.getFont());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parent.setLayout(new GridLayout(4, false));

		// TextMate theme
		var label = new Label(parent, SWT.NONE);
		label.setText(TMUIMessages.CreateThemeAssociationWizardPage_theme_text);
		themeViewer = new ComboViewer(parent);
		themeViewer.setContentProvider(ArrayContentProvider.getInstance());
		themeViewer.setLabelProvider(new ThemeLabelProvider());
		themeViewer.setInput(TMUIPlugin.getThemeManager().getThemes());
		themeViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		themeViewer.getControl().addListener(SWT.Selection, this);

		label = new Label(parent, SWT.NONE);
		label.setText(TMUIMessages.CreateThemeAssociationWizardPage_grammar_text);
		grammarsCombo = new ComboViewer(parent);
		grammarsCombo.setContentProvider(ArrayContentProvider.getInstance());
		grammarsCombo.setLabelProvider(new GrammarDefinitionLabelProvider());
		grammarsCombo.setInput(TMEclipseRegistryPlugin.getGrammarRegistryManager().getDefinitions());
		grammarsCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grammarsCombo.getControl().addListener(SWT.Selection, this);

		if (initialDefinition != null) {
			grammarsCombo.setSelection(new StructuredSelection(initialDefinition));
		}

		whenDarkButton = new Button(parent, SWT.CHECK);
		whenDarkButton.setText(TMUIMessages.CreateThemeAssociationWizardPage_whenDark_text);
		final var data = new GridData();
		data.horizontalSpan = 4;
		whenDarkButton.setLayoutData(data);

		final var initialAssociation = this.initialAssociation;
		if (initialAssociation != null) {
			final ITheme selectedTheme = TMUIPlugin.getThemeManager().getThemeById(initialAssociation.getThemeId());
			if (selectedTheme != null) {
				themeViewer.setSelection(new StructuredSelection(selectedTheme));
			}
			whenDarkButton.setSelection(initialAssociation.isWhenDark());
		}
	}

	@Override
	protected void initializeDefaultValues() {
		setPageComplete(false);
	}

	@Nullable
	@Override
	protected IStatus validatePage(final Event event) {
		if (themeViewer.getSelection().isEmpty()) {
			return new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID,
					TMUIMessages.CreateThemeAssociationWizardPage_theme_error_required);
		}

		if (grammarsCombo.getSelection().isEmpty()) {
			return new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID,
					TMUIMessages.CreateThemeAssociationWizardPage_grammar_error_required);
		}
		return null;
	}

	IThemeAssociation getThemeAssociation() {
		final String themeId = ((ITheme) assertNonNull(themeViewer.getStructuredSelection().getFirstElement())).getId();
		final var grammar = (IGrammarDefinition) assertNonNull(grammarsCombo.getStructuredSelection().getFirstElement());
		final String scopeName = grammar.getScope().getName();
		final boolean whenDark = whenDarkButton.getSelection();
		return new ThemeAssociation(themeId, scopeName, whenDark);
	}

}
