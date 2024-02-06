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

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lazyNonNull;

import java.io.File;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.internal.widgets.GrammarDefinitionContentProvider;
import org.eclipse.tm4e.ui.internal.widgets.GrammarDefinitionLabelProvider;
import org.eclipse.tm4e.ui.internal.widgets.TMViewer;
import org.eclipse.tm4e.ui.internal.widgets.TableWidget;
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
	private TableWidget<ITheme> themesTable = lazyNonNull();
	private Button themeRemoveButton = lazyNonNull();

	// Preview content
	private ComboViewer grammarsCombo = lazyNonNull();
	private TMViewer previewViewer = lazyNonNull();

	private final IGrammarRegistryManager grammarRegistryManager = TMEclipseRegistryPlugin.getGrammarRegistryManager();
	private final IThemeManager.EditSession themeManager = ThemeManager.getInstance().createEditSession();

	private Button darkThemeButton = lazyNonNull();
	private Button defaultThemeButton = lazyNonNull();

	private @Nullable ITheme selectedTheme;

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
		themesTable.selectFirstRow();

		Dialog.applyDialogFont(parent);
		innerParent.layout();

		return parent;
	}

	protected void createColumn(TableColumnLayout tableColumnLayout, String label, int columnWeight, int minColWidth, boolean resizable) {
		final var col = new TableColumn(themesTable.getTable(), SWT.NONE);
		col.setText(label);
		final GC gc = new GC(themesTable.getTable().getShell());
		try {
			gc.setFont(JFaceResources.getDialogFont());
			final int labelWidth = gc.stringExtent(label).x + 15;
			tableColumnLayout.setColumnData(col, new ColumnWeightData(columnWeight, Math.max(labelWidth, minColWidth), resizable));
		} finally {
			gc.dispose();
		}
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
		tableComposite.setLayout(new FillLayout());

		themesTable = new TableWidget<>(tableComposite) {
			@Override
			protected void createColumns() {
				createAutoResizeColumn(TMUIMessages.ThemePreferencePage_column_name);
				createAutoResizeColumn(TMUIMessages.ThemePreferencePage_column_type, 0);
				createAutoResizeColumn(TMUIMessages.ThemePreferencePage_column_path);
				createAutoResizeColumn(TMUIMessages.ThemePreferencePage_column_pluginId, 0);
			}

			@Override
			protected @Nullable String getColumnText(ITheme theme, int columnIndex) {
				return switch (columnIndex) {
					case 0 -> theme.getName();
					case 1 -> theme.isDark() ? "dark" : "light";
					case 2 -> theme.getPath();
					case 3 -> theme.getPluginId();
					default -> null;
				};
			}

			@Override
			protected Object[] getElements(@Nullable Object input) {
				if (input instanceof IThemeManager themeManager)
					return themeManager.getThemes();
				return super.getElements(input);
			}
		};
		themesTable.onSelected(selection -> {
			final var selectedTheme = this.selectedTheme = selection.get(0);
			darkThemeButton.setSelection(selectedTheme.isDark());
			darkThemeButton.setEnabled(selectedTheme.getPluginId() == null);
			defaultThemeButton.setSelection(themeManager.getDefaultTheme(selectedTheme.isDark()) == selectedTheme);
			themeRemoveButton.setEnabled(selectedTheme.getPluginId() == null);
			preview();
		});

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
					themesTable.setSelection(newTheme);
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
				return new Theme(name, file.getAbsolutePath(), name, false);
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
		defaultThemeButton.setEnabled(true);
		defaultThemeButton.addListener(SWT.Selection, e -> {
			final var selectedTheme = ThemePreferencePage.this.selectedTheme;
			if (selectedTheme != null) {
				themeManager.setDefaultTheme(selectedTheme.getId(), selectedTheme.isDark());
			}
		});
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
		final IGrammar grammar = grammarRegistryManager.getGrammarForScope(definition.getScope());
		previewViewer.setTheme(theme);
		previewViewer.setGrammar(grammar);

		// Snippet
		final ISnippet[] snippets = TMUIPlugin.getSnippetManager().getSnippets(definition.getScope().getName());
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
