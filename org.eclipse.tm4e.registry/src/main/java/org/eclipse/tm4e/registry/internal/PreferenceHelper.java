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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.registry.GrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarDefinition;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

/**
 * Helper class load, save grammar preferences with JSON format.
 */
final class PreferenceHelper {

	private static final String GRAMMARS = "org.eclipse.tm4e.registry.grammars";

	private static final Gson DEFAULT_GSON = new GsonBuilder()
			.registerTypeAdapter(IGrammarDefinition.class,
					(InstanceCreator<GrammarDefinition>) type -> new GrammarDefinition())
			.create();

	static @Nullable List<IGrammarDefinition> loadGrammars() {
		final var json = TMEclipseRegistryPlugin.getPreference(GRAMMARS, null);
		if (json == null)
			return null;
		return DEFAULT_GSON.fromJson(json, new TypeToken<ArrayList<GrammarDefinition>>() {
		}.getType());
	}

	static void saveGrammars(Collection<IGrammarDefinition> definitions) throws BackingStoreException {
		// Save grammar definitions in the
		// "${workspace_loc}/metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.tm4e.registry.prefs"
		final var json = DEFAULT_GSON.toJson(definitions);
		final var prefs = InstanceScope.INSTANCE.getNode(TMEclipseRegistryPlugin.PLUGIN_ID);
		prefs.put(GRAMMARS, json);
		prefs.flush();
	}

	private PreferenceHelper() {
	}
}
