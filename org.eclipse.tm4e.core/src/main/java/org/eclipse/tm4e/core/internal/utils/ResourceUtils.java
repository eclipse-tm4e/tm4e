/**
 * Copyright (c) 2024 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Sebastian Thomschke (Vegard IT) - initial implementation
 */
package org.eclipse.tm4e.core.internal.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.Nullable;

public final class ResourceUtils {

	public static BufferedReader getResourceReader(final Class<?> clazz, final String resourceName) {
		return getResourceReader(clazz, resourceName, null);
	}

	public static BufferedReader getResourceReader(final Class<?> clazz, final String resourceName,
			final @Nullable Charset charset) {
		final InputStream is = clazz.getResourceAsStream(resourceName);
		if (is == null)
			throw new IllegalArgumentException("Resource not found: " + resourceName);
		return new BufferedReader(new InputStreamReader(is, charset == null ? StandardCharsets.UTF_8 : charset));
	}

	private ResourceUtils() {
	}
}
