/*******************************************************************************
 * Copyright (c) 2023, 2024 Vegard IT GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig.ProblemMarkerConfig;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig.ProblemSeverity;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig.TaskMarkerConfig;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig.TaskPriority;
import org.eclipse.tm4e.ui.internal.utils.MarkerConfig.Type;
import org.eclipse.tm4e.ui.internal.utils.MarkerUtils;
import org.eclipse.tm4e.ui.internal.utils.UI;
import org.eclipse.tm4e.ui.internal.widgets.TableWidget;
import org.eclipse.tm4e.ui.internal.widgets.TableWithControlsWidget;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Task Tags preferences page.
 */
public final class TaskTagsPreferencePage extends AbstractPreferencePage {

	static final String PAGE_ID = "org.eclipse.tm4e.ui.preferences.TaskTagsPreferencePage";

	private static final class MarkerConfigEditDialog extends TitleAreaDialog {

		@Nullable
		MarkerConfig markerConfig;

		Text txtTag = lateNonNull();
		Combo cmbType = lateNonNull();
		Label lblLevel = lateNonNull();
		Combo cmbLevel = lateNonNull();

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
			final var btn = getButton(IDialogConstants.OK_ID);
			if (btn == null)
				return;
			btn.setEnabled(!txtTag.getText().isBlank() && cmbType.getSelectionIndex() > -1 && cmbLevel.getSelectionIndex() > -1);
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
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
			cmbType.addListener(SWT.Modify, (final Event e) -> {
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
			layoutData.widthHint = UI.getTextWidth("1234567890");
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
	private TableWidget<MarkerConfig> markerConfigsTable = lateNonNull();

	public TaskTagsPreferencePage() {
		super(null, TMUIMessages.TaskTagsPreferencePage_description);
	}

	@Override
	protected Control createContents(final Composite parent) {
		createMarkerConfigsTable(parent);

		applyDialogFont(parent);

		markerConfigsTable.setInput(markerConfigs);

		return markerConfigsTable.getControl();
	}

	private void createMarkerConfigsTable(final Composite parent) {

		final var tableWithControls = new TableWithControlsWidget<MarkerConfig>(parent, null, true) {

			@Override
			protected TableWidget<MarkerConfig> createTable(final Composite parent) {
				return new TableWidget<>(parent, false) {

					@Override
					protected void createColumns() {
						createAutoResizeColumn(TMUIMessages.TaskTagsPreferencePage_column_tag);
						createAutoResizeColumn(TMUIMessages.TaskTagsPreferencePage_column_type, 0);
						createAutoResizeColumn(TMUIMessages.TaskTagsPreferencePage_column_level, 0);
					}

					@Override
					protected @Nullable Object getColumnText(final MarkerConfig taskTag, final int columnIndex) {
						return switch (columnIndex) {
							case 0 -> taskTag.tag;
							case 1 -> taskTag.type.name().charAt(0) + taskTag.type.name().substring(1).toLowerCase();
							case 2 -> switch (taskTag.type) {
								case PROBLEM -> taskTag.asProblemMarkerConfig().severity;
								case TASK -> taskTag.asTaskMarkerConfig().priority;
							};
							default -> null;
						};
					}
				};
			}

			@Override
			protected void createButtons() {
				// Add tag
				createButton(TMUIMessages.Button_new, () -> {
					final var dlg = new MarkerConfigEditDialog(getShell(), null);
					if (dlg.open() == Window.OK) {
						markerConfigs.add(castNonNull(dlg.markerConfig));
						table.refresh();
						table.setSelection(true, castNonNull(dlg.markerConfig));
					}
				});

				// Edit tag
				final var editBtn = createButton(TMUIMessages.Button_edit, () -> {
					final MarkerConfig selectedTag = table.getFirstSelectedElement();
					if (selectedTag != null) {
						final var dlg = new MarkerConfigEditDialog(getShell(), selectedTag);
						if (dlg.open() == Window.OK && !selectedTag.equals(dlg.markerConfig)) {
							markerConfigs.remove(selectedTag);
							markerConfigs.add(castNonNull(dlg.markerConfig));
							table.refresh();
							table.setSelection(true, castNonNull(dlg.markerConfig));
						}
					}
				});
				table.onSelectionChanged(sel -> editBtn.setEnabled(!sel.isEmpty()));

				// Remove tag
				final var removeBtn = createButton(TMUIMessages.Button_remove, () -> {
					final MarkerConfig selectedTag = table.getFirstSelectedElement();
					if (selectedTag != null) {
						markerConfigs.remove(selectedTag);
						table.refresh();
					}
				});
				table.onSelectionChanged(sel -> removeBtn.setEnabled(!sel.isEmpty()));
			}
		};

		tableWithControls.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, true)
				.hint(360, convertHeightInCharsToPixels(10))
				.create());

		markerConfigsTable = tableWithControls.getTable();
	}

	@Override
	protected void performDefaults() {
		markerConfigs.clear();
		markerConfigs.addAll(MarkerConfig.getDefaults());
		markerConfigsTable.refresh();
	}

	@Override
	public boolean performOk() {
		try {
			PreferenceHelper.saveMarkerConfigs(markerConfigs);
			MarkerUtils.reloadMarkerConfigs();
		} catch (final BackingStoreException ex) {
			TMUIPlugin.logError(ex);
			return false;
		}
		return super.performOk();
	}
}
