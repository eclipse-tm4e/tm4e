/**
 * Copyright (c) 2015-2018 Angelo ZERR and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 * Lucas Bullen (Red Hat Inc.) - configuration viewing and editing
 */
package org.eclipse.tm4e.languageconfiguration.internal.preferences;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.WorkingCopyLanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.widgets.ColumnSelectionAdapter;
import org.eclipse.tm4e.languageconfiguration.internal.widgets.ColumnViewerComparator;
import org.eclipse.tm4e.languageconfiguration.internal.widgets.LanguageConfigurationPreferencesWidget;
import org.eclipse.tm4e.languageconfiguration.internal.wizards.LanguageConfigurationImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A language configuration preference page allows configuration of the language
 * configuration It provides controls for adding, removing and changing language
 * configuration as well as enablement, default management.
 */
public final class LanguageConfigurationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	final static String PAGE_ID = "org.eclipse.tm4e.languageconfiguration.preferences.LanguageConfigurationPreferencePage"; //$NON-NLS-1$

	private final ILanguageConfigurationRegistryManager manager = new WorkingCopyLanguageConfigurationRegistryManager(
			LanguageConfigurationRegistryManager.getInstance());

	private TableViewer definitionViewer;

	private Button definitionNewButton;
	private Button definitionRemoveButton;
	private LanguageConfigurationPreferencesWidget infoWidget;

	public LanguageConfigurationPreferencePage() {
		setDescription(LanguageConfigurationMessages.LanguageConfigurationPreferencePage_description);
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

		createDefinitionsListContent(parent);

		definitionViewer.setInput(manager);

		infoWidget = new LanguageConfigurationPreferencesWidget(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		infoWidget.setLayoutData(data);

		Dialog.applyDialogFont(parent);
		innerParent.layout();

		return parent;

	}

	/**
	 * Create grammar list content.
	 *
	 * @param parent
	 */
	private void createDefinitionsListContent(Composite parent) {
		Label description = new Label(parent, SWT.NONE);
		description.setText(LanguageConfigurationMessages.LanguageConfigurationPreferencePage_description2);
		description.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 360;
		data.heightHint = convertHeightInCharsToPixels(10);
		tableComposite.setLayoutData(data);

		TableColumnLayout columnLayout = new TableColumnLayout();
		tableComposite.setLayout(columnLayout);
		Table table = new Table(tableComposite,
				SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GC gc = new GC(getShell());
		gc.setFont(JFaceResources.getDialogFont());

		ColumnViewerComparator viewerComparator = new ColumnViewerComparator();

		definitionViewer = new TableViewer(table);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(LanguageConfigurationMessages.LanguageConfigurationPreferencePage_contentType);
		int minWidth = computeMinimumColumnWidth(gc,
				LanguageConfigurationMessages.LanguageConfigurationPreferencePage_contentType);
		columnLayout.setColumnData(column1, new ColumnWeightData(2, minWidth, true));
		column1.addSelectionListener(new ColumnSelectionAdapter(column1, definitionViewer, 0, viewerComparator));

		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(LanguageConfigurationMessages.LanguageConfigurationPreferencePage_path);
		minWidth = computeMinimumColumnWidth(gc,
				LanguageConfigurationMessages.LanguageConfigurationPreferencePage_path);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));
		column2.addSelectionListener(new ColumnSelectionAdapter(column2, definitionViewer, 1, viewerComparator));

		TableColumn column3 = new TableColumn(table, SWT.NONE);
		column3.setText(LanguageConfigurationMessages.LanguageConfigurationPreferencePage_pluginId);
		minWidth = computeMinimumColumnWidth(gc,
				LanguageConfigurationMessages.LanguageConfigurationPreferencePage_pluginId);
		columnLayout.setColumnData(column3, new ColumnWeightData(2, minWidth, true));
		column3.addSelectionListener(new ColumnSelectionAdapter(column3, definitionViewer, 2, viewerComparator));

		gc.dispose();

		definitionViewer.setLabelProvider(new LanguageConfigurationLabelProvider());
		definitionViewer.setContentProvider(new LanguageConfigurationContentProvider());
		definitionViewer.setComparator(viewerComparator);

		definitionViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				IStructuredSelection selection = definitionViewer.getStructuredSelection();
				infoWidget.refresh(null, manager);
				if (selection.isEmpty()) {
					return;
				}
				ILanguageConfigurationDefinition definition = (ILanguageConfigurationDefinition) (selection)
						.getFirstElement();
				// Update button
				definitionRemoveButton.setEnabled(definition.getPluginId() == null);
				selectDefinition(definition);
			}

			private void selectDefinition(ILanguageConfigurationDefinition definition) {
				infoWidget.refresh(definition, manager);
			}
		});

		// Specify default sorting
		table.setSortColumn(column1);
		table.setSortDirection(viewerComparator.getDirection());

		BidiUtils.applyTextDirection(definitionViewer.getControl(), BidiUtils.BTD_DEFAULT);

		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		definitionNewButton = new Button(buttons, SWT.PUSH);
		definitionNewButton.setText(LanguageConfigurationMessages.LanguageConfigurationPreferencePage_new);
		definitionNewButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		definitionNewButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				add();
			}

			private void add() {
				// Open import wizard for language configurations.
				LanguageConfigurationImportWizard wizard = new LanguageConfigurationImportWizard(false);
				wizard.setRegistryManager(manager);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					ILanguageConfigurationDefinition created = wizard.getCreatedDefinition();
					definitionViewer.refresh();
					definitionViewer.setSelection(new StructuredSelection(created));
				}
			}
		});

		definitionRemoveButton = new Button(buttons, SWT.PUSH);
		definitionRemoveButton.setText(LanguageConfigurationMessages.LanguageConfigurationPreferencePage_remove);
		definitionRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		definitionRemoveButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				remove();
			}

			private void remove() {
				Collection<ILanguageConfigurationDefinition> definitions = getSelectedUserDefinitions();
				if (!definitions.isEmpty()) {
					for (ILanguageConfigurationDefinition definition : definitions) {
						manager.unregisterLanguageConfigurationDefinition(definition);
					}
					definitionViewer.refresh();
				}
			}
		});
	}

	private int computeMinimumColumnWidth(GC gc, String string) {
		return gc.stringExtent(string).x + 10; // pad 10 to accommodate table header trimmings
	}

	/**
	 * Returns list of selected definitions which was created by the user.
	 *
	 * @return list of selected definitions which was created by the user.
	 */
	private Collection<ILanguageConfigurationDefinition> getSelectedUserDefinitions() {
		IStructuredSelection selection = definitionViewer.getStructuredSelection();
		if (selection.isEmpty()) {
			return Collections.emptyList();
		}
		return ((Collection<ILanguageConfigurationDefinition>) selection.toList()).stream()
				.filter(definition -> definition.getPluginId() == null).collect(Collectors.toList());
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setTitle(LanguageConfigurationMessages.LanguageConfigurationPreferencePage_title);
		}
	}

	@Override
	public boolean performOk() {
		try {
			manager.save();
		} catch (BackingStoreException e) {
			// TODO: Log
		}
		return super.performOk();
	}

	@Override
	public void init(IWorkbench workbench) {

	}
}
