/**
 * Copyright (c) 2015-2019 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Pierre-Yves B. - Issue #221 NullPointerException when retrieving fileTypes
 */
package org.eclipse.tm4e.ui.internal.widgets;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.ui.internal.TMUIMessages;

/**
 * Widget which display grammar information like name, scope, and file types.
 */
public final class GrammarInfoWidget extends Composite {

	private final Text nameText;
	private final Text scopeNameText;
	private final Text fileTypesText;

	public GrammarInfoWidget(final Composite parent, final int style) {
		super(parent, style);
		final var layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		super.setLayout(layout);
		super.setLayoutData(new GridData(GridData.FILL_BOTH));

		final var container = new Composite(this, SWT.NONE);
		final var containerLayout = new GridLayout(2, false);
		containerLayout.marginHeight = 0;
		containerLayout.marginWidth = 0;
		containerLayout.marginLeft = 0;
		containerLayout.marginRight = 0;
		container.setLayout(containerLayout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		final var grammarNameLabel = new Label(container, SWT.NONE);
		grammarNameLabel.setText(TMUIMessages.GrammarInfoWidget_name_text);
		nameText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final var grammarScopeNameLabel = new Label(container, SWT.NONE);
		grammarScopeNameLabel.setText(TMUIMessages.GrammarInfoWidget_scopeName_text);
		scopeNameText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		scopeNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final var grammarFileTypesLabel = new Label(container, SWT.NONE);
		grammarFileTypesLabel.setText(TMUIMessages.GrammarInfoWidget_fileTypes_text);
		fileTypesText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		//fileTypesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileTypesText.setLayoutData(GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).create());
	}

	public void refresh(final @Nullable IGrammar grammar) {
		if (grammar == null) {
			nameText.setText("");
			scopeNameText.setText("");
			fileTypesText.setText("");
		} else {
			final String name = grammar.getName();
			nameText.setText(name != null ? name : "");
			final String scope = grammar.getScopeName();
			scopeNameText.setText(scope);
			final Collection<String> fileTypes = grammar.getFileTypes();
			final String types = fileTypes.stream().map(Object::toString).collect(Collectors.joining(","));
			fileTypesText.setText(types);
		}
	}

	public Text getGrammarNameText() {
		return nameText;
	}

	public Text getScopeNameText() {
		return scopeNameText;
	}

	public Text getFileTypesText() {
		return fileTypesText;
	}
}
