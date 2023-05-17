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

import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
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
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.registry.WorkingCopyLanguageConfigurationRegistryManager;
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

	static final String PAGE_ID = "org.eclipse.tm4e.languageconfiguration.preferences.LanguageConfigurationPreferencePage"; //$NON-NLS-1$

	private final ILanguageConfigurationRegistryManager manager = new WorkingCopyLanguageConfigurationRegistryManager(
			LanguageConfigurationRegistryManager.getInstance());

	@Nullable
	private TableViewer definitionViewer;

	@Nullable
	private LanguageConfigurationPreferencesWidget infoWidget;

	public LanguageConfigurationPreferencePage() {
		setDescription(LanguageConfigurationPreferencePage_description);
	}

	@Override
	protected Control createContents(@Nullable final Composite ancestor) {
		final var parent = new Composite(ancestor, SWT.NONE);
		final var layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		final var innerParent = new Composite(parent, SWT.NONE);
		final var innerLayout = new GridLayout();
		innerLayout.numColumns = 2;
		innerLayout.marginHeight = 0;
		innerLayout.marginWidth = 0;
		innerParent.setLayout(innerLayout);
		final var gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		innerParent.setLayoutData(gd);

		createDefinitionsListContent(parent);

		assert definitionViewer != null;
		definitionViewer.setInput(manager);

		final var infoWidget = new LanguageConfigurationPreferencesWidget(parent, SWT.NONE);
		this.infoWidget = infoWidget;

		final var data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		infoWidget.setLayoutData(data);

		Dialog.applyDialogFont(parent);
		innerParent.layout();

		return parent;

	}

	/**
	 * Create grammar list content.
	 */
	private void createDefinitionsListContent(final Composite parent) {
		final var description = new Label(parent, SWT.NONE);
		description.setText(LanguageConfigurationPreferencePage_description2);
		description.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		final var tableComposite = new Composite(parent, SWT.NONE);
		final var data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 360;
		data.heightHint = convertHeightInCharsToPixels(10);
		tableComposite.setLayoutData(data);

		final var columnLayout = new TableColumnLayout();
		tableComposite.setLayout(columnLayout);
		final var table = new Table(tableComposite,
				SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final GC gc = new GC(getShell());
		gc.setFont(JFaceResources.getDialogFont());

		final var viewerComparator = new ColumnViewerComparator();

		final var definitionViewer = new TableViewer(table);
		this.definitionViewer = definitionViewer;

		for (int i = 0; i < 4; i++) {
			final var column = new TableColumn(table, SWT.NONE);
			final String label = switch (i) {
				case 0 -> LanguageConfigurationPreferencePage_contentTypeName;
				case 1 -> LanguageConfigurationPreferencePage_contentTypeId;
				case 2 -> LanguageConfigurationPreferencePage_pluginId;
				case 3 -> LanguageConfigurationPreferencePage_path;
				default -> throw new IllegalArgumentException("Unexpected value: " + i);
			};

			column.setText(label);
			final int minWidth = computeMinimumColumnWidth(gc, label);
			columnLayout.setColumnData(column, new ColumnWeightData(2, minWidth, true));
			column.addSelectionListener(new ColumnSelectionAdapter(column, definitionViewer, i, viewerComparator));

			if (i == 0) {
				// Specify default sorting
				table.setSortColumn(column);
				table.setSortDirection(viewerComparator.getDirection());
			}
		}

		gc.dispose();

		definitionViewer.setLabelProvider(new LanguageConfigurationLabelProvider());
		definitionViewer.setContentProvider(new LanguageConfigurationContentProvider());
		definitionViewer.setComparator(viewerComparator);

		BidiUtils.applyTextDirection(definitionViewer.getControl(), BidiUtils.BTD_DEFAULT);

		final var buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		final var layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		final var definitionNewButton = new Button(buttons, SWT.PUSH);
		definitionNewButton.setText(LanguageConfigurationPreferencePage_new);
		definitionNewButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		definitionNewButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(@Nullable final Event e) {
				add();
			}

			private void add() {
				// Open import wizard for language configurations.
				final var wizard = new LanguageConfigurationImportWizard(false);
				wizard.setRegistryManager(manager);
				final var dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					final var created = wizard.getCreatedDefinition();
					definitionViewer.refresh();
					definitionViewer.setSelection(new StructuredSelection(created));
				}
			}
		});

		final var definitionRemoveButton = new Button(buttons, SWT.PUSH);
		definitionRemoveButton.setText(LanguageConfigurationPreferencePage_remove);
		definitionRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		definitionRemoveButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(@Nullable final Event e) {
				remove();
			}

			private void remove() {
				final var definitions = getSelectedUserDefinitions(definitionViewer);
				if (!definitions.isEmpty()) {
					for (final var definition : definitions) {
						manager.unregisterLanguageConfigurationDefinition(definition);
					}
					definitionViewer.refresh();
				}
			}
		});

		definitionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(@Nullable final SelectionChangedEvent e) {
				final var selection = definitionViewer.getStructuredSelection();
				assert infoWidget != null;
				infoWidget.refresh(null, manager);
				if (selection.isEmpty()) {
					return;
				}
				final var definition = (ILanguageConfigurationDefinition) selection.getFirstElement();
				// Update button
				assert definitionRemoveButton != null;
				definitionRemoveButton.setEnabled(definition.getPluginId() == null);
				selectDefinition(definition);
			}

			private void selectDefinition(final ILanguageConfigurationDefinition definition) {
				assert infoWidget != null;
				infoWidget.refresh(definition, manager);
			}
		});
	}

	private int computeMinimumColumnWidth(final GC gc, final String string) {
		return gc.stringExtent(string).x + 10; // pad 10 to accommodate table header trimmings
	}

	/**
	 * Returns list of selected definitions which was created by the user.
	 *
	 * @return list of selected definitions which was created by the user.
	 */
	private Collection<ILanguageConfigurationDefinition> getSelectedUserDefinitions(
			final TableViewer definitionViewer) {
		final var selection = definitionViewer.getStructuredSelection();
		if (selection.isEmpty()) {
			return Collections.emptyList();
		}
		return ((Collection<ILanguageConfigurationDefinition>) selection.toList()).stream()
				.filter(definition -> definition.getPluginId() == null).toList();
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setTitle(LanguageConfigurationPreferencePage_title);
		}
	}

	@Override
	public boolean performOk() {
		try {
			manager.save();
		} catch (final BackingStoreException ex) {
			LanguageConfigurationPlugin.logError(ex);
		}
		return super.performOk();
	}

	@Override
	public void init(@Nullable final IWorkbench workbench) {

	}
}
