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

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Default implementation for new, language-specific code template context types.
 */
public abstract class AbstractTMTemplateContextType extends TemplateContextType {

	public AbstractTMTemplateContextType(final String contextTypeId, final String contextTypeName) {
		super(contextTypeId, contextTypeName);
		addTemplateVariableResolvers();
	}

	/**
	 * Adds {@link TemplateVariableResolver}s.
	 * Subclasses may override this method, but should call <code>super.addTemplateVariableResolvers()</code>.
	 * The default implementation adds some global template variables like <code>${user}</code>,
	 * <code>${date}</code>, and <code>${cursor}</code> (see {@link #addGlobalTemplateVariableResolvers()}).
	 */
	protected void addTemplateVariableResolvers() {
		addGlobalTemplateVariableResolvers();
	}

	/**
	 * Adds {@link TemplateVariableResolver}s for some global template variables like
	 * <code>${user}</code> (user name),
	 * <code>${date}</code>, <code>${time}</code>, <code>${year}</code>, <code>${cursor}</code>,
	 * <code>${lineselection}</code>, and <code>${wordselection}</code>.
	 */
	protected final void addGlobalTemplateVariableResolvers() {
		addResolver(new GlobalTemplateVariables.User());

		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.Year());

		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.WordSelection());

		addResolver(new GlobalTemplateVariables.Dollar());
	}

}
