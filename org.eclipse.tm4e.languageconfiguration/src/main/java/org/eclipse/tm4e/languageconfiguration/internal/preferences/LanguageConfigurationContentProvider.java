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
package org.eclipse.tm4e.languageconfiguration.internal.preferences;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm4e.languageconfiguration.ILanguageConfigurationRegistryManager;

public class LanguageConfigurationContentProvider implements IStructuredContentProvider {

	private ILanguageConfigurationRegistryManager registry;

	@Override
	public Object[] getElements(Object input) {
		return registry.getDefinitions();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		registry = (ILanguageConfigurationRegistryManager) newInput;
	}

	@Override
	public void dispose() {
		registry = null;
	}

}
