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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.jar.JarEntry;

import org.eclipse.jdt.annotation.Nullable;

public final class ResourceUtils {

	/**
	 * @throws FileNotFoundException if the resource isn't found on the classpath
	 */
	public static BufferedReader getResourceReader(final Class<?> clazz, final String resourceName) throws FileNotFoundException {
		return getResourceReader(clazz, resourceName, null);
	}

	/**
	 * @throws FileNotFoundException if the resource isn't found on the classpath
	 */
	public static BufferedReader getResourceReader(final Class<?> clazz, final String resourceName,
			final @Nullable Charset charset) throws FileNotFoundException {
		final InputStream is = clazz.getResourceAsStream(resourceName);
		if (is == null)
			throw new FileNotFoundException("Resource not found: " + resourceName);
		return new BufferedReader(new InputStreamReader(is, charset == null ? StandardCharsets.UTF_8 : charset));
	}

	/**
	 * Returns last modified (ms since epoch) for a classpath resource.
	 *
	 * @return milliseconds since the epoch, or 0 if the timestamp is unavailable
	 *
	 * @throws IOException on I/O errors
	 * @throws FileNotFoundException if the resource isn't found on the classpath
	 */
	public static long getResourceLastModified(final Class<?> clazz, final String resourceName) throws IOException {
		final URL url = clazz.getResource(resourceName);
		if (url == null) {
			throw new FileNotFoundException("Resource not found: " + resourceName);
		}

		final String protocol = url.getProtocol();
		switch (protocol) {
			case "file":
				try {
					return new File(url.toURI()).lastModified();
				} catch (final URISyntaxException ex) {
					return new File(url.getFile()).lastModified();
				}

			case "jar":
				final var jarConn = (JarURLConnection) url.openConnection();
				final JarEntry entry = jarConn.getJarEntry();
				if (entry != null) {
					final long t = entry.getTime();
					return t > 0 ? t : 0L; // normalize "unknown" (-1) to 0
				}
				return jarConn.getLastModified();

			default:
				return url.openConnection().getLastModified();
		}
	}

	/**
	 * Return a URI for the given classpath resource.
	 * - If the resource exists (i.e., clazz.getResource(resourceName) != null), its real URI is returned
	 * - Otherwise, a fallback URI is created based on the class's code-source location (JAR or directory).
	 *
	 * @throws IllegalArgumentException if the URI cannot be constructed (e.g., missing code source).
	 */
	public static URI getResourceURI(final Class<?> clazz, String resourceName) {
		final URL resUrl = clazz.getResource(resourceName);
		if (resUrl != null)
			return URI.create(resUrl.toString());

		final CodeSource cs = clazz.getProtectionDomain().getCodeSource();
		if (cs == null)
			throw new IllegalArgumentException("Cannot determine code source for class: " + clazz.getName());
		final URL csLoc = cs.getLocation();
		if (csLoc == null)
			throw new IllegalArgumentException("Cannot determine code-source URL for class: " + clazz.getName());

		// Normalize the resourceName (strip leading slash, if present)
		resourceName = resourceName.startsWith("/")
				? resourceName.substring(1)
				: resourceName;

		final String csLocExternal = csLoc.toExternalForm();

		return switch (csLoc.getProtocol()) {
			case "file": {
				if (csLocExternal.endsWith(".jar")) {
					// Build: "jar:file:/.../myapp.jar!/normalized"
					yield URI.create((csLocExternal.endsWith("!/") ? csLocExternal : csLocExternal + "!/") + resourceName);
				}
				// Build a "file:" URI pointing to <location>/<normalized>
				yield URI.create((csLocExternal.endsWith("/") ? csLocExternal : csLocExternal + "/") + resourceName);
			}
			case "jar": {
				// Build: "jar:file:/path/to/myapp.jar!/normalized"
				yield URI.create((csLocExternal.endsWith("!/") ? csLocExternal : csLocExternal + "!/") + resourceName);
			}
			default: {
				yield URI.create((csLocExternal.endsWith("/") ? csLocExternal : csLocExternal + "/") + resourceName);
			}
		};
	}

	private ResourceUtils() {
	}
}
