/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nicolaj Hoess <nicohoess@gmail.com> - Editor templates pref page: Allow to sort by column - https://bugs.eclipse.org/203722
 *     Angelo Zerr <angelo.zerr@gmail.com> - Adapt org.eclipse.ui.texteditor.templates.TemplatePreferencePage for TextMate grammar
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.registry.WorkingCopyGrammarRegistryManager;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.themes.WorkingCopyThemeManager;
import org.eclipse.tm4e.ui.internal.widgets.ColumnSelectionAdapter;
import org.eclipse.tm4e.ui.internal.widgets.ColumnViewerComparator;
import org.eclipse.tm4e.ui.internal.widgets.ContentTypesBindingWidget;
import org.eclipse.tm4e.ui.internal.widgets.GrammarDefinitionContentProvider;
import org.eclipse.tm4e.ui.internal.widgets.GrammarDefinitionLabelProvider;
import org.eclipse.tm4e.ui.internal.widgets.GrammarInfoWidget;
import org.eclipse.tm4e.ui.internal.widgets.TMViewer;
import org.eclipse.tm4e.ui.internal.widgets.ThemeAssociationsWidget;
import org.eclipse.tm4e.ui.internal.wizards.TextMateGrammarImportWizard;
import org.eclipse.tm4e.ui.snippets.ISnippet;
import org.eclipse.tm4e.ui.snippets.ISnippetManager;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A grammar preference page allows configuration of the TextMate grammar It
 * provides controls for adding, removing and changing grammar as well as
 * enablement, default management.
 */
public final class GrammarPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	static final String PAGE_ID = "org.eclipse.tm4e.ui.preferences.GrammarPreferencePage";

	// Managers
	private IGrammarRegistryManager grammarRegistryManager;
	private IThemeManager themeManager;
	private ISnippetManager snippetManager;

	// Grammar list
	private TableViewer grammarViewer;
	private Button grammarNewButton;
	private Button grammarRemoveButton;

	// General tab
	private GrammarInfoWidget grammarInfoWidget;
	// Content type tab
	private ContentTypesBindingWidget contentTypesWidget;
	// Theme associations tab
	private ThemeAssociationsWidget themeAssociationsWidget;
	// Preview
	private TMViewer previewViewer;

	public GrammarPreferencePage() {
		setDescription(TMUIMessages.GrammarPreferencePage_description);
		setGrammarRegistryManager(
				new WorkingCopyGrammarRegistryManager(TMEclipseRegistryPlugin.getGrammarRegistryManager()));
		setThemeManager(new WorkingCopyThemeManager(TMUIPlugin.getThemeManager()));
		setSnippetManager(TMUIPlugin.getSnippetManager());
	}

	/**
	 * Returns the grammar registry manager.
	 *
	 * @return the grammar registry manager.
	 */
	IGrammarRegistryManager getGrammarRegistryManager() {
		return grammarRegistryManager;
	}

	/**
	 * Set the grammar registry manager.
	 *
	 * @param grammarRegistryManager
	 */
	void setGrammarRegistryManager(IGrammarRegistryManager grammarRegistryManager) {
		this.grammarRegistryManager = grammarRegistryManager;
	}

	/**
	 * Returns the theme manager.
	 *
	 * @return the theme manager.
	 */
	IThemeManager getThemeManager() {
		return themeManager;
	}

	/**
	 * Set the theme manager.
	 *
	 * @param themeManager
	 */
	void setThemeManager(IThemeManager themeManager) {
		this.themeManager = themeManager;
	}

	ISnippetManager getSnippetManager() {
		return snippetManager;
	}

	void setSnippetManager(ISnippetManager snippetManager) {
		this.snippetManager = snippetManager;
	}

	@Override
	protected Control createContents(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		Composite innerParent = new Composite(parent, SWT.NONE);
		GridLayout innerLayout = new GridLayout();
		innerLayout.numColumns = 2;
		innerLayout.marginHeight = 0;
		innerLayout.marginWidth = 0;
		innerParent.setLayout(innerLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		innerParent.setLayoutData(gd);

		createGrammarListContent(innerParent);
		createGrammarDetailContent(innerParent);

		previewViewer = doCreateViewer(innerParent);
		grammarViewer.setInput(grammarRegistryManager);

		updateButtons();
		Dialog.applyDialogFont(parent);
		innerParent.layout();

		return parent;
	}

	/**
	 * Create grammar list content.
	 *
	 * @param parent
	 */
	private void createGrammarListContent(Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 360;
		data.heightHint = convertHeightInCharsToPixels(10);
		tableComposite.setLayoutData(data);

		TableColumnLayout columnLayout = new TableColumnLayout();
		tableComposite.setLayout(columnLayout);
		Table table = new Table(tableComposite,
				SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GC gc = new GC(getShell());
		gc.setFont(JFaceResources.getDialogFont());

		ColumnViewerComparator viewerComparator = new ColumnViewerComparator();

		grammarViewer = new TableViewer(table);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(TMUIMessages.GrammarPreferencePage_column_scopeName);
		int minWidth = computeMinimumColumnWidth(gc, TMUIMessages.GrammarPreferencePage_column_scopeName);
		columnLayout.setColumnData(column1, new ColumnWeightData(2, minWidth, true));
		column1.addSelectionListener(new ColumnSelectionAdapter(column1, grammarViewer, 0, viewerComparator));

		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(TMUIMessages.GrammarPreferencePage_column_path);
		minWidth = computeMinimumColumnWidth(gc, TMUIMessages.GrammarPreferencePage_column_path);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));
		column2.addSelectionListener(new ColumnSelectionAdapter(column2, grammarViewer, 1, viewerComparator));

		TableColumn column3 = new TableColumn(table, SWT.NONE);
		column3.setText(TMUIMessages.GrammarPreferencePage_column_pluginId);
		minWidth = computeMinimumColumnWidth(gc, TMUIMessages.GrammarPreferencePage_column_pluginId);
		columnLayout.setColumnData(column3, new ColumnWeightData(2, minWidth, true));
		column3.addSelectionListener(new ColumnSelectionAdapter(column3, grammarViewer, 2, viewerComparator));

		gc.dispose();

		grammarViewer.setLabelProvider(new GrammarDefinitionLabelProvider());
		grammarViewer.setContentProvider(new GrammarDefinitionContentProvider());
		grammarViewer.setComparator(viewerComparator);

		grammarViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				IStructuredSelection selection = grammarViewer.getStructuredSelection();
				if (selection.isEmpty()) {
					return;
				}
				IGrammarDefinition definition = (IGrammarDefinition) (selection).getFirstElement();
				// Update button
				grammarRemoveButton.setEnabled(definition.getPluginId() == null);
				themeAssociationsWidget.getNewButton().setEnabled(false);
				themeAssociationsWidget.getRemoveButton().setEnabled(false);
				// Select grammar
				selectGrammar(definition);
			}

			private void selectGrammar(IGrammarDefinition definition) {
				String scopeName = definition.getScopeName();
				// Fill "General" tab
				fillGeneralTab(scopeName);
				// Fill "Content type" tab
				fillContentTypeTab(scopeName);
				// Fill "Theme" tab
				IThemeAssociation selectedAssociation = fillThemeTab(definition);
				// Fill preview
				fillPreview(scopeName, selectedAssociation);
			}

			private void fillGeneralTab(String scopeName) {
				IGrammar grammar = grammarRegistryManager.getGrammarForScope(scopeName);
				grammarInfoWidget.refresh(grammar);
			}

			private void fillContentTypeTab(String scopeName) {
				// Load the content type binding for the given grammar
				contentTypesWidget.setInput(grammarRegistryManager.getContentTypesForScope(scopeName));
			}

			private IThemeAssociation fillThemeTab(IGrammarDefinition definition) {
				IThemeAssociation selectedAssociation = null;
				IStructuredSelection oldSelection = themeAssociationsWidget.getSelection();
				// Load the theme associations for the given grammar
				IThemeAssociation[] themeAssociations = themeAssociationsWidget.setGrammarDefinition(definition);
				// Try to keep selection
				if (!oldSelection.isEmpty()
						&& Arrays.asList(themeAssociations).contains(oldSelection.getFirstElement())) {
					selectedAssociation = (IThemeAssociation) oldSelection.getFirstElement();
					themeAssociationsWidget.setSelection(oldSelection);
				} else {
					selectedAssociation = themeAssociations != null && themeAssociations.length > 0
							? themeAssociations[0]
							: null;
					if (selectedAssociation != null) {
						themeAssociationsWidget.setSelection(new StructuredSelection(selectedAssociation));
					}
				}
				return selectedAssociation;
			}

			private void fillPreview(String scopeName, IThemeAssociation selectedAssociation) {
				// Preview the grammar
				IGrammar grammar = grammarRegistryManager.getGrammarForScope(scopeName);
				if (selectedAssociation != null) {
					setPreviewTheme(selectedAssociation.getThemeId());
				}
				previewViewer.setGrammar(grammar);

				// Snippet
				ISnippet[] snippets = snippetManager.getSnippets(scopeName);
				if (snippets == null || snippets.length == 0) {
					previewViewer.setText("");
				} else {
					// TODO: manage list of snippet for the given scope.
					previewViewer.setText(snippets[0].getContent());
				}
			}

		});

		// Specify default sorting
		table.setSortColumn(column1);
		table.setSortDirection(viewerComparator.getDirection());

		BidiUtils.applyTextDirection(grammarViewer.getControl(), BidiUtils.BTD_DEFAULT);

		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		grammarNewButton = new Button(buttons, SWT.PUSH);
		grammarNewButton.setText(TMUIMessages.Button_new);
		grammarNewButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grammarNewButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				add();
			}

			private void add() {
				// Open import wizard for TextMate grammar.
				TextMateGrammarImportWizard wizard = new TextMateGrammarImportWizard(false);
				wizard.setGrammarRegistryManager(grammarRegistryManager);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					// User grammar was saved, refresh the list of grammar and
					// select the created grammar.
					IGrammarDefinition created = wizard.getCreatedDefinition();
					grammarViewer.refresh();
					grammarViewer.setSelection(new StructuredSelection(created));
				}
			}
		});

		grammarRemoveButton = new Button(buttons, SWT.PUSH);
		grammarRemoveButton.setText(TMUIMessages.Button_remove);
		grammarRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grammarRemoveButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				remove();
			}

			private void remove() {
			    IGrammarDefinition definition = (IGrammarDefinition) ((IStructuredSelection) grammarViewer.getSelection()).getFirstElement();
			    grammarRegistryManager.unregisterGrammarDefinition(definition);
			    grammarViewer.refresh();
			}
		});
	}

	/**
	 * Create detail grammar content which is filled when a grammar is selected in
	 * the grammar list.
	 *
	 * @param parent
	 */
	private void createGrammarDetailContent(Composite parent) {
		TabFolder folder = new TabFolder(parent, SWT.NONE);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		folder.setLayoutData(gd);

		createGeneralTab(folder);
		createContentTypeTab(folder);
		createThemeTab(folder);
		createInjectionTab(folder);
	}

	/**
	 * Create "General" tab
	 *
	 * @param folder
	 */
	private void createGeneralTab(TabFolder folder) {
		TabItem tab = new TabItem(folder, SWT.NONE);
		tab.setText(TMUIMessages.GrammarPreferencePage_tab_general_text);

		Composite parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		grammarInfoWidget = new GrammarInfoWidget(parent, SWT.NONE);
		grammarInfoWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tab.setControl(parent);

	}

	/**
	 * Create "Content type" tab
	 *
	 * @param folder
	 */
	private void createContentTypeTab(TabFolder folder) {
		TabItem tab = new TabItem(folder, SWT.NONE);
		tab.setText(TMUIMessages.GrammarPreferencePage_tab_contentType_text);

		Composite parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		contentTypesWidget = new ContentTypesBindingWidget(parent, SWT.NONE);
		contentTypesWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tab.setControl(parent);
	}

	/**
	 * Create "Theme" tab
	 *
	 * @param folder
	 */
	private void createThemeTab(TabFolder folder) {
		TabItem tab = new TabItem(folder, SWT.NONE);
		tab.setText(TMUIMessages.GrammarPreferencePage_tab_theme_text);

		Composite parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		themeAssociationsWidget = new ThemeAssociationsWidget(themeManager, parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		themeAssociationsWidget.setLayoutData(data);
		themeAssociationsWidget.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				IThemeAssociation association = (IThemeAssociation) ((IStructuredSelection) e.getSelection())
						.getFirstElement();
				selectTheme(association);
			}

			private void selectTheme(IThemeAssociation association) {
				themeAssociationsWidget.getNewButton()
						.setEnabled(association != null /* && association.getPluginId() == null */);
				themeAssociationsWidget.getRemoveButton()
						.setEnabled(association != null /* && association.getPluginId() == null */);
				if (association != null) {
					setPreviewTheme(association.getThemeId());
				}
			}
		});

		tab.setControl(parent);
	}

	private void setPreviewTheme(String themeId) {
		ITheme theme = themeManager.getThemeById(themeId);
		if (theme != null) {
			previewViewer.setTheme(theme);
		}
	}

	/**
	 * Create "Injection" tab
	 *
	 * @param folder
	 */
	private void createInjectionTab(TabFolder folder) {
		TabItem tab = new TabItem(folder, SWT.NONE);
		tab.setText(TMUIMessages.GrammarPreferencePage_tab_injection_text);

		Composite parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// TODO: manage UI injection

		tab.setControl(parent);
	}

	private int computeMinimumColumnWidth(GC gc, String string) {
		return gc.stringExtent(string).x + 10; // pad 10 to accommodate table
												// header trimmings
	}

	private void updateButtons() {
		grammarRemoveButton.setEnabled(false);

	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			setTitle(TMUIMessages.GrammarPreferencePage_title);
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	private TMViewer doCreateViewer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(TMUIMessages.GrammarPreferencePage_preview);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		TMViewer viewer = createViewer(parent);

		// Don't set caret to 'null' as this causes
		// https://bugs.eclipse.org/293263
		// viewer.getTextWidget().setCaret(null);

		Control control = viewer.getControl();
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		data.heightHint = convertHeightInCharsToPixels(5);
		control.setLayoutData(data);

		return viewer;
	}

	/**
	 * Creates, configures and returns a source viewer to present the template
	 * pattern on the preference page. Clients may override to provide a custom
	 * source viewer featuring e.g. syntax coloring.
	 *
	 * @param parent
	 *            the parent control
	 * @return a configured source viewer
	 */
	private TMViewer createViewer(Composite parent) {
		return new TMViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	@Override
	public boolean performOk() {
		try {
			// Save the working copy if there are some changed.
			grammarRegistryManager.save();
			themeManager.save();
		} catch (BackingStoreException e) {
			e.printStackTrace();
			return false;
		}
		return super.performOk();
	}
}
