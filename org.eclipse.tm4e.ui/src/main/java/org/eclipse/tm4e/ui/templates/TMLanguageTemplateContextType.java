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

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm4e.ui.TMUIPlugin;

/**
 * Language-specific default code template context type. It is created for each language with a registered TM4E grammar.
 */
public class TMLanguageTemplateContextType extends AbstractTMTemplateContextType {

	private static final String CONTEXT_TYPE_ID_PREFIX = TMUIPlugin.PLUGIN_ID + ".templates.context."; //$NON-NLS-1$

	public TMLanguageTemplateContextType(final String contextTypeName, final String contextTypeIdSuffix) {
		super(CONTEXT_TYPE_ID_PREFIX + contextTypeIdSuffix, contextTypeName);
	}

	public static String getContextTypeId(final String contextTypeIdSuffix) {
		Assert.isLegal(contextTypeIdSuffix != null && contextTypeIdSuffix.length() > 0);

		return CONTEXT_TYPE_ID_PREFIX + contextTypeIdSuffix;
	}
}
