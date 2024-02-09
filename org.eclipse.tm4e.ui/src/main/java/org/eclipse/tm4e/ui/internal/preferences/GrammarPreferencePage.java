/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - IBM Corporation - initial API and implementation
 * - Nicolaj Hoess <nicohoess@gmail.com> - Editor templates pref page: Allow to sort by column - https://bugs.eclipse.org/203722
 * - Angelo Zerr <angelo.zerr@gmail.com> - Adapt org.eclipse.ui.texteditor.templates.TemplatePreferencePage for TextMate grammar
 * - Sebastian Thomschke (Vegard IT) - major cleanup/refactoring, added table filtering and performDefaults support
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lazyNonNull;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.registry.WorkingCopyGrammarRegistryManager;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.internal.widgets.ContentTypesBindingWidget;
import org.eclipse.tm4e.ui.internal.widgets.GrammarInfoWidget;
import org.eclipse.tm4e.ui.internal.widgets.TMViewer;
import org.eclipse.tm4e.ui.internal.widgets.TableWidget;
import org.eclipse.tm4e.ui.internal.widgets.TableWithControlsWidget;
import org.eclipse.tm4e.ui.internal.widgets.ThemeAssociationsWidget;
import org.eclipse.tm4e.ui.internal.widgets.VerticalSplitPane;
import org.eclipse.tm4e.ui.internal.wizards.TextMateGrammarImportWizard;
import org.eclipse.tm4e.ui.snippets.ISnippet;
import org.eclipse.tm4e.ui.snippets.ISnippetManager;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A grammar preference page allows configuration of the TextMate grammar.
 * It provides controls for adding, removing and changing grammar as well as enablement, default management.
 */
public final class GrammarPreferencePage extends AbstractPreferencePage {

	static final String PAGE_ID = "org.eclipse.tm4e.ui.preferences.GrammarPreferencePage";

	// Managers
	private IGrammarRegistryManager grammarManager = new WorkingCopyGrammarRegistryManager(
			TMEclipseRegistryPlugin.getGrammarRegistryManager());
	private IThemeManager.EditSession themeManager = ThemeManager.getInstance().newEditSession();
	private ISnippetManager snippetManager = TMUIPlugin.getSnippetManager();

	private TableWidget<IGrammarDefinition> grammarsTable = lazyNonNull();

	// Grammar info tabs
	private GrammarInfoWidget grammarInfoWidget = lazyNonNull();
	private ContentTypesBindingWidget contentTypesWidget = lazyNonNull();
	private ThemeAssociationsWidget themeAssociationsWidget = lazyNonNull();

	private TMViewer grammarPreview = lazyNonNull();

	public GrammarPreferencePage() {
		super(TMUIMessages.GrammarPreferencePage_title, TMUIMessages.GrammarPreferencePage_description);
	}

	@Override
	protected Control createContents(final @NonNullByDefault({}) Composite parent) {
		final var control = new VerticalSplitPane(parent, 1, 1) {

			@Override
			protected void configureUpperArea(final Composite parent) {
				createGrammarsTable(parent);
			}

			@Override
			protected void configureLowerArea(final Composite parent) {
				createGrammarDetailsView(parent);
				createThemePreview(parent);
			}
		}.getControl();

		Dialog.applyDialogFont(control);

		grammarsTable.setInput(grammarManager);
		grammarsTable.selectFirstRow();

		return control;
	}

	private void createGrammarsTable(final Composite parent) {
		final var tableWithControls = new TableWithControlsWidget<IGrammarDefinition>(parent, null, true) {

			@Override
			protected TableWidget<IGrammarDefinition> createTable(final Composite parent) {
				return new TableWidget<>(parent, false) {

					@Override
					protected void createColumns() {
						createAutoResizeColumn(TMUIMessages.GrammarPreferencePage_column_contentTypes, 1);
						createAutoResizeColumn(TMUIMessages.GrammarPreferencePage_column_scopeName, 2);
						createAutoResizeColumn(TMUIMessages.GrammarPreferencePage_column_source, 0, 1);
					}

					@Override
					protected @Nullable String getColumnText(final IGrammarDefinition def, final int columnIndex) {
						return switch (columnIndex) {
							case 0 -> {
								final var contentTypes = grammarManager.getContentTypesForScope(def.getScope());
								yield contentTypes == null
										? null
										: contentTypes.stream().map(ct -> ct.getName())
												.distinct()
												.sorted()
												.collect(Collectors.joining(", "));
							}
							case 1 -> def.getScope().getName();
							case 2 -> (def.getPluginId() == null ? "" : "" + def.getPluginId() + " > ") + def.getPath();
							default -> null;
						};
					}

					@Override
					protected Object[] getElements(final @Nullable Object input) {
						if (input instanceof final IGrammarRegistryManager manager)
							return manager.getDefinitions();
						return super.getElements(input);
					}
				};
			}

			@Override
			protected void createButtons() {
				// Add Grammar
				createButton(TMUIMessages.Button_new, () -> {
					// Open import wizard for TextMate grammar.
					final var wizard = new TextMateGrammarImportWizard(false);
					wizard.setGrammarRegistryManager(grammarManager);
					final var dialog = new WizardDialog(getShell(), wizard);
					if (dialog.open() == Window.OK) {
						// User grammar was saved, refresh the list of grammar and select the created grammar.
						final var created = wizard.getCreatedDefinition();
						table.refresh();
						table.setSelection(true, created);
					}
				});

				// Remove Grammar
				final var removeBtn = createButton(TMUIMessages.Button_remove, () -> {
					final var definition = table.getFirstSelectedElement();
					if (definition != null) {
						grammarManager.unregisterGrammarDefinition(definition);
						table.refresh();
					}
				});
				table.onSelectionChanged(sel -> removeBtn.setEnabled(!sel.isEmpty() && sel.get(0).getPluginId() == null));
			}
		};

		tableWithControls.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, true)
				.hint(360, convertHeightInCharsToPixels(10))
				.create());

		grammarsTable = tableWithControls.getTable();

		grammarsTable.onSelectionChanged(selectedGrammarDefinitions -> {
			if (!selectedGrammarDefinitions.isEmpty()) {
				selectGrammar(selectedGrammarDefinitions.get(0));
			}
		});
	}

	/**
	 * Create detail grammar content which is filled when a grammar is selected in the grammar list.
	 */
	private void createGrammarDetailsView(final Composite parent) {
		final var folder = new TabFolder(parent, SWT.NONE);

		final var gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		folder.setLayoutData(gd);

		createGeneralTab(folder);
		createContentTypeTab(folder);
		createThemeTab(folder);
		createInjectionTab(folder);
	}

	/**
	 * Create "General" tab
	 */
	private void createGeneralTab(final TabFolder folder) {
		final var tab = new TabItem(folder, SWT.NONE);
		tab.setText(TMUIMessages.GrammarPreferencePage_tab_general_text);

		final var parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		grammarInfoWidget = new GrammarInfoWidget(parent, SWT.NONE);
		grammarInfoWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tab.setControl(parent);
	}

	/**
	 * Create "Content type" tab
	 */
	private void createContentTypeTab(final TabFolder folder) {
		final var tab = new TabItem(folder, SWT.NONE);
		tab.setText(TMUIMessages.GrammarPreferencePage_tab_contentType_text);

		final var parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		contentTypesWidget = new ContentTypesBindingWidget(parent, SWT.NONE);
		contentTypesWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tab.setControl(parent);
	}

	/**
	 * Create "Theme" tab
	 */
	private void createThemeTab(final TabFolder folder) {
		final var tab = new TabItem(folder, SWT.NONE);
		tab.setText(TMUIMessages.GrammarPreferencePage_tab_theme_text);

		final var parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		themeAssociationsWidget = new ThemeAssociationsWidget(themeManager, parent, SWT.NONE);
		final var data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		themeAssociationsWidget.setLayoutData(data);
		themeAssociationsWidget.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final @Nullable SelectionChangedEvent e) {
				if (e == null)
					return;
				final var association = (IThemeAssociation) ((IStructuredSelection) e.getSelection()).getFirstElement();
				selectTheme(association);
			}

			private void selectTheme(final @Nullable IThemeAssociation association) {
				themeAssociationsWidget.getNewButton().setEnabled(association != null /* && association.getPluginId() == null */);
				themeAssociationsWidget.getRemoveButton().setEnabled(association != null /* && association.getPluginId() == null */);
				if (association != null) {
					setPreviewTheme(association.getThemeId());
				}
			}
		});

		tab.setControl(parent);

		grammarsTable.onSelectionChanged(selectedGrammarDefinitions -> {
			themeAssociationsWidget.getNewButton().setEnabled(false);
			themeAssociationsWidget.getRemoveButton().setEnabled(false);
		});
	}

	private void setPreviewTheme(final String themeId) {
		final ITheme theme = themeManager.getThemeById(themeId);
		if (theme != null) {
			grammarPreview.setTheme(theme);
		}
	}

	/**
	 * Create "Injection" tab
	 */
	private void createInjectionTab(final TabFolder folder) {
		final var tab = new TabItem(folder, SWT.NONE);
		tab.setText(TMUIMessages.GrammarPreferencePage_tab_injection_text);

		final var parent = new Composite(folder, SWT.NONE);
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// TODO: manage UI injection

		tab.setControl(parent);
	}

	private void selectGrammar(final IGrammarDefinition definition) {
		fillGeneralTab(definition);
		fillContentTypeTab(definition);
		final IThemeAssociation selectedAssociation = fillThemeTab(definition);
		preview(definition, selectedAssociation);
	}

	private void fillGeneralTab(final IGrammarDefinition definition) {
		final IGrammar grammar = grammarManager.getGrammarForScope(definition.getScope());
		grammarInfoWidget.refresh(grammar);
	}

	private void fillContentTypeTab(final IGrammarDefinition definition) {
		// Load the content type binding for the given grammar
		contentTypesWidget.setInput(grammarManager.getContentTypesForScope(definition.getScope()));
	}

	@Nullable
	private IThemeAssociation fillThemeTab(final IGrammarDefinition definition) {
		IThemeAssociation selectedAssociation = null;
		final IStructuredSelection oldSelection = themeAssociationsWidget.getSelection();
		// Load the theme associations for the given grammar
		final IThemeAssociation[] themeAssociations = themeAssociationsWidget.setGrammarDefinition(definition);
		// Try to keep selection
		if (!oldSelection.isEmpty() && Arrays.asList(themeAssociations).contains(oldSelection.getFirstElement())) {
			selectedAssociation = (IThemeAssociation) oldSelection.getFirstElement();
			themeAssociationsWidget.setSelection(oldSelection);
		} else {
			selectedAssociation = themeAssociations.length > 0
					? themeAssociations[0]
					: null;
			if (selectedAssociation != null) {
				themeAssociationsWidget.setSelection(new StructuredSelection(selectedAssociation));
			}
		}
		return selectedAssociation;
	}

	private void createThemePreview(final Composite parent) {
		final var label = new Label(parent, SWT.NONE);
		label.setText(TMUIMessages.GrammarPreferencePage_preview);
		var data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		grammarPreview = new TMViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		// Don't set caret to 'null' as this causes https://bugs.eclipse.org/293263
		// viewer.getTextWidget().setCaret(null);

		final var control = grammarPreview.getControl();
		control.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, true)
				.hint(SWT.DEFAULT, convertHeightInCharsToPixels(5))
				.create());
	}

	private void preview(final IGrammarDefinition definition, final @Nullable IThemeAssociation selectedAssociation) {
		// Preview the grammar
		final IGrammar grammar = grammarManager.getGrammarForScope(definition.getScope());
		if (selectedAssociation != null) {
			setPreviewTheme(selectedAssociation.getThemeId());
		}
		grammarPreview.setGrammar(grammar);
		// Snippet
		final ISnippet[] snippets = snippetManager.getSnippets(definition.getScope().getName());
		if (snippets.length == 0) {
			grammarPreview.setText("");
		} else {
			// TODO: manage list of snippet for the given scope.
			grammarPreview.setText(snippets[0].getContent());
		}
	}

	@Override
	protected void performDefaults() {
		grammarManager = new WorkingCopyGrammarRegistryManager(TMEclipseRegistryPlugin.getGrammarRegistryManager());
		themeManager = ThemeManager.getInstance().newEditSession();
		grammarsTable.setInput(themeManager);
	}

	@Override
	public boolean performOk() {
		try {
			grammarManager.save();
			themeManager.save();
		} catch (final BackingStoreException ex) {
			TMUIPlugin.logError(ex);
			return false;
		}
		return super.performOk();
	}
}
