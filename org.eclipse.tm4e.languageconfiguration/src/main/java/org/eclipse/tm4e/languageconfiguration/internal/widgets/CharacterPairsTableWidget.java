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
import org.eclipse.tm4e.languageconfiguration.internal.model.CharacterPair;

class CharacterPairsTableWidget extends AbstractTableWidget<CharacterPair> {

	CharacterPairsTableWidget(final Composite parent) {
		super(parent);
	}

	@Override
	protected String getColumnText(CharacterPair charPair, int columnIndex) {
		return switch (columnIndex) {
			case 0 -> charPair.open;
			case 1 -> charPair.close;
			default -> "";
		};
	}

	@Override
	protected void createColumns() {
		createColumn(CharacterPairsTableWidget_start, 1, 0, true);
		createColumn(CharacterPairsTableWidget_end, 1, 0, true);
	}
}
