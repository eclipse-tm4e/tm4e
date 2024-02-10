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
package org.eclipse.tm4e.registry.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm4e.registry.GrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.registry.XMLConstants;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Grammar registry manager singleton.
 */
public final class GrammarRegistryManager extends AbstractGrammarRegistryManager {

	private static final String EXTENSION_GRAMMARS = "grammars";

	/** see https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java */
	private static final class InstanceHolder {
		static final GrammarRegistryManager INSTANCE = new GrammarRegistryManager();
		static {
			INSTANCE.load();
		}
	}

	public static GrammarRegistryManager getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private GrammarRegistryManager() {
	}

	private void load() {
		loadGrammarsFromExtensionPoints();
		loadGrammarsFromPreferences();
	}

	/**
	 * Load TextMate grammars from extension point.
	 */
	private void loadGrammarsFromExtensionPoints() {
		final IConfigurationElement[] cf = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(TMEclipseRegistryPlugin.PLUGIN_ID, EXTENSION_GRAMMARS);
		for (final IConfigurationElement ce : cf) {
			final String extensionName = ce.getName();
			switch (extensionName) {
				case XMLConstants.GRAMMAR_ELT:
					registerGrammarDefinition(new GrammarDefinition(ce));
					break;
				case XMLConstants.INJECTION_ELT: {
					final String scopeName = ce.getAttribute(XMLConstants.SCOPE_NAME_ATTR);
					final String injectTo = ce.getAttribute(XMLConstants.INJECT_TO_ATTR);
					registerInjection(scopeName, injectTo);
					break;
				}
				case XMLConstants.SCOPE_NAME_CONTENT_TYPE_BINDING_ELT: {
					final String contentTypeId = ce.getAttribute(XMLConstants.CONTENT_TYPE_ID_ATTR);
					final IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
					if (contentType == null) {
						Platform.getLog(getClass())
								.warn("No content-type found with id='" + contentTypeId + "', ignoring TM4E association.");
					} else {
						final String scopeName = ce.getAttribute(XMLConstants.SCOPE_NAME_ATTR);
						registerContentTypeToScopeBinding(ce.getNamespaceIdentifier(), contentType, scopeName);
					}
					break;
				}
			}
		}
	}

	private void loadGrammarsFromPreferences() {
		final var definitions = PreferenceHelper.loadGrammars();
		if (definitions != null) {
			for (final IGrammarDefinition definition : definitions) {
				userDefinitions.add(definition);
			}
		}
	}

	void save() throws BackingStoreException {
		PreferenceHelper.saveGrammars(userDefinitions.stream().toList());
	}

	@Override
	public IGrammarRegistryManager.EditSession newEditSession() {
		return new WorkingCopyGrammarRegistryManager(this);
	}
}
