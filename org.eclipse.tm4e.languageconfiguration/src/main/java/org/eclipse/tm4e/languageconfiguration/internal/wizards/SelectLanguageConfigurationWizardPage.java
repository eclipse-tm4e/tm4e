/**
 * Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Lucas Bullen (Red Hat Inc.) - initial API and implementation
 * Sebastian Thomschke (Vegard IT) - fixed content type selection
 */
package org.eclipse.tm4e.languageconfiguration.internal.wizards;

import static org.eclipse.tm4e.core.internal.utils.NullSafetyHelper.*;
import static org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages.*;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm4e.languageconfiguration.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.model.LanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.widgets.LanguageConfigurationInfoWidget;
import org.eclipse.tm4e.ui.internal.utils.ContentTypeHelper;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;

final class SelectLanguageConfigurationWizardPage extends WizardPage implements Listener {
	private static final String PAGE_NAME = SelectLanguageConfigurationWizardPage.class.getName();

	private static final String[] TEXTMATE_EXTENSIONS = { "*language-configuration.json" }; //$NON-NLS-1$

	private Text fileText = lateNonNull();
	private Text contentTypeText = lateNonNull();
	private LanguageConfigurationInfoWidget infoWidget = lateNonNull();

	private final ILanguageConfigurationRegistryManager registryManager;

	SelectLanguageConfigurationWizardPage(final ILanguageConfigurationRegistryManager registryManager) {
		super(PAGE_NAME);
		this.registryManager = registryManager;
		super.setTitle(SelectLanguageConfigurationWizardPage_page_title);
		super.setDescription(SelectLanguageConfigurationWizardPage_page_description);
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final var topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		topLevel.setFont(parent.getFont());

		createBody(topLevel);
		setPageComplete(false);
		setControl(topLevel);
	}

	@Override
	public void handleEvent(final Event event) {
		validateAndUpdateStatus();
	}

	private void validateAndUpdateStatus() {
		final IStatus status = validatePage();
		statusChanged(status == null ? Status.OK_STATUS : status);
	}

	private void statusChanged(final IStatus status) {
		setPageComplete(!status.matches(IStatus.ERROR));
		applyToStatusLine(this, status);
	}

	private static void applyToStatusLine(final DialogPage page, final IStatus status) {
		final var message = Status.OK_STATUS.equals(status) ? null : status.getMessage();
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
				page.setMessage(null);
				page.setErrorMessage(message != null && message.isEmpty() ? null : message);
				break;
		}
	}

	private void createBody(final Composite ancestor) {
		final var parent = new Composite(ancestor, SWT.NONE);
		parent.setFont(parent.getFont());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parent.setLayout(new GridLayout(2, false));

		fileText = createText(parent, SelectLanguageConfigurationWizardPage_file);
		fileText.addListener(SWT.Modify, this);

		final var buttons = new Composite(parent, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		final var gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		buttons.setLayoutData(gd);

		infoWidget = new LanguageConfigurationInfoWidget(parent, SWT.NONE);
		final var data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		infoWidget.setLayoutData(data);

		final var browseFileSystemButton = new Button(buttons, SWT.NONE);
		browseFileSystemButton.setText(SelectLanguageConfigurationWizardPage_browse_fileSystem);
		browseFileSystemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final var dialog = new FileDialog(parent.getShell());
				dialog.setFilterExtensions(TEXTMATE_EXTENSIONS);
				dialog.setFilterPath(fileText.getText());
				final String result = dialog.open();
				if (result != null && !result.isEmpty()) {
					fileText.setText(result);
				}
			}
		});

		final var browseWorkspaceButton = new Button(buttons, SWT.NONE);
		browseWorkspaceButton.setText(SelectLanguageConfigurationWizardPage_browse_workspace);
		browseWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final var dialog = new ResourceSelectionDialog(browseWorkspaceButton.getShell(),
						ResourcesPlugin.getWorkspace().getRoot(),
						SelectLanguageConfigurationWizardPage_workspace_description);
				dialog.setTitle(SelectLanguageConfigurationWizardPage_workspace_title);
				final int returnCode = dialog.open();
				final Object[] results = dialog.getResult();
				if (returnCode == 0 && results.length > 0) {
					fileText.setText(((IResource) results[0]).getFullPath().makeRelative().toString());
				}
			}
		});
		contentTypeText = createText(parent, SelectLanguageConfigurationWizardPage_contentType);
		contentTypeText.addListener(SWT.Modify, this);
		createContentTypeTreeViewer(parent);
	}

	private void createContentTypeTreeViewer(final Composite composite) {
		final var contentTypesViewer = new TreeViewer(composite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		contentTypesViewer.getControl().setFont(composite.getFont());
		contentTypesViewer.setContentProvider(new ContentTypesContentProvider());
		contentTypesViewer.setLabelProvider(new ContentTypesLabelProvider());
		contentTypesViewer.setComparator(new ViewerComparator());
		contentTypesViewer.setInput(Platform.getContentTypeManager());
		final var data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		contentTypesViewer.getControl().setLayoutData(data);

		contentTypesViewer.addSelectionChangedListener(event -> {
			contentTypeText.setText(event.getStructuredSelection().getFirstElement() instanceof IContentType ct ? ct.toString() : "");
		});
	}

	private static final class ContentTypesLabelProvider extends LabelProvider {
		@Override
		public @Nullable String getText(final @Nullable Object element) {
			return element == null
					? ""
					: ((IContentType) element).getName();
		}
	}

	private static final class ContentTypesContentProvider implements ITreeContentProvider {

		private IContentTypeManager manager = Platform.getContentTypeManager();

		@Override
		public Object[] getChildren(final @Nullable Object parentElement) {
			final var elements = new ArrayList<>();
			final var baseType = (IContentType) parentElement;
			for (final var contentType : manager.getAllContentTypes()) {
				if (Objects.equals(baseType, contentType.getBaseType())) {
					elements.add(contentType);
				}
			}
			return elements.toArray();
		}

		@Override
		public @Nullable Object getParent(final @Nullable Object element) {
			if (element == null)
				return null;
			final IContentType contentType = (IContentType) element;
			return contentType.getBaseType();
		}

		@Override
		public boolean hasChildren(final @Nullable Object element) {
			return getChildren(element).length > 0;
		}

		@Override
		public Object[] getElements(final @Nullable Object inputElement) {
			return getChildren(manager.getContentType(IContentTypeManager.CT_TEXT));
		}

		@Override
		public void inputChanged(final Viewer viewer, final @Nullable Object oldInput, final @Nullable Object newInput) {
			manager = newInput == null ? Platform.getContentTypeManager() : (IContentTypeManager) newInput;
		}
	}

	private Text createText(final Composite parent, final String s) {
		final var label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText(s);

		final var text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}

	private @Nullable IStatus validatePage() {
		infoWidget.refresh(null);

		final String path = fileText.getText();
		if (path.length() == 0) {
			return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
					SelectLanguageConfigurationWizardPage_fileError_noSelection);
		}
		IPath p = new Path(path);
		if (!p.isAbsolute()) {
			p = castNonNull(ResourcesPlugin.getWorkspace().getRoot().getFile(p).getLocation());
		}
		try (var file = new FileReader(p.toFile())) {
			final var configuration = LanguageConfiguration.load(file);
			if (configuration == null) {
				return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
						SelectLanguageConfigurationWizardPage_fileError_invalid);
			}
			infoWidget.refresh(configuration);
		} catch (final Exception e) {
			return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
					SelectLanguageConfigurationWizardPage_fileError_error + e.getLocalizedMessage());
		}

		if (contentTypeText.getText().isEmpty()) {
			return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
					SelectLanguageConfigurationWizardPage_contentTypeError_noSelection);
		}
		final var contentType = ContentTypeHelper.getContentTypeById(contentTypeText.getText());
		if (contentType == null) {
			return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
					SelectLanguageConfigurationWizardPage_contentTypeError_invalid);
		}
		if (registryManager.getLanguageConfigurationFor(contentType) != null) {
			return new Status(IStatus.WARNING, LanguageConfigurationPlugin.PLUGIN_ID,
					SelectLanguageConfigurationWizardPage_contentTypeWarning_duplicate);
		}
		return null;
	}

	ILanguageConfigurationDefinition getDefinition() {
		IPath path = new Path(fileText.getText());
		if (!path.isAbsolute()) {
			path = castNonNull(ResourcesPlugin.getWorkspace().getRoot().getFile(path).getLocation());
		}

		final var contentType = castNonNull(ContentTypeHelper.getContentTypeById(contentTypeText.getText()));
		return new LanguageConfigurationDefinition(contentType, path.toString());
	}
}
