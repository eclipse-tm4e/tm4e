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
package org.eclipse.tm4e.registry.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tm4e.registry.GrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.registry.XMLConstants;
import org.eclipse.tm4e.registry.internal.preferences.PreferenceConstants;
import org.eclipse.tm4e.registry.internal.preferences.PreferenceHelper;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Grammar registry manager singleton.
 */
public class GrammarRegistryManager extends AbstractGrammarRegistryManager {

	private static final String EXTENSION_GRAMMARS = "grammars";

	private static GrammarRegistryManager INSTANCE;

	public static GrammarRegistryManager getInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		INSTANCE = createInstance();
		return INSTANCE;
	}

	private static synchronized GrammarRegistryManager createInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		GrammarRegistryManager manager = new GrammarRegistryManager();
		manager.load();
		return manager;
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
		IConfigurationElement[] cf = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(TMEclipseRegistryPlugin.PLUGIN_ID, EXTENSION_GRAMMARS);
		for (IConfigurationElement ce : cf) {
			String extensionName = ce.getName();
			if (XMLConstants.GRAMMAR_ELT.equals(extensionName)) {
				super.registerGrammarDefinition(new GrammarDefinition(ce));
			} else if (XMLConstants.INJECTION_ELT.equals(extensionName)) {
				String scopeName = ce.getAttribute(XMLConstants.SCOPE_NAME_ATTR);
				String injectTo = ce.getAttribute(XMLConstants.INJECT_TO_ATTR);
				super.registerInjection(scopeName, injectTo);
			} else if (XMLConstants.SCOPE_NAME_CONTENT_TYPE_BINDING_ELT.equals(extensionName)) {
				String contentTypeId = ce.getAttribute(XMLConstants.CONTENT_TYPE_ID_ATTR);
				String scopeName = ce.getAttribute(XMLConstants.SCOPE_NAME_ATTR);
				super.registerContentTypeBinding(contentTypeId, scopeName);
			}
		}
	}

	/**
	 * Load TextMate grammars from preferences.
	 */
	private void loadGrammarsFromPreferences() {
		// Load grammar definitions from the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.registry.prefs"
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(TMEclipseRegistryPlugin.PLUGIN_ID);
		String json = prefs.get(PreferenceConstants.GRAMMARS, null);
		if (json != null) {
			IGrammarDefinition[] definitions = PreferenceHelper.loadGrammars(json);
			for (IGrammarDefinition definition : definitions) {
				userCache.registerGrammarDefinition(definition);
			}
		}
	}

	@Override
	public void save() throws BackingStoreException {
		// Save grammar definitions in the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.registry.prefs"
		String json = PreferenceHelper.toJson(userCache.getDefinitions());
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(TMEclipseRegistryPlugin.PLUGIN_ID);
		prefs.put(PreferenceConstants.GRAMMARS, json);
		prefs.flush();
	}

}
