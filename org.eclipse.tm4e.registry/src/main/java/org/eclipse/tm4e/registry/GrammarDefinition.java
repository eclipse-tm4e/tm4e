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
 */
package org.eclipse.tm4e.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Grammar definition defined by the "org.eclipse.tm4e.registry.grammars"
 * extension point. Here a sample to register TypeScript TextMate grammar.
 *
 * <pre>
 * <extension
         point="org.eclipse.tm4e.registry.grammars">
      <grammar
            scopeName="source.ts"
            path="./syntaxes/TypeScript.tmLanguage.json" >
      </grammar>
   </extension>
 * </pre>
 *
 */
public class GrammarDefinition extends TMResource implements IGrammarDefinition {

	@Nullable
	private String scopeName;

	/**
	 * Constructor for user preferences (loaded from Json with Gson).
	 */
	public GrammarDefinition() {
	}

	/**
	 * Constructor for extension point.
	 *
	 * @param scopeName
	 */
	public GrammarDefinition(final String scopeName, final String path) {
		super(path);
		this.scopeName = scopeName;
	}

	public GrammarDefinition(final IConfigurationElement ce) {
		super(ce);
		this.scopeName = ce.getAttribute(XMLConstants.SCOPE_NAME_ATTR);
	}

	@Override
	public String getScopeName() {
		assert scopeName != null;
		return scopeName;
	}
}
