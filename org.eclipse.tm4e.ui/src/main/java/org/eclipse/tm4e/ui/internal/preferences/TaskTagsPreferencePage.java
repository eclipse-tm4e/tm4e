/*******************************************************************************
 * Copyright (c) 2023 Vegard IT GmbH and others.
 * This program and the Faccompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig.*;
import org.eclipse.tm4e.ui.internal.utils.MarkerUtils;
import org.eclipse.tm4e.ui.internal.widgets.ColumnSelectionAdapter;
import org.eclipse.tm4e.ui.internal.widgets.ColumnViewerComparator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Task Tags preferences page.
 */
public final class TaskTagsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	static final String PAGE_ID = "org.eclipse.tm4e.ui.preferences.TaskTagsPreferencePage";

	private static final class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public @Nullable Image getColumnImage(final @Nullable Object element, final int columnIndex) {
			return null;
		}

		@Override
		public @Nullable String getText(final @Nullable Object element) {
			return getColumnText(element, 0);
		}

		@Override
		public @Nullable String getColumnText(final @Nullable Object element, final int columnIndex) {
			if (element == null)
				return "";

			final MarkerConfig item = (MarkerConfig) element;
			return switch (columnIndex) {
				case 0 -> item.tag;
				case 1 -> item.type.name().charAt(0) + item.type.name().substring(1).toLowerCase();
				case 2 -> switch (item.type) {
					case PROBLEM -> item.asProblemMarkerConfig().severity.toString();
					case TASK -> item.asTaskMarkerConfig().priority.toString();
				};
				default -> "";
			};
		}
	}

	private final class MarkerConfigEditDialog extends TitleAreaDialog {

		@Nullable
		MarkerConfig markerConfig;

		Text txtTag = lazyNonNull();
		Combo cmbType = lazyNonNull();
		Label lblLevel = lazyNonNull();
		Combo cmbLevel = lazyNonNull();

		MarkerConfigEditDialog(final Shell parentShell, final @Nullable MarkerConfig markerConfig) {
			super(parentShell);
			this.markerConfig = markerConfig;
		}

		@Override
		public void create() {
			super.create();
			if (markerConfig == null) {
				getShell().setText(TMUIMessages.TaskTagsPreferencePage_addTagDialog_windowTitle);
				setTitle(TMUIMessages.TaskTagsPreferencePage_addTagDialog_header);
				setMessage(TMUIMessages.TaskTagsPreferencePage_addTagDialog_message, IMessageProvider.INFORMATION);
			} else {
				getShell().setText(TMUIMessages.TaskTagsPreferencePage_editTagDialog_windowTitle);
				setTitle(TMUIMessages.TaskTagsPreferencePage_editTagDialog_header);
				setMessage(TMUIMessages.TaskTagsPreferencePage_editTagDialog_message, IMessageProvider.INFORMATION);
			}
			validateInput(null);
		}

		@Override
		protected void okPressed() {
			markerConfig = switch (Type.valueOf(cmbType.getText())) {
				case PROBLEM -> new ProblemMarkerConfig(txtTag.getText(), ProblemSeverity.valueOf(cmbLevel.getText()));
				case TASK -> new TaskMarkerConfig(txtTag.getText(), TaskPriority.valueOf(cmbLevel.getText()));
			};
			super.okPressed();
		}

		void validateInput(@SuppressWarnings("unused") final @Nullable Event e) {
			var btn = getButton(IDialogConstants.OK_ID);
			if (btn == null)
				return;
			btn.setEnabled(!txtTag.getText().isBlank() && cmbType.getSelectionIndex() > -1 && cmbLevel.getSelectionIndex() > -1);
		}

		@Override
		protected Control createDialogArea(final @Nullable Composite parent) {
			final var area = (Composite) super.createDialogArea(parent);
			final var container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			final var layout = new GridLayout(2, false);
			container.setLayout(layout);
			createTagText(container);
			createTypeCombo(container);
			createLevel(container);

			final var markerConfig = this.markerConfig;
			if (markerConfig != null) {
				txtTag.setText(markerConfig.tag);
				cmbType.setText(markerConfig.type.name());
				cmbLevel.setText(switch (markerConfig.type) {
					case PROBLEM -> markerConfig.asProblemMarkerConfig().severity.name();
					case TASK -> markerConfig.asTaskMarkerConfig().priority.name();
				});
			} else {
				cmbType.setText(MarkerConfig.Type.TASK.name());
				cmbLevel.setText(TaskPriority.NORMAL.name());
			}

			return area;
		}

		void createTagText(final Composite parent) {
			final var label = new Label(parent, SWT.NONE);
			label.setText(TMUIMessages.TaskTagsPreferencePage_column_tag);
			txtTag = new Text(parent, SWT.BORDER);
			final var layoutData = new GridData();
			layoutData.grabExcessHorizontalSpace = true;
			layoutData.horizontalAlignment = GridData.FILL;
			txtTag.setLayoutData(layoutData);
			txtTag.addListener(SWT.Modify, this::validateInput);
		}

		void createTypeCombo(final Composite parent) {
			final var label = new Label(parent, SWT.NONE);
			label.setText(TMUIMessages.TaskTagsPreferencePage_column_type);
			cmbType = new Combo(parent, SWT.READ_ONLY);
			cmbType.setItems(Stream.of(MarkerConfig.Type.values()).map(Enum::name).toArray(String[]::new));
			cmbType.addListener(SWT.Modify, (final @Nullable Event e) -> {
				if (!cmbType.getText().isBlank())
					switch (MarkerConfig.Type.valueOf(cmbType.getText())) {
						case PROBLEM:
							lblLevel.setText("Severity");
							cmbLevel.setItems(Stream.of(ProblemSeverity.values()).map(Enum::name).toArray(String[]::new));
							break;
						case TASK:
							lblLevel.setText("Priority");
							cmbLevel.setItems(Stream.of(TaskPriority.values()).map(Enum::name).toArray(String[]::new));
							break;
					}
				validateInput(null);
			});
		}

		void createLevel(final Composite parent) {
			lblLevel = new Label(parent, SWT.NONE);
			final var layoutData = new GridData();
			layoutData.widthHint = computeMinimumColumnWidth("1234567890");
			layoutData.horizontalAlignment = GridData.FILL;
			lblLevel.setLayoutData(layoutData);
			cmbLevel = new Combo(parent, SWT.READ_ONLY);
			cmbLevel.addListener(SWT.Modify, this::validateInput);
		}

		@Override
		public boolean isHelpAvailable() {
			return false;
		}
	}

	private final Set<MarkerConfig> markerConfigs = PreferenceHelper.loadMarkerConfigs();
	private TableViewer markerConfigsTable = lazyNonNull();

	public TaskTagsPreferencePage() {
		setDescription(TMUIMessages.TaskTagsPreferencePage_description);
	}

	@Override
	protected Control createContents(final @Nullable Composite ancestor) {
		final var parent = new Composite(ancestor, SWT.NONE);
		final var layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		final var table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		markerConfigsTable = new TableViewer(table);
		markerConfigsTable.setLabelProvider(new TableLabelProvider());
		markerConfigsTable.setContentProvider(new IStructuredContentProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(final @Nullable Object inputElement) {
				return inputElement == null ? new MarkerConfig[0] : ((Collection<MarkerConfig>) inputElement).toArray();
			}
		});

		final var tableColumnSorter = new ColumnViewerComparator();
		markerConfigsTable.setComparator(tableColumnSorter);

		final var column1 = new TableColumn(table, SWT.NONE);
		column1.setText(TMUIMessages.TaskTagsPreferencePage_column_tag);
		column1.setWidth(computeMinimumColumnWidth("1234567890"));
		column1.addSelectionListener(new ColumnSelectionAdapter(column1, markerConfigsTable, 0, tableColumnSorter));

		final var column2 = new TableColumn(table, SWT.NONE);
		column2.setText(TMUIMessages.TaskTagsPreferencePage_column_type);
		column2.setWidth(column1.getWidth());
		column2.addSelectionListener(new ColumnSelectionAdapter(column2, markerConfigsTable, 1, tableColumnSorter));

		final var column3 = new TableColumn(table, SWT.NONE);
		column3.setText(TMUIMessages.TaskTagsPreferencePage_column_level);
		column3.setWidth(column1.getWidth());
		column3.addSelectionListener(new ColumnSelectionAdapter(column3, markerConfigsTable, 2, tableColumnSorter));

		// Specify default sorting
		table.setSortColumn(column1);
		table.setSortDirection(tableColumnSorter.getDirection());

		final var buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		buttons.setLayout(new GridLayout());
		final var newTagButton = new Button(buttons, SWT.PUSH);
		newTagButton.setText(TMUIMessages.Button_new);
		newTagButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newTagButton.addListener(SWT.Selection, (final @Nullable Event e) -> {
			final var dlg = new MarkerConfigEditDialog(getShell(), null);
			if (dlg.open() == Window.OK) {
				markerConfigs.add(castNonNull(dlg.markerConfig));
				markerConfigsTable.refresh();
			}
		});
		final var editTagButton = new Button(buttons, SWT.PUSH);
		editTagButton.setText(TMUIMessages.Button_edit);
		editTagButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editTagButton.addListener(SWT.Selection, (final @Nullable Event e) -> {
			final var selection = (MarkerConfig) ((IStructuredSelection) markerConfigsTable.getSelection()).getFirstElement();
			if (selection != null) {
				final var dlg = new MarkerConfigEditDialog(getShell(), selection);
				if (dlg.open() == Window.OK) {
					if (!selection.equals(dlg.markerConfig)) {
						markerConfigs.remove(selection);
						markerConfigs.add(castNonNull(dlg.markerConfig));
						markerConfigsTable.refresh();
					}
				}
			}
		});
		final var removeTagButton = new Button(buttons, SWT.PUSH);
		removeTagButton.setText(TMUIMessages.Button_remove);
		removeTagButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeTagButton.addListener(SWT.Selection, (final @Nullable Event e) -> {
			final var selection = (MarkerConfig) ((IStructuredSelection) markerConfigsTable.getSelection()).getFirstElement();
			if (selection != null) {
				markerConfigs.remove(selection);
				markerConfigsTable.refresh();
			}
		});
		applyDialogFont(parent);

		markerConfigsTable.setInput(markerConfigs);

		return parent;
	}

	private int computeMinimumColumnWidth(final String string) {
		final GC gc = new GC(getShell());
		try {
			gc.setFont(JFaceResources.getDialogFont());
			return gc.stringExtent(string).x + 20; // pad 20 to accommodate table header trimmings
		} finally {
			gc.dispose();
		}
	}

	@Override
	public void init(final @Nullable IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		markerConfigs.clear();
		markerConfigs.addAll(MarkerConfig.getDefaults());
		markerConfigsTable.refresh();
	}

	@Override
	public boolean performOk() {
		PreferenceHelper.saveMarkerConfigs(markerConfigs);
		MarkerUtils.reloadMarkerConfigs();
		return true;
	}
}
