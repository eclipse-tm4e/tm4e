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
 *
 */
package org.eclipse.tm4e.ui.internal.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Abstract class for wizard page.
 *
 */
abstract class AbstractWizardPage extends WizardPage implements Listener {

	protected AbstractWizardPage(final String pageName) {
		super(pageName);
	}

	protected AbstractWizardPage(final String pageName, final String title, final ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public final void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		// top level group
		final var topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		topLevel.setFont(parent.getFont());

		// Create Body UI
		createBody(topLevel);
		// initialize page with default values
		initializeDefaultValues();
		// Validate page fields.
		// validateAndUpdateStatus(null);
		setControl(topLevel);
	}

	@Override
	public void handleEvent(final Event event) {
		validateAndUpdateStatus(event);
	}

	private void validateAndUpdateStatus(final Event event) {
		final IStatus status = validatePage(event);
		statusChanged(status == null ? Status.OK_STATUS : status);
	}

	void statusChanged(final IStatus status) {
		setPageComplete(!status.matches(IStatus.ERROR));
		applyToStatusLine(this, status);
	}

	/**
	 * Applies the status to the status line of a dialog page.
	 */
	private static void applyToStatusLine(final DialogPage page, final IStatus status) {
		String message = Status.OK_STATUS.equals(status) ? null : status.getMessage();
		switch (status.getSeverity()) {
			case IStatus.OK:
				page.setMessage(message, IMessageProvider.NONE);
				page.setErrorMessage(null);
				break;
			case IStatus.WARNING:
				page.setMessage(message, IMessageProvider.WARNING);
				page.setErrorMessage(null);
				break;
			case IStatus.INFO:
				page.setMessage(message, IMessageProvider.INFORMATION);
				page.setErrorMessage(null);
				break;
			default:
				if (message != null && message.isEmpty()) {
					message = null;
				}
				page.setMessage(null);
				page.setErrorMessage(message);
				break;
		}
	}

	protected abstract void createBody(Composite parent);

	protected abstract void initializeDefaultValues();

	protected abstract @Nullable IStatus validatePage(Event event);

}
