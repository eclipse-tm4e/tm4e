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
package org.eclipse.tm4e.registry.internal.preferences;

import java.util.Collection;

import org.eclipse.tm4e.registry.GrammarDefinition;
import org.eclipse.tm4e.registry.IGrammarDefinition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

/**
 * Helper class load, save grammar preferences with Json format.
 *
 */
public final class PreferenceHelper {

	private static final Gson DEFAULT_GSON;

	static {
		DEFAULT_GSON = new GsonBuilder()
				.registerTypeAdapter(IGrammarDefinition.class, (InstanceCreator<GrammarDefinition>) type -> new GrammarDefinition())
				.create();
	}

	public static IGrammarDefinition[] loadGrammars(final String json) {
		return DEFAULT_GSON.fromJson(json, GrammarDefinition[].class);
	}

	public static String toJson(final Collection<IGrammarDefinition> definitions) {
		return DEFAULT_GSON.toJson(definitions);
	}

	private PreferenceHelper() {
	}
}
