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

import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.AutoClosingPairConditionalTableWidget_notIn;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm4e.languageconfiguration.internal.model.AutoClosingPairConditional;
import org.eclipse.tm4e.languageconfiguration.internal.model.CharacterPair;

final class AutoClosingPairConditionalTableWidget extends CharacterPairsTableWidget {

	AutoClosingPairConditionalTableWidget(final Composite parent) {
		super(parent);
	}

	@Override
	protected void createColumns() {
		super.createColumns();
		createColumn(AutoClosingPairConditionalTableWidget_notIn, 2, 0);
	}

	@Override
	protected @Nullable String getColumnText(final CharacterPair charPair, final int columnIndex) {
		if (columnIndex == 2 && charPair instanceof final AutoClosingPairConditional conditionalPair) {
			return String.join(", ", conditionalPair.notIn);
		}
		return super.getColumnText(charPair, columnIndex);
	}
}
