/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.ui.internal.widgets;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;

/**
 * A content provider for the template grammar page's table viewer.
 * 
 */
public final class GrammarDefinitionContentProvider implements IStructuredContentProvider {

	private IGrammarRegistryManager registry;

	@Override
	public Object[] getElements(final Object input) {
		return registry.getDefinitions();
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		registry = (IGrammarRegistryManager) newInput;
	}

	@Override
	public void dispose() {
		registry = null;
	}

}
