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
 *     Angelo Zerr <angelo.zerr@gmail.com> - Adapt org.eclipse.ui.texteditor.templates.TemplatePreferencePage for TextMate theme
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A theme preference page allows configuration of the TextMate themes It
 * provides controls for adding, removing and changing theme as well as
 * enablement, default management.
 */
public class ThemePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public final static String PAGE_ID = "org.eclipse.tm4e.ui.preferences.ThemePreferencePage";

	// Theme content
	private TableViewer themeViewer;
	private Button themeNewButton;
	private Button themeRemoveButton;

	// Preview content
	private ComboViewer grammarViewer;
	private TMViewer previewViewer;

	private IGrammarRegistryManager grammarRegistryManager;
	private IThemeManager themeManager;

	private Button darkThemeButton;

	private Button defaultThemeButton;

	public ThemePreferencePage() {
		super();
		setDescription(TMUIMessages.ThemePreferencePage_description);
		setGrammarRegistryManager(TMEclipseRegistryPlugin.getGrammarRegistryManager());
		setThemeManager(TMUIPlugin.getThemeManager());
	}

	/**
	 * Returns the grammar registry manager.
	 *
	 * @return the grammar registry manager.
	 */
	public IGrammarRegistryManager getGrammarRegistryManager() {
		return grammarRegistryManager;
	}

	/**
	 * Set the grammar registry manager.
	 *
	 * @param grammarRegistryManager
	 */
	public void setGrammarRegistryManager(IGrammarRegistryManager grammarRegistryManager) {
		this.grammarRegistryManager = grammarRegistryManager;
	}

	/**
	 * Returns the theme manager.
	 *
	 * @return the theme manager.
	 */
	public IThemeManager getThemeManager() {
		return themeManager;
	}

	/**
	 * Set the theme manager.
	 *
	 * @param themeManager
	 */
	public void setThemeManager(IThemeManager themeManager) {
		this.themeManager = themeManager;
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

		createThemesContent(innerParent);
		createThemeDetailContent(innerParent);
		createPreviewContent(innerParent);

		grammarViewer.setInput(grammarRegistryManager);
		if (grammarViewer.getCombo().getItemCount() > 0) {
			grammarViewer.getCombo().select(0);
		}
		themeViewer.setInput(themeManager);

		updateButtons();
		Dialog.applyDialogFont(parent);
		innerParent.layout();

		return parent;
	}

	/**
	 * Create the theme list content.
	 *
	 * @param parent
	 */
	private void createThemesContent(Composite parent) {
		GridLayout layout;
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

		themeViewer = new TableViewer(table);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(TMUIMessages.ThemePreferencePage_column_name);
		int minWidth = computeMinimumColumnWidth(gc, TMUIMessages.ThemePreferencePage_column_name);
		columnLayout.setColumnData(column1, new ColumnWeightData(2, minWidth, true));
		column1.addSelectionListener(new ColumnSelectionAdapter(column1, themeViewer, 0, viewerComparator));

		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(TMUIMessages.ThemePreferencePage_column_path);
		minWidth = computeMinimumColumnWidth(gc, TMUIMessages.ThemePreferencePage_column_path);
		columnLayout.setColumnData(column2, new ColumnWeightData(2, minWidth, true));
		column2.addSelectionListener(new ColumnSelectionAdapter(column2, themeViewer, 1, viewerComparator));

		TableColumn column3 = new TableColumn(table, SWT.NONE);
		column3.setText(TMUIMessages.ThemePreferencePage_column_pluginId);
		minWidth = computeMinimumColumnWidth(gc, TMUIMessages.ThemePreferencePage_column_pluginId);
		columnLayout.setColumnData(column3, new ColumnWeightData(2, minWidth, true));
		column3.addSelectionListener(new ColumnSelectionAdapter(column3, themeViewer, 2, viewerComparator));

		gc.dispose();

		themeViewer.setLabelProvider(new ThemeLabelProvider());
		themeViewer.setContentProvider(new ThemeContentProvider());
		themeViewer.setComparator(viewerComparator);
		themeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				// Fill Theme details
				ITheme theme = ((ITheme) ((IStructuredSelection) themeViewer.getSelection()).getFirstElement());
				if (theme != null) {
					darkThemeButton.setSelection(theme.isDark());
					defaultThemeButton.setSelection(theme.isDefault());
				}
				preview();
			}
		});

		// Specify default sorting
		table.setSortColumn(column1);
		table.setSortDirection(viewerComparator.getDirection());

		BidiUtils.applyTextDirection(themeViewer.getControl(), BidiUtils.BTD_DEFAULT);

		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		themeNewButton = new Button(buttons, SWT.PUSH);
		themeNewButton.setText(TMUIMessages.Button_new);
		themeNewButton.setLayoutData(getButtonGridData(themeNewButton));
		themeNewButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				// add();
			}
		});

		themeRemoveButton = new Button(buttons, SWT.PUSH);
		themeRemoveButton.setText(TMUIMessages.Button_remove);
		themeRemoveButton.setLayoutData(getButtonGridData(themeRemoveButton));
		themeRemoveButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				// remove();
			}
		});
	}

	/**
	 * Create theme detail content.
	 *
	 * @param parent
	 */
	private void createThemeDetailContent(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		parent.setLayoutData(data);

		GridLayout layout = new GridLayout();
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
	 *
	 * @param parent
	 */
	private void createPreviewContent(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		parent.setLayoutData(data);

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		previewViewer = doCreateViewer(parent);
	}

	private int computeMinimumColumnWidth(GC gc, String string) {
		return gc.stringExtent(string).x + 10; // pad 10 to accommodate table
												// header trimmings
	}

	/**
	 * Return the grid data for the button.
	 *
	 * @param button
	 *            the button
	 * @return the grid data
	 */
	private static GridData getButtonGridData(Button button) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		// TODO replace SWTUtil
		// data.widthHint= SWTUtil.getButtonWidthHint(button);
		// data.heightHint= SWTUtil.getButtonHeightHint(button);

		return data;
	}

	private void updateButtons() {
		themeNewButton.setEnabled(false);
		themeRemoveButton.setEnabled(false);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			setTitle(TMUIMessages.ThemePreferencePage_title);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	private void preview() {
		IStructuredSelection selection = (IStructuredSelection) themeViewer.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		ITheme theme = (ITheme) selection.getFirstElement();

		selection = (IStructuredSelection) grammarViewer.getSelection();
		if (selection.isEmpty()) {
			return;
		}

		IGrammarDefinition definition = (IGrammarDefinition) selection.getFirstElement();

		// Preview the grammar
		IGrammar grammar = grammarRegistryManager.getGrammarForScope(definition.getScopeName());
		previewViewer.setTheme(theme);
		previewViewer.setGrammar(grammar);
	}

	private TMViewer doCreateViewer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(TMUIMessages.ThemePreferencePage_preview);
		GridData data = new GridData();
		label.setLayoutData(data);

		grammarViewer = new ComboViewer(parent);
		grammarViewer.setContentProvider(new GrammarDefinitionContentProvider());
		grammarViewer.setLabelProvider(new GrammarDefinitionLabelProvider());
		grammarViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				preview();
			}
		});
		grammarViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
	protected TMViewer createViewer(Composite parent) {
		return new TMViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	}

}
