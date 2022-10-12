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

import java.nio.file.Paths;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
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
import org.eclipse.tm4e.core.registry.IGrammarSource;
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
final class SelectGrammarWizardPage extends AbstractWizardPage {

	private static final String PAGE_NAME = SelectGrammarWizardPage.class.getName();

	private static final String[] TEXTMATE_EXTENSIONS = {
			"*.tmLanguage",
			"*.json",
			"*.YAML-tmLanguage",
			"*.yaml",
			"*.yml" };

	private Text grammarFileText = lazyNonNull();
	private GrammarInfoWidget grammarInfoWidget = lazyNonNull();

	// private ContentTypesBindingWidget contentTypesWidget;

	protected SelectGrammarWizardPage() {
		super(PAGE_NAME);
		super.setTitle(TMUIMessages.SelectGrammarWizardPage_title);
		super.setDescription(TMUIMessages.SelectGrammarWizardPage_description);
	}

	@Override
	protected void createBody(final Composite ancestor) {
		final var parent = new Composite(ancestor, SWT.NONE);
		parent.setFont(parent.getFont());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parent.setLayout(new GridLayout(2, false));

		// Text Field
		grammarFileText = createText(parent, TMUIMessages.SelectGrammarWizardPage_file_label);
		grammarFileText.addListener(SWT.Modify, this);

		// Buttons
		final var buttons = new Composite(parent, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		final var gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		buttons.setLayoutData(gd);

		final var browseFileSystemButton = new Button(buttons, SWT.NONE);
		browseFileSystemButton.setText(TMUIMessages.Button_browse_FileSystem);
		browseFileSystemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(@Nullable final SelectionEvent e) {
				final var dialog = new FileDialog(parent.getShell());
				dialog.setFilterExtensions(TEXTMATE_EXTENSIONS);
				dialog.setFilterPath(grammarFileText.getText());
				final String result = dialog.open();
				if (result != null && !result.isEmpty()) {
					grammarFileText.setText(result);
				}
			}
		});

		final var browseWorkspaceButton = new Button(buttons, SWT.NONE);
		browseWorkspaceButton.setText(TMUIMessages.Button_browse_Workspace);
		browseWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(@Nullable final SelectionEvent e) {
				// TODO
			}
		});

		final var data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		grammarInfoWidget = new GrammarInfoWidget(parent, SWT.NONE);
		grammarInfoWidget.setLayoutData(data);
	}

	private Text createText(final Composite parent, final String s) {
		final var label = new Label(parent, SWT.NONE);
		label.setText(s);

		final var text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}

	@Override
	protected void initializeDefaultValues() {
		setPageComplete(false);
	}

	@Nullable
	@Override
	protected IStatus validatePage(final Event event) {
		grammarInfoWidget.refresh(null);
		final String path = grammarFileText.getText();
		if (path.isEmpty()) {
			return new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID,
					TMUIMessages.SelectGrammarWizardPage_file_error_required);
		}
		final var registry = new Registry();
		try {
			final IGrammar grammar = registry.addGrammar(IGrammarSource.fromFile(Paths.get(path)));
			grammarInfoWidget.refresh(grammar);
		} catch (final Exception e) {
			return new Status(IStatus.ERROR, TMUIPlugin.PLUGIN_ID,
					NLS.bind(TMUIMessages.SelectGrammarWizardPage_file_error_load, e.getMessage()), e);
		}
		return null;
	}

	IGrammarDefinition getGrammarDefinition() {
		return new GrammarDefinition(grammarInfoWidget.getScopeNameText().getText(), grammarFileText.getText());
	}
}
