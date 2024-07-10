/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.wizards;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lateNonNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationRegistryManager;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Wizard to import language configurations
 */
public final class LanguageConfigurationImportWizard extends Wizard implements IImportWizard {

	private final ILanguageConfigurationRegistryManager.EditSession manager;
	private final boolean saveOnFinish;

	private SelectLanguageConfigurationWizardPage mainPage = lateNonNull();
	private ILanguageConfigurationDefinition createdDefinition = lateNonNull();

	public LanguageConfigurationImportWizard(final ILanguageConfigurationRegistryManager.EditSession manager, final boolean saveOnFinish) {
		this.manager = manager;
		this.saveOnFinish = saveOnFinish;
	}

	@Override
	public void addPages() {
		mainPage = new SelectLanguageConfigurationWizardPage(manager);
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		final ILanguageConfigurationDefinition definition = mainPage.getDefinition();
		manager.registerLanguageConfigurationDefinition(definition);
		if (saveOnFinish) {
			try {
				manager.save();
			} catch (final BackingStoreException ex) {
				LanguageConfigurationPlugin.logError(ex);
				return false;
			}
		}
		createdDefinition = definition;
		return true;
	}

	@Override
	public void init(final @Nullable IWorkbench workbench, final @Nullable IStructuredSelection selection) {
	}

	public ILanguageConfigurationDefinition getCreatedDefinition() {
		return createdDefinition;
	}

}
