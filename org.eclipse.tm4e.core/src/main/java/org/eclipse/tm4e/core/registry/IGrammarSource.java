/**
 * Copyright (c) 2022 Sebastian Thomschke and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.tm4e.core.registry;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tm4e.core.internal.utils.ResourceUtils;

public interface IGrammarSource {

	/**
	 * Supported grammar content types
	 */
	enum ContentType {
		JSON,
		YAML,
		XML
	}

	private static ContentType guessFileFormat(final String fileName) {
		final String extension = fileName.substring(fileName.lastIndexOf('.') + 1).trim().toLowerCase();

		return switch (extension) {
			case "json" -> ContentType.JSON;
			case "yaml", "yaml-tmlanguage", "yml" -> ContentType.YAML;
			case "plist", "tmlanguage", "xml" -> ContentType.XML;
			default -> throw new IllegalArgumentException("Unsupported file type: " + fileName);
		};
	}

	static IGrammarSource fromFile(final Path file) {
		return fromFile(file, null, null);
	}

	static IGrammarSource fromFile(final Path file, final @Nullable ContentType contentType, final @Nullable Charset charset) {
		final var uri = file.toUri();
		final var contentType1 = contentType == null ? guessFileFormat(file.toString()) : contentType;
		return new IGrammarSource() {
			@Override
			public Reader getReader() throws IOException {
				return Files.newBufferedReader(file, charset == null ? StandardCharsets.UTF_8 : charset);
			}

			@Override
			public URI getURI() {
				return uri;
			}

			@Override
			public ContentType getContentType() {
				return contentType1;
			}

			@Override
			public long getLastModified() {
				return file.toFile().lastModified();
			}
		};
	}

	/**
	 * @throws IllegalArgumentException if the content type is unsupported or cannot be determined
	 */
	static IGrammarSource fromResource(final Class<?> clazz, final String resourceName) {
		return fromResource(clazz, resourceName, null, null);
	}

	/**
	 * @throws IllegalArgumentException if the content type is unsupported or cannot be determined
	 */
	static IGrammarSource fromResource(final Class<?> clazz, final String resourceName, final @Nullable ContentType contentType,
			final @Nullable Charset charset) {
		final var uri = ResourceUtils.getResourceURI(clazz, resourceName);
		final var contentType1 = contentType == null ? guessFileFormat(resourceName) : contentType;
		return new IGrammarSource() {
			@Override
			public Reader getReader() throws IOException {
				return ResourceUtils.getResourceReader(clazz, resourceName, charset);
			}

			@Override
			public URI getURI() {
				return uri;
			}

			@Override
			public ContentType getContentType() {
				return contentType1;
			}

			@Override
			public long getLastModified() {
				try {
					return ResourceUtils.getResourceLastModified(clazz, resourceName);
				} catch (final IOException ex) {
					return 0;
				}
			}
		};
	}

	static IGrammarSource fromString(final ContentType contentType, final String content) {
		final var uri = URI.create("data:" //
				+ switch (contentType) {
					case JSON -> "application/json";
					case YAML -> "application/x-yaml";
					case XML -> "application/xml";
					default -> "text/plain";
				} //
				+ ";charset=UTF-8," //
				+ URLEncoder.encode(content, StandardCharsets.UTF_8));
		final long modified = System.currentTimeMillis();
		return new IGrammarSource() {
			@Override
			public Reader getReader() {
				return new StringReader(content);
			}

			@Override
			public URI getURI() {
				return uri;
			}

			@Override
			public ContentType getContentType() {
				return contentType;
			}

			@Override
			public long getLastModified() {
				return modified;
			}
		};
	}

	default ContentType getContentType() {
		return guessFileFormat(getURI().getPath());
	}

	URI getURI();

	Reader getReader() throws IOException;

	/**
	 * @return 0 if resource does not exist or modification date is not available
	 */
	long getLastModified();
}
