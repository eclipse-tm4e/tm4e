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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.castNonNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.wizards.CreateThemeAssociationWizard;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;

/**
 * Widget which displays theme associations list on the left and "Edit", "Remove"
 * buttons on the right.
 */
public final class ThemeAssociationsWidget extends TableWithControlsWidget<IThemeAssociation> {

	private final IThemeManager.EditSession themeManager;

	public ThemeAssociationsWidget(final IThemeManager.EditSession themeManager, final Composite parent) {
		super(parent, TMUIMessages.ThemeAssociationsWidget_description, false);
		this.themeManager = themeManager;
	}

	@Override
	protected TableWidget<IThemeAssociation> createTable(final Composite parent) {
		return new TableWidget<>(parent, false) {
			{
				getTable().setHeaderVisible(false);
			}

			@Override
			protected void createColumns() {
				createColumn("", 100, 0);
			}

			@Override
			protected @Nullable String getColumnText(final IThemeAssociation association, final int columnIndex) {
				return switch (columnIndex) {
					case 0 -> {
						final String themeId = association.getThemeId();
						final ITheme theme = TMUIPlugin.getThemeManager().getThemeById(themeId);
						final String themeName = theme != null ? theme.getName() : themeId;

						final boolean isDefaultThemeAssociation = themeId
								.equals(themeManager.getDefaultTheme(association.isWhenDark()).getId());

						yield NLS.bind(association.isWhenDark()
								? TMUIMessages.ThemeAssociationLabelProvider_dark
								: TMUIMessages.ThemeAssociationLabelProvider_light,
								isDefaultThemeAssociation ? "default " : "",
								themeName);
					}
					default -> null;
				};
			}

			@Override
			protected Object[] getElements(final @Nullable Object input) {
				if (input instanceof final IGrammarDefinition grammarDef) {
					return themeManager
							.getThemeAssociationsForScope(grammarDef.getScope().getName());
				}
				return super.getElements(input);
			}
		};
	}

	@Override
	protected void createButtons() {
		final Button editButton = createButton(TMUIMessages.Button_edit, () -> {
			// Open the wizard to create association between theme and grammar.
			final var wizard = new CreateThemeAssociationWizard(themeManager, false);
			wizard.setInitialDefinition(getGrammarDefinition());
			wizard.setInitialAssociation(getTable().getFirstSelectedElement());
			final var dialog = new WizardDialog(getShell(), wizard);
			if (dialog.open() == Window.OK) {
				getTable().refresh();
				getTable().setSelection(castNonNull(wizard.getCreatedThemeAssociation()));
			}
		});

		final Button removeButton = createButton(TMUIMessages.Button_remove, () -> {
			if (MessageDialog.openConfirm(getShell(),
					TMUIMessages.ThemeAssociationsWidget_remove_dialog_title,
					TMUIMessages.ThemeAssociationsWidget_remove_dialog_message)) {
				themeManager.unregisterThemeAssociation(castNonNull(getTable().getFirstSelectedElement()));
				getTable().refresh();
			}
		});

		getTable().onSelectionChanged(associations -> {
			if (associations.isEmpty()) {
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
			} else {
				editButton.setEnabled(true);
				final IThemeAssociation association = associations.get(0);
				final boolean isDefaultThemeAssociation = association.getThemeId()
						.equals(themeManager.getDefaultTheme(association.isWhenDark()).getId());
				removeButton.setEnabled(!isDefaultThemeAssociation);
			}
		});
	}

	public @Nullable IGrammarDefinition getGrammarDefinition() {
		return getTable().getInput() instanceof final IGrammarDefinition def
				? def
				: null;
	}

	public void setGrammarDefinition(final IGrammarDefinition definition) {
		getTable().setInput(definition);
	}
}
