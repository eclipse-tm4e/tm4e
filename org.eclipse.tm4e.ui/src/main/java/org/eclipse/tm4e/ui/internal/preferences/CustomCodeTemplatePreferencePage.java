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
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.tm4e.core.grammar.IGrammar;
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

		public TMEditTemplateDialog(final Shell shell, final Template template, final boolean edit, final boolean isNameModifiable,
				@SuppressWarnings("deprecation") final org.eclipse.jface.text.templates.ContextTypeRegistry contextTypeRegistry) {
			super(shell, template, edit, isNameModifiable, contextTypeRegistry);
		}

		@Override
		protected SourceViewer createViewer(final @Nullable Composite parent) {
			// TODO Can we adapt the SourceViewerConfiguration from super.createViewer and add our own presentation reconsiler that adapts to the selected context type?
			return super.createViewer(parent);
		}
	}

	@Override
	protected String getFormatterPreferenceKey() {
		return PreferenceConstants.TEMPLATES_USE_CODEFORMATTER;
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
