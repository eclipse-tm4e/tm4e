/**
 *  Copyright (c) 2018 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Lucas Bullen (Red Hat Inc.) - initial API and implementation
 */
package org.eclipse.tm4e.languageconfiguration.internal.wizards;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
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
import org.eclipse.tm4e.languageconfiguration.ILanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationRegistryManager;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfiguration;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationMessages;
import org.eclipse.tm4e.languageconfiguration.internal.LanguageConfigurationPlugin;
import org.eclipse.tm4e.languageconfiguration.internal.widgets.LanguageConfigurationInfoWidget;
import org.eclipse.tm4e.ui.utils.ContentTypeHelper;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;

public class SelectLanguageConfigurationWizardPage extends WizardPage implements Listener {
	private static final String PAGE_NAME = SelectLanguageConfigurationWizardPage.class.getName();

	protected static final String[] TEXTMATE_EXTENSIONS = { "*language-configuration.json" }; //$NON-NLS-1$

	private Button browseFileSystemButton;
	private Button browseWorkspaceButton;
	private Text fileText;
	private Text contentTypeText;
	private LanguageConfigurationInfoWidget infoWidget;

	private ILanguageConfigurationRegistryManager registryManager;

	protected SelectLanguageConfigurationWizardPage(String pageName) {
		super(pageName);
	}

	public SelectLanguageConfigurationWizardPage(ILanguageConfigurationRegistryManager registryManager) {
		super(PAGE_NAME);
		this.registryManager = registryManager;
		super.setTitle(LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_page_title);
		super.setDescription(LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_page_description);
	}

	@Override
	public final void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		topLevel.setFont(parent.getFont());

		createBody(topLevel);
		setPageComplete(false);
		setControl(topLevel);
	}

	@Override
	public void handleEvent(Event event) {
		validateAndUpdateStatus(event);
	}

	private void validateAndUpdateStatus(Event event) {
		IStatus status = validatePage(event);
		statusChanged(status == null ? Status.OK_STATUS : status);
	}

	public void statusChanged(IStatus status) {
		setPageComplete(!status.matches(IStatus.ERROR));
		applyToStatusLine(this, status);
	}

	private static void applyToStatusLine(DialogPage page, IStatus status) {
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
			if (message != null && message.length() == 0) {
				message = null;
			}
			page.setMessage(null);
			page.setErrorMessage(message);
			break;
		}
	}

	protected void createBody(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		parent.setFont(parent.getFont());
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parent.setLayout(new GridLayout(2, false));

		fileText = createText(parent, LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_file);
		fileText.addListener(SWT.Modify, this);

		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		buttons.setLayoutData(gd);

		infoWidget = new LanguageConfigurationInfoWidget(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		infoWidget.setLayoutData(data);

		browseFileSystemButton = new Button(buttons, SWT.NONE);
		browseFileSystemButton
				.setText(LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_browse_fileSystem);
		browseFileSystemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setFilterExtensions(TEXTMATE_EXTENSIONS);
				dialog.setFilterPath(fileText.getText());
				String result = dialog.open();
				if (result != null && result.length() > 0) {
					fileText.setText(result);
				}
			}
		});

		browseWorkspaceButton = new Button(buttons, SWT.NONE);
		browseWorkspaceButton
				.setText(LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_browse_workspace);
		browseWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ResourceSelectionDialog dialog = new ResourceSelectionDialog(browseWorkspaceButton.getShell(),
						ResourcesPlugin.getWorkspace().getRoot(),
						LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_workspace_description);
				dialog.setTitle(LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_workspace_title);
				int returnCode = dialog.open();
				Object[] results = dialog.getResult();
				if (returnCode == 0 && results.length > 0) {
					fileText.setText(((IResource) results[0]).getFullPath().makeRelative().toString());
				}
			}
		});
		contentTypeText = createText(parent,
				LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_contentType);
		contentTypeText.addListener(SWT.Modify, this);
		createContentTypeTreeViewer(parent);
	}

	private void createContentTypeTreeViewer(Composite composite) {
		TreeViewer contentTypesViewer = new TreeViewer(composite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		contentTypesViewer.getControl().setFont(composite.getFont());
		contentTypesViewer.setContentProvider(new ContentTypesContentProvider());
		contentTypesViewer.setLabelProvider(new ContentTypesLabelProvider());
		contentTypesViewer.setComparator(new ViewerComparator());
		contentTypesViewer.setInput(Platform.getContentTypeManager());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		contentTypesViewer.getControl().setLayoutData(data);

		contentTypesViewer.addSelectionChangedListener(event -> contentTypeText
				.setText(((IContentType) event.getStructuredSelection().getFirstElement()).toString()));
	}

	private static class ContentTypesLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			IContentType contentType = (IContentType) element;
			return contentType.getName();
		}
	}

	private static class ContentTypesContentProvider implements ITreeContentProvider {

		private IContentTypeManager manager = Platform.getContentTypeManager();

		@Override
		public Object[] getChildren(Object parentElement) {
			List<Object> elements = new ArrayList<>();
			IContentType baseType = (IContentType) parentElement;
			for (IContentType contentType : manager.getAllContentTypes()) {
				if ((contentType.getBaseType() == null && baseType == null)
						|| ((contentType.getBaseType() != null && contentType.getBaseType().equals(baseType)))) {
					elements.add(contentType);
				}
			}
			return elements.toArray();
		}

		@Override
		public Object getParent(Object element) {
			IContentType contentType = (IContentType) element;
			return contentType.getBaseType();
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(null);
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			manager = (IContentTypeManager) newInput;
		}
	}

	private Text createText(Composite parent, String s) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText(s);

		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}

	protected IStatus validatePage(Event event) {
		infoWidget.refresh(null);
		String path = fileText.getText();
		if (path.length() == 0) {
			return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
					LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_fileError_noSelection);
		}
		IPath p = new Path(path);
		if (!p.isAbsolute()) {
			p = ResourcesPlugin.getWorkspace().getRoot().getFile(p).getLocation();
		}
		try {
			ILanguageConfiguration configuration = LanguageConfiguration.load(new FileReader(p.toFile()));
			if (configuration == null) {
				return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
						LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_fileError_invalid);
			}
			infoWidget.refresh(configuration);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
					LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_fileError_error
							+ e.getLocalizedMessage());
		}
		if (contentTypeText.getText().isEmpty()) {
			return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
					LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_contentTypeError_noSelection);
		}
		IContentType contentType = ContentTypeHelper.getContentTypeById(contentTypeText.getText());
		if (contentType == null) {
			return new Status(IStatus.ERROR, LanguageConfigurationPlugin.PLUGIN_ID,
					LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_contentTypeError_invalid);
		}
		if (registryManager.getLanguageConfigurationFor(new IContentType[] { contentType }) != null) {
			return new Status(IStatus.WARNING, LanguageConfigurationPlugin.PLUGIN_ID,
					LanguageConfigurationMessages.SelectLanguageConfigurationWizardPage_contentTypeWarning_duplicate);
		}
		return null;
	}

	public ILanguageConfigurationDefinition getDefinition() {
		IPath p = new Path(fileText.getText());
		if (!p.isAbsolute()) {
			p = ResourcesPlugin.getWorkspace().getRoot().getFile(p).getLocation();
		}
		return new LanguageConfigurationDefinition(ContentTypeHelper.getContentTypeById(contentTypeText.getText()),
				p.toString());
	}

}
