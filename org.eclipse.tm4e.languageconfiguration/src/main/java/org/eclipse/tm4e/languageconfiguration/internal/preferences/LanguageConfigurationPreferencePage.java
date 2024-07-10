/**
 * Copyright (c) 2015-2018 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * - Lucas Bullen (Red Hat Inc.) - configuration viewing and editing
 * - Sebastian Thomschke (Vegard IT) - major cleanup/refactoring, added table filtering and performDefaults support
 */
package org.eclipse.tm4e.languageconfiguration.internal.preferences;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lateNonNull;
import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.widgets.LanguageConfigurationPreferencesWidget;
import org.eclipse.tm4e.languageconfiguration.internal.wizards.LanguageConfigurationImportWizard;
import org.eclipse.tm4e.ui.internal.preferences.AbstractPreferencePage;
import org.eclipse.tm4e.ui.internal.widgets.TableWidget;
import org.eclipse.tm4e.ui.internal.widgets.TableWithControlsWidget;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A language configuration preference page allows configuration of the language configuration.
 * It provides controls for adding, removing and changing language configuration as well as enablement, default management.
 */
public final class LanguageConfigurationPreferencePage extends AbstractPreferencePage {

	static final String PAGE_ID = "org.eclipse.tm4e.languageconfiguration.preferences.LanguageConfigurationPreferencePage"; //$NON-NLS-1$

	private ILanguageConfigurationRegistryManager.EditSession manager = LanguageConfigurationRegistryManager.getInstance().newEditSession();
	private TableWidget<ILanguageConfigurationDefinition> langCfgsTable = lateNonNull();

	public LanguageConfigurationPreferencePage() {
		super(LanguageConfigurationPreferencePage_title, LanguageConfigurationPreferencePage_description);
	}

	@Override
	protected Control createContents(final @NonNullByDefault({}) Composite parent) {
		final var control = new Composite(parent, SWT.NONE);
		control.setLayout(GridLayoutFactory.fillDefaults().create());

		createLanguageConfigsTable(control);

		final var infoWidget = new LanguageConfigurationPreferencesWidget(control, SWT.NONE);
		infoWidget.setLayoutData(GridDataFactory.fillDefaults().create());

		Dialog.applyDialogFont(control);

		langCfgsTable.onSelectionChanged(selectedDefinitions -> {
			infoWidget.refresh(selectedDefinitions.isEmpty()
					? null
					: selectedDefinitions.get(0), manager);
		});
		langCfgsTable.setInput(manager);

		return control;
	}

	private void createLanguageConfigsTable(final Composite parent) {

		final var tableWithControls = new TableWithControlsWidget<ILanguageConfigurationDefinition>(parent,
				LanguageConfigurationPreferencePage_description2, true) {

			@Override
			protected TableWidget<ILanguageConfigurationDefinition> createTable(final Composite parent) {
				return new TableWidget<>(parent, false) {

					@Override
					protected void createColumns() {
						createAutoResizeColumn(LanguageConfigurationPreferencePage_column_contentTypeName, 1);
						createAutoResizeColumn(LanguageConfigurationPreferencePage_column_contentTypeId, 2);
						createAutoResizeColumn(LanguageConfigurationPreferencePage_column_source, 1);
					}

					@Override
					protected @Nullable String getColumnText(final ILanguageConfigurationDefinition def, final int columnIndex) {
						return switch (columnIndex) {
							case 0 -> def.getContentType().getName();
							case 1 -> def.getContentType().getId();
							case 2 -> (def.getPluginId() == null ? "" : "" + def.getPluginId() + " > ") + def.getPath();
							default -> null;
						};
					}

					@Override
					protected Object[] getElements(@Nullable final Object input) {
						if (input instanceof final ILanguageConfigurationRegistryManager manager)
							return manager.getDefinitions();
						return super.getElements(input);
					}
				};
			}

			@Override
			protected void createButtons() {
				// Add config
				createButton(LanguageConfigurationPreferencePage_button_add, () -> {
					// Open import wizard for language configurations.
					final var wizard = new LanguageConfigurationImportWizard(manager, false);
					final var dialog = new WizardDialog(getShell(), wizard);
					if (dialog.open() == Window.OK) {
						final var newLangCfgDef = wizard.getCreatedDefinition();
						table.refresh();
						table.setSelection(true, newLangCfgDef);
					}
				});

				// Remove config
				final var removeBtn = createButton(LanguageConfigurationPreferencePage_button_remove, () -> {
					final var langCfgDef = table.getFirstSelectedElement();
					if (langCfgDef != null && langCfgDef.getPluginId() == null) {
						manager.unregisterLanguageConfigurationDefinition(langCfgDef);
						table.refresh();
					}
				});
				table.onSelectionChanged(sel -> removeBtn.setEnabled(!sel.isEmpty() && sel.get(0).getPluginId() == null));
			}
		};

		tableWithControls.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, true)
				.hint(360, convertHeightInCharsToPixels(10))
				.create());

		langCfgsTable = tableWithControls.getTable();
	}

	@Override
	protected void performDefaults() {
		manager = LanguageConfigurationRegistryManager.getInstance().newEditSession();
		langCfgsTable.setInput(manager);
	}

	@Override
	public boolean performOk() {
		try {
			manager.save();
		} catch (final BackingStoreException ex) {
			LanguageConfigurationPlugin.logError(ex);
			return false;
		}
		return super.performOk();
	}
}
