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
package org.eclipse.tm4e.languageconfiguration.internal.widgets;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.tm4e.languageconfiguration.internal.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.ILanguageConfigurationRegistryManager;

@NonNullByDefault({})
public final class LanguageConfigurationPreferencesWidget extends LanguageConfigurationInfoWidget {

	private Button toggleOnEnterButton;
	private Button toggleBracketAutoClosingButton;
	private Button toggleMatchingPairsButton;

	private ILanguageConfigurationDefinition definition;
	private ILanguageConfigurationRegistryManager manager;

	public LanguageConfigurationPreferencesWidget(final Composite parent, final int style) {
		super(parent, style);
	}

	public void refresh(@Nullable final ILanguageConfigurationDefinition definition,
			final ILanguageConfigurationRegistryManager manager) {
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
	protected void createOnEnterRulesTab(final TabFolder folder) {
		super.createOnEnterRulesTab(folder);
		final Composite parent = (Composite) onEnterRulesTab.getControl();
		toggleOnEnterButton = new Button(parent, SWT.CHECK);
		toggleOnEnterButton.setText(LanguageConfigurationPreferencesWidget_enableOnEnterActions);
		toggleOnEnterButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toggleOnEnterButton.setEnabled(false);
		toggleOnEnterButton.addSelectionListener(widgetSelectedAdapter(e -> {
			manager.unregisterLanguageConfigurationDefinition(definition);
			definition.setOnEnterEnabled(toggleOnEnterButton.getSelection());
			manager.registerLanguageConfigurationDefinition(definition);
		}));
	}

	@Override
	protected void createAutoClosingPairsTab(final TabFolder folder) {
		super.createAutoClosingPairsTab(folder);
		final Composite parent = (Composite) autoClosingPairsTab.getControl();
		toggleBracketAutoClosingButton = new Button(parent, SWT.CHECK);
		toggleBracketAutoClosingButton.setText(LanguageConfigurationPreferencesWidget_enableAutoClosing);
		toggleBracketAutoClosingButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toggleBracketAutoClosingButton.setEnabled(false);
		toggleBracketAutoClosingButton.addSelectionListener(widgetSelectedAdapter(e -> {
			manager.unregisterLanguageConfigurationDefinition(definition);
			definition.setBracketAutoClosingEnabled(toggleBracketAutoClosingButton.getSelection());
			manager.registerLanguageConfigurationDefinition(definition);
		}));
	}

	@Override
	protected void createSurroundingPairsTab(final TabFolder folder) {
		super.createSurroundingPairsTab(folder);
		final Composite parent = (Composite) surroundingPairsTab.getControl();
		toggleMatchingPairsButton = new Button(parent, SWT.CHECK);
		toggleMatchingPairsButton.setText(LanguageConfigurationPreferencesWidget_enableMatchingBrackets);
		toggleMatchingPairsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toggleMatchingPairsButton.setEnabled(false);
		toggleMatchingPairsButton.addSelectionListener(widgetSelectedAdapter(e -> {
			manager.unregisterLanguageConfigurationDefinition(definition);
			definition.setMatchingPairsEnabled(toggleMatchingPairsButton.getSelection());
			manager.registerLanguageConfigurationDefinition(definition);
		}));
	}
}
