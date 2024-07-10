/**
 * Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.preferences;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.lateNonNull;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm4e.ui.TMUIPlugin;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.osgi.service.prefs.BackingStoreException;

/**
 * TextMate Global preferences page.
 */
public final class TextMatePreferencePage extends AbstractPreferencePage {

	private Button tmTokenHoverCheckbox = lateNonNull();

	public TextMatePreferencePage() {
		super(null, null);
	}

	@Override
	protected Control createContents(final Composite parent) {
		final var composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());

		addRelatedLinks(composite);

		new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		tmTokenHoverCheckbox = new Button(composite, SWT.CHECK);
		tmTokenHoverCheckbox.setText(TMUIMessages.TextMatePreferencePage_ShowTextMateTokenInfoHover);
		tmTokenHoverCheckbox.setSelection(PreferenceHelper.isTMTokenHoverEnabled());

		applyDialogFont(composite);
		return composite;
	}

	private void addRelatedLinks(final Composite parent) {
		// Add link to grammar preference page
		addRelatedLink(parent, GrammarPreferencePage.PAGE_ID, TMUIMessages.TextMatePreferencePage_GrammarRelatedLink);

		// Add link to language configuration preference page
		if (Platform.getBundle("org.eclipse.tm4e.languageconfiguration") != null) {
			addRelatedLink(parent,
					"org.eclipse.tm4e.languageconfiguration.preferences.LanguageConfigurationPreferencePage",
					TMUIMessages.TextMatePreferencePage_LanguageConfigurationRelatedLink);
		}

		// Add link to task tags preference page
		addRelatedLink(parent, TaskTagsPreferencePage.PAGE_ID, TMUIMessages.TextMatePreferencePage_TaskTagsRelatedLink);

		// Add link to theme preference page
		addRelatedLink(parent, ThemePreferencePage.PAGE_ID, TMUIMessages.TextMatePreferencePage_ThemeRelatedLink);
	}

	private void addRelatedLink(final Composite parent, final String pageId, final String message) {
		final var contentTypeArea = new PreferenceLinkArea(parent, SWT.NONE, pageId, message,
				(IWorkbenchPreferenceContainer) getContainer(), null);

		contentTypeArea.getControl()
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
	}

	@Override
	public void init(final IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		tmTokenHoverCheckbox.setSelection(PreferenceHelper.isTMTokenHoverEnabled());
	}

	@Override
	public boolean performOk() {
		try {
			PreferenceHelper.saveTMTokenHoverEnabled(tmTokenHoverCheckbox.getSelection());
		} catch (final BackingStoreException ex) {
			TMUIPlugin.logError(ex);
			return false;
		}
		return super.performOk();
	}
}
