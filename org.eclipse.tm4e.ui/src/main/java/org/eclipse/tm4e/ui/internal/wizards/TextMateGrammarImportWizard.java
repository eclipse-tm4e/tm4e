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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lazyNonNull;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Wizard to import TextMate grammar.
 *
 */
public final class TextMateGrammarImportWizard extends Wizard implements IImportWizard {

	private final IGrammarRegistryManager.EditSession manager;
	private final boolean saveOnFinish;

	private SelectGrammarWizardPage mainPage = lazyNonNull();
	private IGrammarDefinition createdDefinition = lazyNonNull();

	public TextMateGrammarImportWizard(final IGrammarRegistryManager.EditSession manager, final boolean saveOnFinish) {
		this.manager = manager;
		this.saveOnFinish = saveOnFinish;
	}

	@Override
	public void addPages() {
		mainPage = new SelectGrammarWizardPage();
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		final IGrammarDefinition definition = mainPage.getGrammarDefinition();
		manager.registerGrammarDefinition(definition);
		if (saveOnFinish) {
			try {
				manager.save();
			} catch (final BackingStoreException ex) {
				TMUIPlugin.logError(ex);
				return false;
			}
		}
		createdDefinition = definition;
		return true;
	}

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
	}

	public IGrammarDefinition getCreatedDefinition() {
		return createdDefinition;
	}
}
