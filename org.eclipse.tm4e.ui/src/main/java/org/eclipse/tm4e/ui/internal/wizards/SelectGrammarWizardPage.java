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

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.Registry;
import org.eclipse.tm4e.registry.GrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.widgets.GrammarInfoWidget;

/**
 * Wizard page to select a textMate grammar file and register it in the grammar
 * registry.
 *
 */
public class SelectGrammarWizardPage extends AbstractWizardPage {

	private static final String PAGE_NAME = SelectGrammarWizardPage.class.getName();

	protected static final String[] TEXTMATE_EXTENSIONS = {"*.tmLanguage","*.json"};

	private Button browseFileSystemButton;
	private Button browseWorkspaceButton;

	private Text grammarFileText;

	private GrammarInfoWidget grammarInfoWidget;

	// private ContentTypesBindingWidget contentTypesWidget;

	protected SelectGrammarWizardPage() {
		super(PAGE_NAME);
		super.setTitle(TMUIMessages.SelectGrammarWizardPage_title);
		super.setDescription(TMUIMessages.SelectGrammarWizardPage_description);
	}

	@Override
	protected void createBody(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		parent.setFont(parent.getFont());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parent.setLayout(new GridLayout(2, false));

		// Text Field
		grammarFileText = createText(parent, TMUIMessages.SelectGrammarWizardPage_file_label);
		grammarFileText.addListener(SWT.Modify, this);

		// Buttons
		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		buttons.setLayoutData(gd);

		browseFileSystemButton = new Button(buttons, SWT.NONE);
		browseFileSystemButton.setText(TMUIMessages.Button_browse_FileSystem);
		browseFileSystemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setFilterExtensions(TEXTMATE_EXTENSIONS);
				dialog.setFilterPath(grammarFileText.getText());
				String result = dialog.open();
				if (result != null && result.length() > 0) {
					grammarFileText.setText(result);
				}
			}
		});

		browseWorkspaceButton = new Button(buttons, SWT.NONE);
		browseWorkspaceButton.setText(TMUIMessages.Button_browse_Workspace);
		browseWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO
			}
		});
		
		grammarInfoWidget = new GrammarInfoWidget(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		grammarInfoWidget.setLayoutData(data);
	}

	private Text createText(Composite parent, String s) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(s);

		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}

	@Override
	protected void initializeDefaultValues() {
		setPageComplete(false);
	}

	@Override
	protected IStatus validatePage(Event event) {
		grammarInfoWidget.refresh(null);
		String path = grammarFileText.getText();
		if (path.length() == 0) {
			return new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID,
					TMUIMessages.SelectGrammarWizardPage_file_error_required);
		}
		File f = new File(path);
		Registry registry = new Registry();
		try {
			IGrammar grammar = registry.loadGrammarFromPathSync(f.getName(), new FileInputStream(f));
			if (grammar == null || grammar.getScopeName() == null) {
				return new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID,
						TMUIMessages.SelectGrammarWizardPage_file_error_invalid);
			}
			grammarInfoWidget.refresh(grammar);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID,
					NLS.bind(TMUIMessages.SelectGrammarWizardPage_file_error_load, e.getMessage()), e);
		}
		return null;
	}

	public IGrammarDefinition getGrammarDefinition() {
		return new GrammarDefinition(grammarInfoWidget.getScopeNameText().getText(), grammarFileText.getText());
	}

}
