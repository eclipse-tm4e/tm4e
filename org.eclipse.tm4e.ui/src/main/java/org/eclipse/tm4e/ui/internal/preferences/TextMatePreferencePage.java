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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm4e.ui.internal.TMUIMessages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * TextMate Global preferences page.
 *
 */
public final class TextMatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	@Override
	protected Control createContents(@Nullable final Composite parent) {
		final var composite = new Composite(parent, SWT.NONE);
		final var layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);

		// Add link to grammar preference page
		addRelatedLink(composite, GrammarPreferencePage.PAGE_ID, TMUIMessages.TextMatePreferencePage_GrammarRelatedLink);

		// Add link to language configuration preference page
		if (Platform.getBundle("org.eclipse.tm4e.languageconfiguration") != null) { //$NON-NLS-1$
			addRelatedLink(composite,
					"org.eclipse.tm4e.languageconfiguration.preferences.LanguageConfigurationPreferencePage", //$NON-NLS-1$
					TMUIMessages.TextMatePreferencePage_LanguageConfigurationRelatedLink);
		}

		// Add link to task tags preference page
		addRelatedLink(composite, TaskTagsPreferencePage.PAGE_ID, TMUIMessages.TextMatePreferencePage_TaskTagsRelatedLink);

		// Add link to theme preference page
		addRelatedLink(composite, ThemePreferencePage.PAGE_ID, TMUIMessages.TextMatePreferencePage_ThemeRelatedLink);

		applyDialogFont(composite);
		return composite;

	}

	private void addRelatedLink(final Composite parent, final String pageId, final String message) {
		final var contentTypeArea = new PreferenceLinkArea(parent, SWT.NONE, pageId, message,
				(IWorkbenchPreferenceContainer) getContainer(), null);

		final var data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		contentTypeArea.getControl().setLayoutData(data);
	}

	@Override
	public void init(@Nullable final IWorkbench workbench) {
	}
}
