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
 * - Angelo Zerr <angelo.zerr@gmail.com> - Adapt org.eclipse.ui.texteditor.templates.TemplatePreferencePage for TextMate theme
 * - Sebastian Thomschke (Vegard IT) - major cleanup/refactoring, added table filtering and performDefaults support
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.tm4e.ui.internal.themes.Theme;
import org.eclipse.tm4e.ui.internal.themes.ThemeManager;
import org.eclipse.tm4e.ui.internal.widgets.GrammarDefinitionLabelProvider;
import org.eclipse.tm4e.ui.internal.widgets.TMViewer;
import org.eclipse.tm4e.ui.internal.widgets.TableWidget;
import org.eclipse.tm4e.ui.internal.widgets.TableWithControlsWidget;
import org.eclipse.tm4e.ui.internal.widgets.VerticalSplitPane;
import org.eclipse.tm4e.ui.snippets.ISnippet;
import org.eclipse.tm4e.ui.themes.ITheme;
import org.eclipse.tm4e.ui.themes.IThemeManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A theme preference page allows configuration of the TextMate themes.
 * It provides controls for adding, removing and changing theme as well as enablement, default management.
 */
public final class ThemePreferencePage extends AbstractPreferencePage {

	static final String PAGE_ID = "org.eclipse.tm4e.ui.preferences.ThemePreferencePage";

	// Managers
	private final IGrammarRegistryManager grammarManager = TMEclipseRegistryPlugin.getGrammarRegistryManager();
	private IThemeManager.EditSession themeManager = ThemeManager.getInstance().newEditSession();

	private TableWidget<ITheme> themesTable = lateNonNull();

	// Preview content
	private ComboViewer grammarsCombo = lateNonNull();
	private TMViewer themePreview = lateNonNull();

	public ThemePreferencePage() {
		super(TMUIMessages.ThemePreferencePage_title, TMUIMessages.ThemePreferencePage_description);
	}

	@Override
	protected Control createContents(final Composite parent) {
		final var control = new VerticalSplitPane(parent, 1, 1) {

			@Override
			protected void configureUpperArea(final Composite parent) {
				createThemesTable(parent);
				createThemeDetailsView(parent);
			}

			@Override
			protected void configureLowerArea(final Composite parent) {
				createThemePreview(parent);
			}
		}.getControl();

		Dialog.applyDialogFont(control);

		themesTable.setInput(themeManager);

		return control;
	}

	private void createThemesTable(final Composite parent) {

		final var tableWithControls = new TableWithControlsWidget<ITheme>(parent, null, true) {

			@Override
			protected TableWidget<ITheme> createTable(final Composite parent) {
				return new TableWidget<>(parent, false) {
					@Override
					protected void createColumns() {
						createAutoResizeColumn(TMUIMessages.ThemePreferencePage_column_name);
						createAutoResizeColumn(TMUIMessages.ThemePreferencePage_column_type, 0);
						createAutoResizeColumn(TMUIMessages.ThemePreferencePage_column_source, 0);
					}

					@Override
					protected @Nullable String getColumnText(final ITheme theme, final int columnIndex) {
						return switch (columnIndex) {
							case 0 -> theme.getName();
							case 1 -> theme.isDark() ? "dark" : "light";
							case 2 -> (theme.getPluginId() == null ? "" : "" + theme.getPluginId() + " > ") + theme.getPath();
							default -> null;
						};
					}

					@Override
					protected Object[] getElements(final @Nullable Object input) {
						if (input instanceof final IThemeManager themeManager)
							return themeManager.getThemes();
						return super.getElements(input);
					}
				};
			}

			@Override
			protected void createButtons() {
				// Add theme
				createButton(TMUIMessages.Button_new, () -> {
					final ITheme newTheme = openBrowseForThemeDialog();
					if (newTheme != null) {
						themeManager.registerTheme(newTheme);
						table.refresh();
						table.setSelection(true, newTheme);
					}
				});

				// Remove theme
				final var removeBtn = createButton(TMUIMessages.Button_remove, () -> {
					final var selectedTheme = table.getFirstSelectedElement();
					if (selectedTheme != null) {
						themeManager.unregisterTheme(selectedTheme);
						table.refresh();
					}
				});
				table.onSelectionChanged(sel -> removeBtn.setEnabled(!sel.isEmpty() && sel.get(0).getPluginId() == null));
			}

			private @Nullable ITheme openBrowseForThemeDialog() {
				final var dialog = new FileDialog(getShell());
				dialog.setText("Select textmate theme file");
				dialog.setFilterExtensions(new String[] { "*.css;*.json;*.plist;*.tmTheme;*.YAML-tmTheme" });
				final String res = dialog.open();
				if (res == null) {
					return null;
				}
				final var themePath = Path.of(res);
				final var themeFileName = themePath.getFileName().toString();
				try {
					final String themeName;
					if (themeFileName.endsWith(".css")) {
						themeName = themeFileName.substring(0, themeFileName.lastIndexOf('.'));
					} else {
						final var rawTheme = RawThemeReader.readTheme(IThemeSource.fromFile(themePath));
						final var rawThemeName = rawTheme.getName();
						themeName = rawThemeName == null
								? themeFileName.substring(0, themeFileName.lastIndexOf('.'))
								: rawThemeName;
					}
					return new Theme(themeName, themePath.toAbsolutePath().toString(), themeName, false);
				} catch (final Exception ex) {
					MessageDialog.openError(getShell(), "Invalid theme file", "Failed to parse [" + themePath + "]: " + ex);
					TMUIPlugin.logError(ex);
					return null;
				}

			}
		};

		tableWithControls.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, true)
				.hint(360, convertHeightInCharsToPixels(10))
				.create());

		themesTable = tableWithControls.getTable();
	}

	/**
	 * Create theme detail content.
	 */
	private void createThemeDetailsView(final Composite ancestor) {
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

		final var darkThemeButton = new Button(parent, SWT.CHECK);
		darkThemeButton.setText(TMUIMessages.ThemePreferencePage_darkThemeButton_label);
		darkThemeButton.setEnabled(false);

		final var defaultThemeButton = new Button(parent, SWT.CHECK);
		defaultThemeButton.setText(TMUIMessages.ThemePreferencePage_defaultThemeButton_label);
		defaultThemeButton.setEnabled(true);
		defaultThemeButton.addListener(SWT.Selection, e -> {
			final var selectedTheme = themesTable.getFirstSelectedElement();
			if (selectedTheme != null) {
				themeManager.setDefaultTheme(selectedTheme.getId(), selectedTheme.isDark());
			}
		});

		themesTable.onSelectionChanged(themes -> {
			if (themes.isEmpty()) {
				darkThemeButton.setSelection(false);
				defaultThemeButton.setSelection(false);
				defaultThemeButton.setEnabled(false);
				return;
			}
			final var selectedTheme = themes.get(0);

			// Update buttons
			darkThemeButton.setSelection(selectedTheme.isDark());
			darkThemeButton.setEnabled(selectedTheme.getPluginId() == null);
			defaultThemeButton.setSelection(themeManager.getDefaultTheme(selectedTheme.isDark()) == selectedTheme);
		});
	}

	private void createThemePreview(final Composite parent) {
		final var label = new Label(parent, SWT.NONE);
		label.setText(TMUIMessages.ThemePreferencePage_preview);

		grammarsCombo = new ComboViewer(parent);
		grammarsCombo.setContentProvider(ArrayContentProvider.getInstance());
		grammarsCombo.setLabelProvider(new GrammarDefinitionLabelProvider());
		grammarsCombo.setComparator(new ViewerComparator());
		grammarsCombo.addSelectionChangedListener(e -> preview());
		grammarsCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grammarsCombo.setInput(Stream.of(grammarManager.getDefinitions())
				// only list grammars for which a snippet is registered
				.filter(grammar -> TMUIPlugin.getSnippetManager().getSnippets(grammar.getScope().getName()).length > 0)
				.toArray());
		if (grammarsCombo.getCombo().getItemCount() > 0) {
			grammarsCombo.getCombo().select(0);
		}

		themePreview = new TMViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		// Don't set caret to 'null' as this causes https://bugs.eclipse.org/293263
		// viewer.getTextWidget().setCaret(null);

		final var control = themePreview.getControl();
		control.setLayoutData(GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL)
				.grab(true, true)
				.hint(SWT.DEFAULT, convertHeightInCharsToPixels(5))
				.create());

		themesTable.onSelectionChanged(themes -> preview());
	}

	private void preview() {
		final @Nullable ITheme theme = themesTable.getFirstSelectedElement();
		if (theme == null)
			return;

		final var selection = grammarsCombo.getStructuredSelection();
		if (selection.getFirstElement() instanceof IGrammarDefinition definition) {
			// Preview the grammar
			final IGrammar grammar = grammarManager.getGrammarForScope(definition.getScope());
			themePreview.setTheme(theme);
			themePreview.setGrammar(grammar);

			// Snippet
			final ISnippet[] snippets = TMUIPlugin.getSnippetManager().getSnippets(definition.getScope().getName());
			if (snippets.length == 0) {
				themePreview.setText("");
			} else {
				// TODO: manage list of snippet for the given scope.
				themePreview.setText(snippets[0].getContent());
			}
		}
	}

	@Override
	protected void performDefaults() {
		themeManager = ThemeManager.getInstance().newEditSession();
		themesTable.setInput(themeManager);
	}

	@Override
	public boolean performOk() {
		try {
			themeManager.save();
		} catch (final BackingStoreException ex) {
			TMUIPlugin.logError(ex);
			return false;
		}
		return super.performOk();
	}
}
