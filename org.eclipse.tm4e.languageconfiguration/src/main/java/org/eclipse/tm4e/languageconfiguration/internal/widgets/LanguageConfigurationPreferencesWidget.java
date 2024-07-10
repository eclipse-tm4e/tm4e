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
import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lateNonNull;
import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationRegistryManager;

public final class LanguageConfigurationPreferencesWidget extends LanguageConfigurationInfoWidget {

	private @NonNullByDefault({}) Button toggleOnEnterButton;
	private @NonNullByDefault({}) Button toggleIdentRulesButton;
	private @NonNullByDefault({}) Button toggleBracketAutoClosingButton;
	private @NonNullByDefault({}) Button toggleMatchingPairsButton;

	private ILanguageConfigurationDefinition definition = lateNonNull();
	private ILanguageConfigurationRegistryManager.EditSession manager = lateNonNull();

	public LanguageConfigurationPreferencesWidget(final Composite parent, final int style) {
		super(parent, style);
	}

	public void refresh(final @Nullable ILanguageConfigurationDefinition definition,
			final ILanguageConfigurationRegistryManager.EditSession manager) {
		final var langcfg = definition == null ? null : definition.getLanguageConfiguration();
		super.refresh(langcfg);
		if (definition == null) {
			toggleOnEnterButton.setEnabled(false);
			toggleOnEnterButton.setSelection(false);
			toggleIdentRulesButton.setEnabled(false);
			toggleIdentRulesButton.setSelection(false);
			toggleBracketAutoClosingButton.setEnabled(false);
			toggleBracketAutoClosingButton.setSelection(false);
			toggleMatchingPairsButton.setEnabled(false);
			toggleMatchingPairsButton.setSelection(false);
			return;
		}
		toggleOnEnterButton.setSelection(definition.isOnEnterEnabled());
		toggleOnEnterButton.setEnabled(langcfg != null && !langcfg.getOnEnterRules().isEmpty());
		toggleIdentRulesButton.setSelection(definition.isIndentRulesEnabled());
		toggleIdentRulesButton.setEnabled(langcfg != null && langcfg.getIndentationRules() != null);
		toggleBracketAutoClosingButton.setSelection(definition.isBracketAutoClosingEnabled());
		toggleBracketAutoClosingButton.setEnabled(langcfg != null && !langcfg.getAutoClosingPairs().isEmpty());
		toggleMatchingPairsButton.setSelection(definition.isMatchingPairsEnabled());
		toggleMatchingPairsButton.setEnabled(langcfg != null && !langcfg.getSurroundingPairs().isEmpty());
		this.definition = definition;
		this.manager = manager;
	}

	@Override
	protected void createOnEnterRulesInfo(final Composite parent) {
		super.createOnEnterRulesInfo(parent);
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
	protected void createIndentationRulesInfo(final Composite parent) {
		super.createIndentationRulesInfo(parent);
		toggleIdentRulesButton = new Button(parent, SWT.CHECK);
		toggleIdentRulesButton.setText(LanguageConfigurationPreferencesWidget_enableIndentRules);
		toggleIdentRulesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toggleIdentRulesButton.setEnabled(false);
		toggleIdentRulesButton.addSelectionListener(widgetSelectedAdapter(e -> {
			manager.unregisterLanguageConfigurationDefinition(definition);
			definition.setIndentRulesEnabled(toggleIdentRulesButton.getSelection());
			manager.registerLanguageConfigurationDefinition(definition);
		}));
	}

	@Override
	protected void createAutoClosingPairsInfo(final Composite parent) {
		super.createAutoClosingPairsInfo(parent);
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
	protected void createSurroundingPairsInfo(final Composite parent) {
		super.createSurroundingPairsInfo(parent);
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
