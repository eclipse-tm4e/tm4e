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
package org.eclipse.tm4e.ui.templates;

import org.eclipse.tm4e.ui.TMUIPlugin;

/**
 * Language-independent code template context type for documentation comments, e.g. for javadoc comments.
 */
public class DocumentationCommentTemplateContextType extends AbstractTMTemplateContextType {

	public static final String CONTEXT_ID = TMUIPlugin.PLUGIN_ID + ".templates.context.comment.doc"; //$NON-NLS-1$

	public DocumentationCommentTemplateContextType() {
		super(CONTEXT_ID, "Documentation Comment");
	}

}
