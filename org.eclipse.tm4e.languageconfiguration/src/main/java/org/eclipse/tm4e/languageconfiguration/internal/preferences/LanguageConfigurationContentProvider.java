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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm4e.languageconfiguration.internal.ILanguageConfigurationRegistryManager;

final class LanguageConfigurationContentProvider implements IStructuredContentProvider {

	private static final Object[] EMPTY = {};

	@Nullable
	private ILanguageConfigurationRegistryManager registry;

	@Override
	public Object[] getElements(@Nullable final Object input) {
		return registry != null ? registry.getDefinitions() : EMPTY;
	}

	@Override
	public void inputChanged(@Nullable final Viewer viewer, @Nullable final Object oldInput, @Nullable final Object newInput) {
		registry = (ILanguageConfigurationRegistryManager) newInput;
	}

	@Override
	public void dispose() {
		registry = null;
	}

}
