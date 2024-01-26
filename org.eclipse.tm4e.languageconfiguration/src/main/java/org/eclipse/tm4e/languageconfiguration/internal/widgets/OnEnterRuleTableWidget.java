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
 * Sebastian Thomschke - refactored to extend AbstractTableWidget
 */
package org.eclipse.tm4e.languageconfiguration.internal.widgets;

import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm4e.languageconfiguration.internal.model.EnterAction;
import org.eclipse.tm4e.languageconfiguration.internal.model.OnEnterRule;

final class OnEnterRuleTableWidget extends AbstractTableWidget<OnEnterRule> {

	OnEnterRuleTableWidget(final Composite parent) {
		super(parent);
	}

	@Override
	protected void createColumns() {
		createColumn(OnEnterRuleTableWidget_beforeText, 2, 100, true);
		createColumn(OnEnterRuleTableWidget_afterText, 2, 100, true);
		createColumn(OnEnterRuleTableWidget_previousLineText, 2, 100, true);
		createColumn(OnEnterRuleTableWidget_indentAction, 1, 0, true);
		createColumn(OnEnterRuleTableWidget_appendText, 1, 0, true);
		createColumn(OnEnterRuleTableWidget_removeText, 1, 0, true);
	}

	@Override
	protected String getColumnText(OnEnterRule rule, int columnIndex) {
		final EnterAction action = rule.action;

		return switch (columnIndex) {
			case 0 -> rule.beforeText.pattern();
			case 1 -> rule.afterText != null ? rule.afterText.pattern() : "";
			case 2 -> rule.previousLineText != null ? rule.previousLineText.pattern() : "";
			case 3 -> action.indentAction.toString();
			case 4 -> action.appendText != null ? action.appendText : "";
			case 5 -> action.removeText != null ? action.removeText.toString() : "";
			default -> "";
		};
	}
}
