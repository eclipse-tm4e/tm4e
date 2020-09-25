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
package org.eclipse.tm4e.ui.internal.preferences;

import java.lang.reflect.Type;
import java.util.Collection;

import org.eclipse.tm4e.ui.themes.IThemeAssociation;
import org.eclipse.tm4e.ui.themes.ThemeAssociation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

/**
 * Helper class load, save theme preferences with Json format.
 *
 */
public class PreferenceHelper {

	private static final Gson DEFAULT_GSON;

	static {
		DEFAULT_GSON = new GsonBuilder().registerTypeAdapter(IThemeAssociation.class, new InstanceCreator<ThemeAssociation>() {
			@Override
			public ThemeAssociation createInstance(Type type) {
				return new ThemeAssociation();
			}
		}).create();
	}

	public static IThemeAssociation[] loadThemeAssociations(String json) {
		return DEFAULT_GSON.fromJson(json, ThemeAssociation[].class);
	}
	
	public static String toJsonThemeAssociations(Collection<IThemeAssociation> themeAssociations) {
		return DEFAULT_GSON.toJson(themeAssociations);
	}
}
