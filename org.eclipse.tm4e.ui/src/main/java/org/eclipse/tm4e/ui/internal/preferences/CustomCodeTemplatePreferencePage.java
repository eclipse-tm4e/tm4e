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
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public class CustomCodeTemplatePreferencePage extends TemplatePreferencePage {

	public CustomCodeTemplatePreferencePage() {
		final TMUIPlugin plugin = TMUIPlugin.getDefault();
		if (plugin == null) {
			return;
		}

		setPreferenceStore(plugin.getPreferenceStore());
		setTemplateStore(plugin.getTemplateStore());
		setContextTypeRegistry(TMUIPlugin.from(plugin.getTemplateContextRegistry()));
	}

	private static class Tm4eEditTemplateDialog extends TemplatePreferencePage.EditTemplateDialog {

		public Tm4eEditTemplateDialog(final Shell shell, final Template template, final boolean edit, final boolean isNameModifiable,
				final org.eclipse.jface.text.templates.ContextTypeRegistry contextTypeRegistry) {
			super(shell, template, edit, isNameModifiable, contextTypeRegistry);
		}

		@Override
		protected SourceViewer createViewer(final @Nullable Composite parent) {
			// TODO configure viewer
			return super.createViewer(parent);
		}
	}

	@Override
	protected String getFormatterPreferenceKey() {
		return super.getFormatterPreferenceKey();
		// TODO adapt pref key
		//return PreferenceConstants.TEMPLATES_USE_CODEFORMATTER;
	}

	@Override
	protected @Nullable Template editTemplate(final @Nullable Template template, final boolean edit, final boolean isNameModifiable) {
		if (template == null) {
			return null;
		}

		final Tm4eEditTemplateDialog dialog = new Tm4eEditTemplateDialog(getShell(), template, edit, isNameModifiable,
				getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}

	@Override
	protected SourceViewer createViewer(final @Nullable Composite parent) {
		final SourceViewer sourceViewer = super.createViewer(parent);

		// TODO configure source viewer for syntax highlighting with TM4E (adapt highlighting depending on context type)
//		sourceViewer.configure(new SourceViewerConfiguration() {
//			@Override
//			public IPresentationReconciler getPresentationReconciler(@Nullable final ISourceViewer sourceViewer) {
//				// TODO check if we need special config for highlighting template variables
//				return new TMPresentationReconciler();
//			}
//		});

		return sourceViewer;
	}

}
