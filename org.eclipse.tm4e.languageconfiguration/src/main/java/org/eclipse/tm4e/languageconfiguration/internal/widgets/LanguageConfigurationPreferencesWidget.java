/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.widgets;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationRegistryManager;

public final class LanguageConfigurationPreferencesWidget extends LanguageConfigurationInfoWidget {

	private Button toggleOnEnterButton;
	private Button toggleBracketAutoClosingButton;
	private Button toggleMatchingPairsButton;

	private ILanguageConfigurationDefinition definition;
	private ILanguageConfigurationRegistryManager manager;

	public LanguageConfigurationPreferencesWidget(Composite parent, int style) {
		super(parent, style);
	}

	public void refresh(ILanguageConfigurationDefinition definition, ILanguageConfigurationRegistryManager manager) {
		super.refresh(definition == null ? null : definition.getLanguageConfiguration());
		if (definition == null) {
			toggleOnEnterButton.setEnabled(false);
			toggleOnEnterButton.setSelection(false);
			toggleBracketAutoClosingButton.setEnabled(false);
			toggleBracketAutoClosingButton.setSelection(false);
			toggleMatchingPairsButton.setEnabled(false);
			toggleMatchingPairsButton.setSelection(false);
			return;
		}
		toggleOnEnterButton.setSelection(definition.isOnEnterEnabled());
		toggleOnEnterButton.setEnabled(true);
		toggleBracketAutoClosingButton.setSelection(definition.isBracketAutoClosingEnabled());
		toggleBracketAutoClosingButton.setEnabled(true);
		toggleMatchingPairsButton.setSelection(definition.isMatchingPairsEnabled());
		toggleMatchingPairsButton.setEnabled(true);
		this.definition = definition;
		this.manager = manager;
	}

	@Override
	protected void createOnEnterRulesTab(TabFolder folder) {
		super.createOnEnterRulesTab(folder);
		Composite parent = (Composite) onEnterRulesTab.getControl();
		toggleOnEnterButton = new Button(parent, SWT.CHECK);
		toggleOnEnterButton.setText("Enable on enter actions");
		toggleOnEnterButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toggleOnEnterButton.setEnabled(false);
		toggleOnEnterButton.addSelectionListener(widgetSelectedAdapter(e -> {
			manager.unregisterLanguageConfigurationDefinition(definition);
			definition.setOnEnterEnabled(toggleOnEnterButton.getSelection());
			manager.registerLanguageConfigurationDefinition(definition);
		}));
	}

	private boolean getToggleOnEnter() {
		return toggleOnEnterButton.getSelection();
	}

	private void setToggleOnEnter(Boolean selection) {
		toggleOnEnterButton.setSelection(selection);
	}

	@Override
	protected void createAutoClosingPairsTab(TabFolder folder) {
		super.createAutoClosingPairsTab(folder);
		Composite parent = (Composite) autoClosingPairsTab.getControl();
		toggleBracketAutoClosingButton = new Button(parent, SWT.CHECK);
		toggleBracketAutoClosingButton.setText("Enable auto closing brackets");
		toggleBracketAutoClosingButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toggleBracketAutoClosingButton.setEnabled(false);
		toggleBracketAutoClosingButton.addSelectionListener(widgetSelectedAdapter(e -> {
			manager.unregisterLanguageConfigurationDefinition(definition);
			definition.setBracketAutoClosingEnabled(toggleBracketAutoClosingButton.getSelection());
			manager.registerLanguageConfigurationDefinition(definition);
		}));
	}

	private boolean getBracketAutoClosing() {
		return toggleBracketAutoClosingButton.getSelection();
	}

	private void setBracketAutoClosing(Boolean selection) {
		toggleBracketAutoClosingButton.setSelection(selection);
	}

	@Override
	protected void createSurroundingPairsTab(TabFolder folder) {
		super.createSurroundingPairsTab(folder);
		Composite parent = (Composite) surroundingPairsTab.getControl();
		toggleMatchingPairsButton = new Button(parent, SWT.CHECK);
		toggleMatchingPairsButton.setText("Enable matching brackets");
		toggleMatchingPairsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toggleMatchingPairsButton.setEnabled(false);
		toggleMatchingPairsButton.addSelectionListener(widgetSelectedAdapter(e -> {
			manager.unregisterLanguageConfigurationDefinition(definition);
			definition.setMatchingPairsEnabled(toggleMatchingPairsButton.getSelection());
			manager.registerLanguageConfigurationDefinition(definition);
		}));
	}

	private boolean getMatchingPairs() {
		return toggleMatchingPairsButton.getSelection();
	}

	private void setMatchingPairs(Boolean selection) {
		toggleMatchingPairsButton.setSelection(selection);
	}

}
