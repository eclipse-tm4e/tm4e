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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Wizard to create association between grammar and theme.
 *
 */
public final class CreateThemeAssociationWizard extends Wizard {

	@Nullable
	private CreateThemeAssociationWizardPage mainPage;

	@Nullable
	private IThemeAssociation createdThemeAssociation;

	private IThemeManager themeManager = TMUIPlugin.getThemeManager();

	private final boolean save;

	@Nullable
	private IGrammarDefinition initialDefinition;

	@Nullable
	private IThemeAssociation initialAssociation;

	public CreateThemeAssociationWizard(final boolean save) {
		this.save = save;
	}

	/**
	 * Set theme manager to use to add the created theme associations.
	 */
	public void setThemeManager(final IThemeManager themeManager) {
		this.themeManager = themeManager;
	}

	@Override
	public void addPages() {
		mainPage = new CreateThemeAssociationWizardPage(initialDefinition, initialAssociation);
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		final IThemeAssociation association = castNonNull(mainPage).getThemeAssociation();
		themeManager.registerThemeAssociation(association);
		if (save) {
			try {
				themeManager.save();
			} catch (final BackingStoreException e) {
				e.printStackTrace();
				return false;
			}
		}
		createdThemeAssociation = association;
		return true;
	}

	@Nullable
	public IThemeAssociation getCreatedThemeAssociation() {
		return createdThemeAssociation;
	}

	public void setInitialDefinition(@Nullable final IGrammarDefinition definition) {
		this.initialDefinition = definition;
	}

	public void setInitialAssociation(@Nullable final IThemeAssociation association) {
		this.initialAssociation = association;
	}
}
