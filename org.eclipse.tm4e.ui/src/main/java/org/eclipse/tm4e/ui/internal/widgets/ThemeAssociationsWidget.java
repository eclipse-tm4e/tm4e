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
package org.eclipse.tm4e.ui.internal.widgets;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.wizards.CreateThemeAssociationWizard;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;

/**
 * Widget which displays theme associations list on the left and "New", "Remove"
 * buttons on the right.
 *
 */
public final class ThemeAssociationsWidget extends TableAndButtonsWidget {

	private final IThemeManager themeManager;

	private Button editButton = lazyNonNull();
	private Button removeButton = lazyNonNull();

	@Nullable
	private IGrammarDefinition definition;

	public ThemeAssociationsWidget(final IThemeManager themeManager, final Composite parent, final int style) {
		super(parent, style, TMUIMessages.ThemeAssociationsWidget_description);
		this.themeManager = themeManager;
		super.setContentProvider(ArrayContentProvider.getInstance());
		super.setLabelProvider(new ThemeAssociationLabelProvider());
		createButtons();
	}

	private void createButtons() {
		editButton = new Button(getButtonsArea(), SWT.PUSH);
		editButton.setText(TMUIMessages.Button_edit);
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.addListener(SWT.Selection, e -> {
			// Open the wizard to create association between theme and grammar.
			final var wizard = new CreateThemeAssociationWizard(false);
			wizard.setInitialDefinition(definition);
			final IStructuredSelection selection = super.getSelection();
			wizard.setInitialAssociation(selection.isEmpty() ? null : (IThemeAssociation) selection.getFirstElement());
			wizard.setThemeManager(themeManager);
			final var dialog = new WizardDialog(getShell(), wizard);
			if (dialog.open() == Window.OK) {
				final IThemeAssociation association = wizard.getCreatedThemeAssociation();
				refresh(association);
			}
		});
		editButton.setEnabled(false);

		removeButton = new Button(getButtonsArea(), SWT.PUSH);
		removeButton.setText(TMUIMessages.Button_remove);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.addListener(SWT.Selection, e -> {
			if (MessageDialog.openConfirm(getShell(), TMUIMessages.ThemeAssociationsWidget_remove_dialog_title,
					TMUIMessages.ThemeAssociationsWidget_remove_dialog_message)) {
				final IStructuredSelection selection = super.getSelection();
				final Iterator<IThemeAssociation> it = selection.iterator();
				while (it.hasNext()) {
					final IThemeAssociation association = it.next();
					themeManager.unregisterThemeAssociation(association);
				}
				refresh(null);
			}

		});
		removeButton.setEnabled(false);
	}

	public Button getNewButton() {
		return editButton;
	}

	public Button getRemoveButton() {
		return removeButton;
	}

	public IThemeAssociation[] setGrammarDefinition(final IGrammarDefinition definition) {
		this.definition = definition;
		return refresh(null);
	}

	private IThemeAssociation[] refresh(@Nullable IThemeAssociation association) {
		final var definition = this.definition;
		if (definition == null) {
			return new IThemeAssociation[0];
		}
		final IThemeAssociation[] themeAssociations = themeManager
				.getThemeAssociationsForScope(definition.getScope().getName());
		// Refresh the list of associations
		super.setInput(themeAssociations);
		// Select the first of given association
		if (association == null && themeAssociations.length > 0) {
			association = themeAssociations[0];
		}
		if (association != null) {
			super.setSelection(new StructuredSelection(association));
		}
		return themeAssociations;
	}

}
