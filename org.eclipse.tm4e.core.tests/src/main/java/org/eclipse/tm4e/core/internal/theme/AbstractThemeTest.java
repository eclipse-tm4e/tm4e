/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.internal.theme;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.registry.IThemeSource;

public abstract class AbstractThemeTest extends org.assertj.core.api.Assertions {

	protected static final int _NOT_SET = 0;

	protected static <T> List<T> list(@SuppressWarnings("unchecked") final T... items) {
		if (items.length == 0)
			return Collections.emptyList();
		return Arrays.asList(items);
	}

	@SuppressWarnings("unchecked")
	protected static <K, V> Map<K, V> map(final K k, final V v, final Object... moreKVs) {
		final var map = new HashMap<K, V>();
		map.put(k, v);
		if (moreKVs.length == 0)
			return map;
		boolean nextIsValue = false;
		K key = null;
		for (final Object obj : moreKVs)
			if (nextIsValue) {
				map.put(key, (V) obj);
				nextIsValue = false;
			} else {
				key = (K) obj;
				nextIsValue = true;
			}
		return map;
	}

	protected static Theme createTheme(final ParsedThemeRule... rules) {
		return Theme.createFromParsedTheme(list(rules), null);
	}

	protected static Theme createTheme(final String themeAsJsonString) throws Exception {
		return Theme.createFromRawTheme(RawThemeReader.readTheme(
				IThemeSource.fromString(IThemeSource.ContentType.JSON, themeAsJsonString)),
				null);
	}

	protected static List<ParsedThemeRule> parseTheme(final String themeAsJsonString) throws Exception {
		return Theme.parseTheme(RawThemeReader.readTheme(
				IThemeSource.fromString(IThemeSource.ContentType.JSON, themeAsJsonString)));
	}
}
