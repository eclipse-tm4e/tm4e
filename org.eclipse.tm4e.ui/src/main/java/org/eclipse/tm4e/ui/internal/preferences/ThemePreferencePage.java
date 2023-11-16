/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Nicolaj Hoess <nicohoess@gmail.com> - Editor templates pref page: Allow to sort by column -
 * https://bugs.eclipse.org/203722
 * Angelo Zerr <angelo.zerr@gmail.com> - Adapt org.eclipse.ui.texteditor.templates.TemplatePreferencePage for TextMate
 * theme
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.io.File;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.widgets.ColumnSelectionAdapter;
import org.eclipse.tm4e.ui.internal.widgets.ColumnViewerComparator;
import org.eclipse.tm4e.ui.internal.widgets.GrammarDefinitionContentProvider;
import org.eclipse.tm4e.ui.internal.widgets.GrammarDefinitionLabelProvider;
import org.eclipse.tm4e.ui.internal.widgets.TMViewer;
import org.eclipse.tm4e.ui.internal.widgets.ThemeContentProvider;
import org.eclipse.tm4e.ui.internal.widgets.ThemeLabelProvider;
import org.eclipse.tm4e.ui.snippets.ISnippet;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.tm4e.ui.themes.Theme;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A theme preference page allows configuration of the TextMate themes.
 * It provides controls for adding, removing and changing theme as well as enablement, default management.
 */
public final class ThemePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	static final String PAGE_ID = "org.eclipse.tm4e.ui.preferences.ThemePreferencePage";

	// Theme content
	private TableViewer themesTable = lazyNonNull();
	private Button themeRemoveButton = lazyNonNull();

	// Preview content
	private ComboViewer grammarsCombo = lazyNonNull();
	private TMViewer previewViewer = lazyNonNull();

	private final IGrammarRegistryManager grammarRegistryManager = TMEclipseRegistryPlugin.getGrammarRegistryManager();
	private final IThemeManager themeManager = TMUIPlugin.getThemeManager();

	private Button darkThemeButton = lazyNonNull();
	private Button defaultThemeButton = lazyNonNull();

	@Nullable
	private ITheme selectedTheme;

	public ThemePreferencePage() {
		setDescription(TMUIMessages.ThemePreferencePage_description);
	}

	@Override
	protected Control createContents(@Nullable final Composite ancestor) {
		final var parent = new SashForm(ancestor, SWT.VERTICAL | SWT.SMOOTH);
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		parent.setLayout(new FillLayout());

		final var innerParent = new Composite(parent, SWT.NONE);
		final var innerLayout = new GridLayout();
		innerLayout.numColumns = 2;
		innerLayout.marginHeight = 0;
		innerLayout.marginWidth = 0;
		innerParent.setLayout(innerLayout);

		createThemesTableContent(innerParent);
		createThemeDetailContent(innerParent);
		createThemePreviewContent(parent);

		parent.setSashWidth(3);
		parent.setWeights(2, 1);

		themesTable.setInput(themeManager);

		Dialog.applyDialogFont(parent);
		innerParent.layout();

		return parent;
	}

	/**
	 * Create the theme list content.
	 *
	 * @param parent
	 */
	private void createThemesTableContent(final Composite parent) {
		final GridLayout layout;
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

		themesTable = new TableViewer(table);

		final var column1 = new TableColumn(table, SWT.NONE);
		column1.setText(TMUIMessages.ThemePreferencePage_column_name);
		int minWidth = computeMinimumColumnWidth(gc, TMUIMessages.ThemePreferencePage_column_name);
		columnLayout.setColumnData(column1, new ColumnWeightData(2, minWidth, true));
		column1.addSelectionListener(new ColumnSelectionAdapter(column1, themesTable, 0, viewerComparator));

		final var column2 = new TableColumn(table, SWT.NONE);
		column2.setText(TMUIMessages.ThemePreferencePage_column_path);
		minWidth = computeMinimumColumnWidth(gc, TMUIMessages.ThemePreferencePage_column_path);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));
		column2.addSelectionListener(new ColumnSelectionAdapter(column2, themesTable, 1, viewerComparator));

		final var column3 = new TableColumn(table, SWT.NONE);
		column3.setText(TMUIMessages.ThemePreferencePage_column_pluginId);
		minWidth = computeMinimumColumnWidth(gc, TMUIMessages.ThemePreferencePage_column_pluginId);
		columnLayout.setColumnData(column3, new ColumnWeightData(2, minWidth, true));
		column3.addSelectionListener(new ColumnSelectionAdapter(column3, themesTable, 2, viewerComparator));

		gc.dispose();

		themesTable.setLabelProvider(new ThemeLabelProvider());
		themesTable.setContentProvider(new ThemeContentProvider());
		themesTable.setComparator(viewerComparator);
		themesTable.addSelectionChangedListener(e -> {
			// Fill Theme details
			final var selectedTheme = ThemePreferencePage.this.selectedTheme = (ITheme) ((IStructuredSelection) themesTable
					.getSelection()).getFirstElement();
			if (selectedTheme != null) {
				darkThemeButton.setSelection(selectedTheme.isDark());
				defaultThemeButton.setSelection(selectedTheme.isDefault());
				themeRemoveButton.setEnabled(selectedTheme.getPluginId() == null);
			}
			preview();
		});

		// Specify default sorting
		table.setSortColumn(column1);
		table.setSortDirection(viewerComparator.getDirection());

		BidiUtils.applyTextDirection(themesTable.getControl(), BidiUtils.BTD_DEFAULT);

		final var buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		final var themeNewButton = new Button(buttons, SWT.PUSH);
		themeNewButton.setText(TMUIMessages.Button_new);
		themeNewButton.setLayoutData(getButtonGridData(themeNewButton));
		themeNewButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(@Nullable final Event e) {
				final ITheme newTheme = addTheme();
				if (newTheme != null) {
					themeManager.registerTheme(newTheme);
					selectedTheme = newTheme;
					themesTable.refresh();
					themesTable.setSelection(new StructuredSelection(newTheme));
				}
			}

			@Nullable
			private ITheme addTheme() {
				final var dialog = new FileDialog(getShell());
				dialog.setText("Select textmate theme file");
				dialog.setFilterExtensions(new String[] { "*.css" });
				final String res = dialog.open();
				if (res == null) {
					return null;
				}
				final var file = new File(res);
				final String name = file.getName().substring(0, file.getName().length() - ".css".length());
				return new Theme(name, file.getAbsolutePath(), name, false, false);
			}
		});

		themeRemoveButton = new Button(buttons, SWT.PUSH);
		themeRemoveButton.setText(TMUIMessages.Button_remove);
		themeRemoveButton.setLayoutData(getButtonGridData(themeRemoveButton));
		themeRemoveButton.addListener(SWT.Selection, e -> {
			if (selectedTheme != null) {
				themeManager.unregisterTheme(selectedTheme);
			}
			themesTable.refresh();
		});
	}

	/**
	 * Create theme detail content.
	 */
	private void createThemeDetailContent(final Composite ancestor) {
		final var parent = new Composite(ancestor, SWT.NONE);
		final var data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		parent.setLayoutData(data);

		final var layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		parent.setLayout(layout);

		darkThemeButton = new Button(parent, SWT.CHECK);
		darkThemeButton.setText(TMUIMessages.ThemePreferencePage_darkThemeButton_label);
		darkThemeButton.setEnabled(false);

		defaultThemeButton = new Button(parent, SWT.CHECK);
		defaultThemeButton.setText(TMUIMessages.ThemePreferencePage_defaultThemeButton_label);
		defaultThemeButton.setEnabled(false);
	}

	/**
	 * Create theme associations content.
	 */
	private void createThemePreviewContent(final Composite ancestor) {
		final var parent = new Composite(ancestor, SWT.NONE);
		final var layout = new GridLayout(2, false);
		layout.marginHeight = 2;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		final var label = new Label(parent, SWT.NONE);
		label.setText(TMUIMessages.ThemePreferencePage_preview);
		var data = new GridData();
		label.setLayoutData(data);

		grammarsCombo = new ComboViewer(parent);
		grammarsCombo.setContentProvider(new GrammarDefinitionContentProvider());
		grammarsCombo.setLabelProvider(new GrammarDefinitionLabelProvider());
		grammarsCombo.setComparator(new ViewerComparator());
		grammarsCombo.addSelectionChangedListener(e -> preview());
		grammarsCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grammarsCombo.setInput(grammarRegistryManager);
		if (grammarsCombo.getCombo().getItemCount() > 0) {
			grammarsCombo.getCombo().select(0);
		}

		previewViewer = new TMViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		// Don't set caret to 'null' as this causes https://bugs.eclipse.org/293263
		// viewer.getTextWidget().setCaret(null);

		final var control = previewViewer.getControl();
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		data.heightHint = convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
	}

	private int computeMinimumColumnWidth(final GC gc, final String string) {
		return gc.stringExtent(string).x + 10; // pad 10 to accommodate table header trimmings
	}

	/**
	 * @returns the grid data for the button.
	 */
	private static GridData getButtonGridData(final Button button) {
		final var data = new GridData(GridData.FILL_HORIZONTAL);
		// TODO replace SWTUtil
		// data.widthHint= SWTUtil.getButtonWidthHint(button);
		// data.heightHint= SWTUtil.getButtonHeightHint(button);

		return data;
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);
		if (visible)
			setTitle(TMUIMessages.ThemePreferencePage_title);
	}

	@Override
	public void init(@Nullable final IWorkbench workbench) {
	}

	private void preview() {
		var selection = (IStructuredSelection) themesTable.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		final ITheme theme = (ITheme) selection.getFirstElement();

		selection = (IStructuredSelection) grammarsCombo.getSelection();
		if (selection.isEmpty()) {
			return;
		}

		final IGrammarDefinition definition = (IGrammarDefinition) selection.getFirstElement();

		// Preview the grammar
		final IGrammar grammar = grammarRegistryManager.getGrammarForScope(definition.getScopeName());
		previewViewer.setTheme(theme);
		previewViewer.setGrammar(grammar);

		// Snippet
		final ISnippet[] snippets = TMUIPlugin.getSnippetManager().getSnippets(definition.getScopeName());
		if (snippets.length == 0) {
			previewViewer.setText("");
		} else {
			// TODO: manage list of snippet for the given scope.
			previewViewer.setText(snippets[0].getContent());
		}
	}

	@Override
	public boolean performOk() {
		try {
			themeManager.save();
			grammarRegistryManager.save();
			return true;
		} catch (final BackingStoreException ex) {
			TMUIPlugin.logError(ex);
			return false;
		}
	}
}
