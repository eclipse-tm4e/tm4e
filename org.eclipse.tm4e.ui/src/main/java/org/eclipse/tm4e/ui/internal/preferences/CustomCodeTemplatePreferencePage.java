/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dietrich Travkin (SOLUNAR GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.tm4e.ui.internal.preferences;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.utils.NullSafetyHelper;
import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.utils.CodeTemplateContextTypeUtils;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public class CustomCodeTemplatePreferencePage extends TemplatePreferencePage {

	private @Nullable TMPresentationReconciler previewReconsiler;

	public CustomCodeTemplatePreferencePage() {
		final TMUIPlugin plugin = TMUIPlugin.getDefault();
		if (plugin == null) {
			return;
		}

		setPreferenceStore(plugin.getPreferenceStore());
		setTemplateStore(plugin.getTemplateStore());
		setContextTypeRegistry(TMUIPlugin.from(plugin.getTemplateContextRegistry()));
	}

	private static class TMEditTemplateDialog extends TemplatePreferencePage.EditTemplateDialog {

		private @Nullable TMPresentationReconciler editTemplatePresentationReconsiler = null;

		public TMEditTemplateDialog(final Shell shell, final Template template, final boolean edit, final boolean isNameModifiable,
				@SuppressWarnings("deprecation") final org.eclipse.jface.text.templates.ContextTypeRegistry contextTypeRegistry) {
			super(shell, template, edit, isNameModifiable, contextTypeRegistry);
		}

		@Override
		protected Control createDialogArea(final Composite ancestor) {
			final Control result = super.createDialogArea(ancestor);

			// work-around 1: find the name text field and set a minimum width
			// work-around 2: find the combo box (drop-down list) and register a modify listener for updating the reconsiler's grammar to the current context type
			boolean foundNameField = false;
			boolean foundDropDownList = false;
			for (final Control childControlLvl1 : ancestor.getChildren()) {
				if (childControlLvl1 instanceof final Composite childL1) {
					for (final Control childControlLvl2 : childL1.getChildren()) {
						if (childControlLvl2 instanceof final Composite childL2) {
							for (final Control childControlLvl3 : childL2.getChildren()) {
								if (childControlLvl3 instanceof final Text nameTextField
										&& nameTextField.getLayoutData() instanceof final GridData gridData) {
									gridData.minimumWidth = 50;
									foundNameField = true;
								} else if (childControlLvl3 instanceof final Combo dropDownList) {
									foundDropDownList = true;
									dropDownList.addModifyListener(e -> {
										if (editTemplatePresentationReconsiler != null
												&& e.widget instanceof final Combo combo
												&& !combo.isDisposed()) {
											final String activeContextTypeName = combo.getText();
											final IGrammar grammar = CodeTemplateContextTypeUtils.toGrammar(activeContextTypeName);
											NullSafetyHelper.castNonNull(editTemplatePresentationReconsiler).setGrammar(grammar);
										}
									});
								}

								if (foundNameField && foundDropDownList) {
									return result;
								}
							}
						}
					}
				}
			}

			return result;
		}

		@Override
		protected SourceViewer createViewer(final @Nullable Composite parent) {
			final SourceViewer originalViewer = super.createViewer(parent);

			// work-around 3: re-use source viewer from parent class, but add our reconsiler for syntax highlighting
			// by un-configuring and re-configuring the source viewer with a modified SourceViewerConfiguration
			final IContentAssistProcessor templateVariableProcessor = getTemplateProcessor();
			if (originalViewer instanceof final ISourceViewerExtension2 viewerExtension) {
				viewerExtension.unconfigure();

				final SourceViewerConfiguration configuration = new SourceViewerConfiguration() {
					@Override
					public @Nullable IContentAssistant getContentAssistant(@Nullable final ISourceViewer sourceViewer) {

						final ContentAssistant assistant = new ContentAssistant();
						assistant.enableAutoActivation(true);
						assistant.enableAutoInsert(true);
						assistant.setContentAssistProcessor(templateVariableProcessor, IDocument.DEFAULT_CONTENT_TYPE);
						return assistant;
					}

					@Override
					public IPresentationReconciler getPresentationReconciler(@Nullable final ISourceViewer sourceViewer) {
						// TODO Is there a hopefully easy way of adding highlighting for code template variables in all grammars?
						editTemplatePresentationReconsiler = new TMPresentationReconciler();
						return editTemplatePresentationReconsiler;
					}
				};

				originalViewer.configure(configuration);
			}

			return originalViewer;
		}
	}

	@Override
	protected boolean isShowFormatterSetting() {
		// Do not show a checkbox for formatting template code, since TM4E can't do that
		return false;
	}

	@Override
	protected @Nullable Template editTemplate(final @Nullable Template template, final boolean edit, final boolean isNameModifiable) {
		if (template == null) {
			return null;
		}

		final TMEditTemplateDialog dialog = new TMEditTemplateDialog(getShell(), template, edit, isNameModifiable,
				getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}

	@Override
	protected SourceViewer createViewer(final @Nullable Composite parent) {
		final SourceViewer viewer = new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		final SourceViewerConfiguration configuration = new SourceViewerConfiguration() {
			@Override
			public IPresentationReconciler getPresentationReconciler(@Nullable final ISourceViewer sourceViewer) {
				// TODO Is there a hopefully easy way of adding highlighting for code template variables in all grammars?
				previewReconsiler = new TMPresentationReconciler();
				return previewReconsiler;
			}
		};
		viewer.configure(configuration);
		final IDocument document = new Document();
		viewer.setDocument(document);
		getTableViewer().addSelectionChangedListener(e -> selectedCodeTemplateChanged());
		return viewer;
	}

	private void selectedCodeTemplateChanged() {
		final Template selectedTemplate = getSelectedTemplate();

		if (selectedTemplate != null) {

			final String id = selectedTemplate.getContextTypeId();
			final ITMScope scope = CodeTemplateContextTypeUtils.findScopeFor(id);

			if (scope != null) {
				final IGrammar languageGrammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarForScope(scope);
				if (languageGrammar != null && previewReconsiler != null) {
					previewReconsiler.setGrammar(languageGrammar);
					return;
				}
			}
		}
		if (previewReconsiler != null) {
			previewReconsiler.setGrammar(null);
		}
	}

	private @Nullable Template getSelectedTemplate() {
		final IStructuredSelection selection = getTableViewer().getStructuredSelection();

		if (selection != null && selection.size() == 1) {
			final TemplatePersistenceData data = (TemplatePersistenceData) selection.getFirstElement();
			if (data != null) {
				return data.getTemplate();
			}
		}
		return null;
	}

}
