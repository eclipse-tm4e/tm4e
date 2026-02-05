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

import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.ui.internal.utils.CodeTemplateContextTypeUtils;

/**
 * Language-specific default code template context type. It is created for each language with a registered TM4E grammar.
 */
public class TMLanguageTemplateContextType extends AbstractTMTemplateContextType {

	// TODO What could be a good approach for adding language-specific code template variables (resolvers)?
	public TMLanguageTemplateContextType(final String contextTypeName, final ITMScope languageScope) {
		super(CodeTemplateContextTypeUtils.toContextTypeId(languageScope), contextTypeName);
	}

}
