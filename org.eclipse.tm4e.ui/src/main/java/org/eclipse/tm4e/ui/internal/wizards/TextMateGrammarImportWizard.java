/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Wizard to import TextMate grammar.
 *
 */
public final class TextMateGrammarImportWizard extends Wizard implements IImportWizard {

	private SelectGrammarWizardPage mainPage;

	private IGrammarDefinition createdDefinition;

	private IGrammarRegistryManager grammarRegistryManager;

	private final boolean save;

	public TextMateGrammarImportWizard() {
		this(true);
	}

	public TextMateGrammarImportWizard(boolean save) {
		this.save = save;
		setGrammarRegistryManager(TMEclipseRegistryPlugin.getGrammarRegistryManager());
	}

	/**
	 * Set grammar registry to use to add the created grammar definitions.
	 * 
	 * @param grammarRegistryManager
	 */
	public void setGrammarRegistryManager(IGrammarRegistryManager grammarRegistryManager) {
		this.grammarRegistryManager = grammarRegistryManager;
	}

	@Override
	public void addPages() {
		mainPage = new SelectGrammarWizardPage();
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		IGrammarDefinition definition = mainPage.getGrammarDefinition();
		grammarRegistryManager.registerGrammarDefinition(definition);
		if (save) {
			try {
				grammarRegistryManager.save();
			} catch (BackingStoreException e) {
				e.printStackTrace();
				return false;
			}
		}
		createdDefinition = definition;
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

	}

	public IGrammarDefinition getCreatedDefinition() {
		return createdDefinition;
	}

}
