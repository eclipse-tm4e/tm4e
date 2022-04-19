/**
 *  Copyright (c) 2015-2019 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Pierre-Yves B. - Issue #221 NullPointerException when retrieving fileTypes
 */
package org.eclipse.tm4e.ui.internal.widgets;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.ui.internal.TMUIMessages;

/**
 * 
 * Widget which display grammar information like name, scope, and file types.
 */
public final class GrammarInfoWidget extends Composite {

	private Text nameText;
	private Text scopeNameText;
	private Text fileTypesText;

	public GrammarInfoWidget(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		super.setLayout(layout);
		super.setLayoutData(new GridData(GridData.FILL_BOTH));
		createUI(this);
	}

	private void createUI(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label grammarNameLabel = new Label(parent, SWT.NONE);
		grammarNameLabel.setText(TMUIMessages.GrammarInfoWidget_name_text);
		nameText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label grammarScopeNameLabel = new Label(parent, SWT.NONE);
		grammarScopeNameLabel.setText(TMUIMessages.GrammarInfoWidget_scopeName_text);
		scopeNameText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		scopeNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label grammarFileTypesLabel = new Label(parent, SWT.NONE);
		grammarFileTypesLabel.setText(TMUIMessages.GrammarInfoWidget_fileTypes_text);
		fileTypesText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		fileTypesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	public void refresh(IGrammar grammar) {
		if (grammar == null) {
			nameText.setText("");
			scopeNameText.setText("");
			fileTypesText.setText("");
		} else {
			String name = grammar.getName();
			nameText.setText(name != null ? name : "");
			String scope = grammar.getScopeName();
			scopeNameText.setText(scope != null ? scope : "");
			Collection<String> fileTypes = grammar.getFileTypes();
			String types = fileTypes.stream().map(Object::toString).collect(Collectors.joining(","));
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
